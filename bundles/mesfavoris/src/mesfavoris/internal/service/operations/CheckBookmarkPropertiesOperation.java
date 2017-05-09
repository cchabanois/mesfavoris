package mesfavoris.internal.service.operations;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Provider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertyDescriptors;
import mesfavoris.bookmarktype.NonUpdatablePropertiesProvider;
import mesfavoris.bookmarktype.PathPropertiesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertyObsolescenceSeverityProvider.ObsolescenceSeverity;
import mesfavoris.internal.Constants;
import mesfavoris.internal.placeholders.PathPlaceholderGuesser;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.placeholders.IPathPlaceholderResolver;
import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblems;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import static mesfavoris.problems.BookmarkProblem.*;

public class CheckBookmarkPropertiesOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IBookmarkProblems bookmarkProblems;
	private final Provider<Set<String>> nonUpdatablePropertiesProvider;
	private final Provider<Set<String>> pathPropertiesProvider;
	private final IPathPlaceholderResolver pathPlaceholderResolver;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private final IBookmarkPropertyDescriptors bookmarkPropertyDescriptors;

	public CheckBookmarkPropertiesOperation(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			IBookmarkPropertyDescriptors bookmarkPropertyDescriptors,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider, IPathPlaceholderResolver pathPlaceholderResolver,
			IBookmarkProblems bookmarkProblems) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.bookmarkPropertyDescriptors = bookmarkPropertyDescriptors;
		this.nonUpdatablePropertiesProvider = new NonUpdatablePropertiesProvider(bookmarkPropertyDescriptors);
		this.pathPropertiesProvider = new PathPropertiesProvider(bookmarkPropertyDescriptors);
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.bookmarkProblems = bookmarkProblems;
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	/**
	 * Check bookmark properties problems. Will add or remove
	 * {@link BookmarkProblem}s.
	 * 
	 * @param bookmarkId
	 * @param part
	 * @param selection
	 */
	public void checkBookmarkPropertiesProblems(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection) {
		new CheckBookmarkProperties(bookmarkId, part, selection).schedule();
	}

	public void checkBookmarkPropertiesProblems(BookmarkId bookmarkId) {
		new CheckBookmarkProperties(bookmarkId).schedule();
	}

	public Set<BookmarkProblem> getBookmarkPropertiesProblems(BookmarkId bookmarkId) {
		Set<BookmarkProblem> bookmarkProblems = new HashSet<>();
		Map<String, String> propertiesUsingUndefinedPlaceholder = getPropertiesUsingUndefinedPlaceholder(bookmarkId);
		if (!propertiesUsingUndefinedPlaceholder.isEmpty()) {
			bookmarkProblems.add(getPlaceholderBookmarkProblem(bookmarkId, propertiesUsingUndefinedPlaceholder,
					Collections.emptySet()));
		}
		Map<String, String> propertiesUsingLocalPath = getPropertiesWithLocalPathShared(bookmarkId);
		if (!propertiesUsingLocalPath.isEmpty()) {
			bookmarkProblems.add(getLocalPathSharedBookmarkProblem(bookmarkId, propertiesUsingLocalPath));
		}
		return bookmarkProblems;
	}

	public Set<BookmarkProblem> getBookmarkPropertiesProblems(BookmarkId bookmarkId, IWorkbenchPart part,
			ISelection selection, IProgressMonitor monitor) {
		Set<BookmarkProblem> bookmarkProblems = new HashSet<>();
		Map<String, String> obsoleteProperties = getObsoleteProperties(bookmarkId, part, selection, monitor);
		Map<String, String> propertiesUsingUndefinedPlaceholder = getPropertiesUsingUndefinedPlaceholder(bookmarkId);
		PathPlaceholderGuesser pathPlaceholderGuesser = new PathPlaceholderGuesser(pathPlaceholderResolver,
				pathPropertiesProvider.get());
		Set<PathPlaceholder> undefinedPathPlaceholders = pathPlaceholderGuesser
				.guessUndefinedPlaceholders(propertiesUsingUndefinedPlaceholder, obsoleteProperties);
		propertiesUsingUndefinedPlaceholder.keySet().forEach(propName -> obsoleteProperties.remove(propName));
		if (!obsoleteProperties.isEmpty()) {
			bookmarkProblems.addAll(getObsoletePropertiesBookmarkProblems(bookmarkId, obsoleteProperties));
		}
		if (!propertiesUsingUndefinedPlaceholder.isEmpty()) {
			bookmarkProblems.add(getPlaceholderBookmarkProblem(bookmarkId, propertiesUsingUndefinedPlaceholder,
					undefinedPathPlaceholders));
		}
		Map<String, String> propertiesUsingLocalPath = getPropertiesWithLocalPathShared(bookmarkId);
		if (!propertiesUsingLocalPath.isEmpty()) {
			bookmarkProblems.add(getLocalPathSharedBookmarkProblem(bookmarkId, propertiesUsingLocalPath));
		}
		return bookmarkProblems;
	}

	/**
	 * Get the obsolete properties
	 * 
	 * @param bookmarkId
	 * @param part
	 * @param selection
	 * @param monitor
	 * @return a {@link Map} where keys are property names and values are new
	 *         property values
	 */
	private Map<String, String> getObsoleteProperties(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		Map<String, String> bookmarkProperties = new HashMap<>();
		Map<String, String> propertiesNeedingUpdate = new HashMap<>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, part, selection, monitor);
		Set<String> nonUpdatableProperties = nonUpdatablePropertiesProvider.get();
		for (String propName : bookmarkProperties.keySet()) {
			String newValue = bookmarkProperties.get(propName);
			if (!nonUpdatableProperties.contains(propName) && !newValue.equals(bookmark.getPropertyValue(propName))) {
				propertiesNeedingUpdate.put(propName, newValue);
			}
		}
		return propertiesNeedingUpdate;
	}

	private Map<String, String> getPropertiesUsingUndefinedPlaceholder(BookmarkId bookmarkId) {
		Map<String, String> propertiesWithUndefinedPlaceholder = new HashMap<>();
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		for (String propName : bookmark.getProperties().keySet()) {
			String propValue = bookmark.getPropertyValue(propName);
			String placeholderName = PathPlaceholderResolver.getPlaceholderName(propValue);
			if (placeholderName != null && pathPlaceholderResolver.expand(propValue) == null) {
				propertiesWithUndefinedPlaceholder.put(propName, propValue);
			}
		}
		return propertiesWithUndefinedPlaceholder;
	}

	private Map<String, String> getPropertiesWithLocalPathShared(BookmarkId bookmarkId) {
		if (!isUnderRemoteBookmarkFolder(bookmarkId)) {
			return Collections.emptyMap();
		}
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		Set<String> pathProperties = pathPropertiesProvider.get();
		return bookmark.getProperties().keySet().stream()
				.filter(propertyName -> isPropertyValueWithLocalPath(pathProperties, bookmark, propertyName))
				.collect(Collectors.toMap(propertyName -> propertyName,
						propertyName -> bookmark.getPropertyValue(propertyName)));
	}

	private boolean isPropertyValueWithLocalPath(Set<String> pathProperties, Bookmark bookmark, String propertyName) {
		if (!pathProperties.contains(propertyName)) {
			return false;
		}
		String placeholderName = PathPlaceholderResolver.getPlaceholderName(bookmark.getPropertyValue(propertyName));
		return placeholderName == null || Constants.PLACEHOLDER_HOME_NAME.equals(placeholderName);
	}

	private boolean isUnderRemoteBookmarkFolder(BookmarkId bookmarkId) {
		return remoteBookmarksStoreManager.getRemoteBookmarkFolderContaining(bookmarkDatabase.getBookmarksTree(),
				bookmarkId) != null;
	}

	private Set<BookmarkProblem> getObsoletePropertiesBookmarkProblems(BookmarkId bookmarkId,
			Map<String, String> obsoleteProperties) {
		Map<String, String> warningProperties = new HashMap<>();
		Map<String, String> infoProperties = new HashMap<>();
		for (Map.Entry<String, String> entry : obsoleteProperties.entrySet()) {
			BookmarkPropertyDescriptor propertyDescriptor = bookmarkPropertyDescriptors
					.getPropertyDescriptor(entry.getKey());
			if (propertyDescriptor == null) {
				warningProperties.put(entry.getKey(), entry.getValue());
			} else {
				Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
				ObsolescenceSeverity severity = propertyDescriptor.getObsolescenceSeverity(bookmark, obsoleteProperties,
						entry.getKey());
				if (severity == ObsolescenceSeverity.INFO) {
					infoProperties.put(entry.getKey(), entry.getValue());
				} else if (severity == ObsolescenceSeverity.WARNING) {
					warningProperties.put(entry.getKey(), entry.getValue());
				}
			}
		}
		Set<BookmarkProblem> bookmarkProblems = new HashSet<>();
		if (!warningProperties.isEmpty()) {
			bookmarkProblems.add(new BookmarkProblem(bookmarkId, TYPE_PROPERTIES_NEED_UPDATE, warningProperties));
		}
		if (!infoProperties.isEmpty()) {
			bookmarkProblems.add(new BookmarkProblem(bookmarkId, TYPE_PROPERTIES_MAY_UPDATE, infoProperties));

		}
		return bookmarkProblems;
	}

	private void removeBookmarkProblem(BookmarkId bookmarkId, String problemType) {
		bookmarkProblems.getBookmarkProblem(bookmarkId, problemType)
				.ifPresent(problem -> bookmarkProblems.delete(problem));
	}

	private BookmarkProblem getPlaceholderBookmarkProblem(BookmarkId bookmarkId,
			Map<String, String> propertiesUsingUndefinedPlaceholder, Set<PathPlaceholder> undefinedPathPlaceholders) {
		Map<String, String> properties = new HashMap<>(propertiesUsingUndefinedPlaceholder);
		undefinedPathPlaceholders.forEach(pathPlaceholder -> properties.put("${" + pathPlaceholder.getName() + "}",
				pathPlaceholder.getPath().toPortableString()));
		BookmarkProblem problem = new BookmarkProblem(bookmarkId, TYPE_PLACEHOLDER_UNDEFINED, properties);
		return problem;
	}

	private BookmarkProblem getLocalPathSharedBookmarkProblem(BookmarkId bookmarkId,
			Map<String, String> propertiesWithLocalPath) {
		return new BookmarkProblem(bookmarkId, TYPE_LOCAL_PATH_SHARED, propertiesWithLocalPath);
	}

	private class CheckBookmarkProperties extends Job {
		private final BookmarkId bookmarkId;
		private final IWorkbenchPart part;
		private final ISelection selection;

		public CheckBookmarkProperties(BookmarkId bookmarkId) {
			super("Checking bookmark properties");
			this.bookmarkId = bookmarkId;
			this.part = null;
			this.selection = null;
		}

		public CheckBookmarkProperties(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection) {
			super("Checking bookmark properties");
			this.bookmarkId = bookmarkId;
			this.part = part;
			this.selection = selection;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Set<BookmarkProblem> newBookmarkProblems;
			if (part == null) {
				newBookmarkProblems = getBookmarkPropertiesProblems(bookmarkId);
			} else {
				newBookmarkProblems = getBookmarkPropertiesProblems(bookmarkId, part, selection, monitor);
			}
			newBookmarkProblems.forEach(bookmarkProblem -> bookmarkProblems.add(bookmarkProblem));
			if (!containsBookmarkProblem(newBookmarkProblems, TYPE_PROPERTIES_NEED_UPDATE)) {
				removeBookmarkProblem(bookmarkId, TYPE_PROPERTIES_NEED_UPDATE);
			}
			if (!containsBookmarkProblem(newBookmarkProblems, TYPE_PROPERTIES_MAY_UPDATE)) {
				removeBookmarkProblem(bookmarkId, TYPE_PROPERTIES_MAY_UPDATE);
			}
			if (!containsBookmarkProblem(newBookmarkProblems, TYPE_PLACEHOLDER_UNDEFINED)) {
				removeBookmarkProblem(bookmarkId, TYPE_PLACEHOLDER_UNDEFINED);
			}
			if (!containsBookmarkProblem(newBookmarkProblems, TYPE_LOCAL_PATH_SHARED)) {
				removeBookmarkProblem(bookmarkId, TYPE_LOCAL_PATH_SHARED);
			}
			return Status.OK_STATUS;
		}

		private boolean containsBookmarkProblem(Set<BookmarkProblem> bookmarkProblems, String problemType) {
			return bookmarkProblems.stream()
					.filter(bookmarkProblem -> bookmarkProblem.getProblemType().equals(problemType)).findAny()
					.isPresent();
		}

	}

}
