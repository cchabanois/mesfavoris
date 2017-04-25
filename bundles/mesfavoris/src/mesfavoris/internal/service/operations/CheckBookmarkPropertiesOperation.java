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

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
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

public class CheckBookmarkPropertiesOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IBookmarkProblems bookmarkProblems;
	private final Provider<Set<String>> nonUpdatablePropertiesProvider;
	private final Provider<Set<String>> pathPropertiesProvider;
	private final IPathPlaceholderResolver pathPlaceholderResolver;
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;

	public CheckBookmarkPropertiesOperation(BookmarkDatabase bookmarkDatabase,
			RemoteBookmarksStoreManager remoteBookmarksStoreManager,
			Provider<Set<String>> nonUpdatablePropertiesProvider, Provider<Set<String>> pathPropertiesProvider,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider, IPathPlaceholderResolver pathPlaceholderResolver,
			IBookmarkProblems bookmarkProblems) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
		this.nonUpdatablePropertiesProvider = nonUpdatablePropertiesProvider;
		this.pathPropertiesProvider = pathPropertiesProvider;
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
		Map<String, String> propertiesNeedingUpdate = getPropertiesNeedingUpdate(bookmarkId, part, selection, monitor);
		Map<String, String> propertiesUsingUndefinedPlaceholder = getPropertiesUsingUndefinedPlaceholder(bookmarkId);
		PathPlaceholderGuesser pathPlaceholderGuesser = new PathPlaceholderGuesser(pathPlaceholderResolver,
				pathPropertiesProvider.get());
		Set<PathPlaceholder> undefinedPathPlaceholders = pathPlaceholderGuesser
				.guessUndefinedPlaceholders(propertiesUsingUndefinedPlaceholder, propertiesNeedingUpdate);
		propertiesUsingUndefinedPlaceholder.keySet().forEach(propName -> propertiesNeedingUpdate.remove(propName));
		if (!propertiesNeedingUpdate.isEmpty()) {
			bookmarkProblems.add(getPropertiesNeedUpdateBookmarkProblem(bookmarkId, propertiesNeedingUpdate));
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

	private Map<String, String> getPropertiesNeedingUpdate(BookmarkId bookmarkId, IWorkbenchPart part,
			ISelection selection, IProgressMonitor monitor) {
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

	private BookmarkProblem getPropertiesNeedUpdateBookmarkProblem(BookmarkId bookmarkId,
			Map<String, String> propertiesNeedingUpdate) {
		BookmarkProblem problem = new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				propertiesNeedingUpdate);
		return problem;
	}

	private void removePropertiesNeedUpdateBookmarkProblem(BookmarkId bookmarkId) {
		bookmarkProblems.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE)
				.ifPresent(problem -> bookmarkProblems.delete(problem));
	}

	private BookmarkProblem getPlaceholderBookmarkProblem(BookmarkId bookmarkId,
			Map<String, String> propertiesUsingUndefinedPlaceholder, Set<PathPlaceholder> undefinedPathPlaceholders) {
		Map<String, String> properties = new HashMap<>(propertiesUsingUndefinedPlaceholder);
		undefinedPathPlaceholders.forEach(pathPlaceholder -> properties.put("${" + pathPlaceholder.getName() + "}",
				pathPlaceholder.getPath().toPortableString()));
		BookmarkProblem problem = new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED,
				properties);
		return problem;
	}

	private BookmarkProblem getLocalPathSharedBookmarkProblem(BookmarkId bookmarkId,
			Map<String, String> propertiesWithLocalPath) {
		return new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_LOCAL_PATH_SHARED, propertiesWithLocalPath);
	}

	private void removeLocalPathSharedBookmarkProblem(BookmarkId bookmarkId) {
		bookmarkProblems.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_LOCAL_PATH_SHARED)
				.ifPresent(problem -> bookmarkProblems.delete(problem));
	}

	private void removePlaceholderBookmarkProblem(BookmarkId bookmarkId) {
		bookmarkProblems.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED)
				.ifPresent(problem -> bookmarkProblems.delete(problem));
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
			if (!newBookmarkProblems.stream().filter(bookmarkProblem -> bookmarkProblem.getProblemType()
					.equals(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE)).findAny().isPresent()) {
				removePropertiesNeedUpdateBookmarkProblem(bookmarkId);
			}
			if (!newBookmarkProblems.stream().filter(bookmarkProblem -> bookmarkProblem.getProblemType()
					.equals(BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED)).findAny().isPresent()) {
				removePlaceholderBookmarkProblem(bookmarkId);
			}
			if (!newBookmarkProblems.stream().filter(
					bookmarkProblem -> bookmarkProblem.getProblemType().equals(BookmarkProblem.TYPE_LOCAL_PATH_SHARED))
					.findAny().isPresent()) {
				removeLocalPathSharedBookmarkProblem(bookmarkId);
			}
			return Status.OK_STATUS;
		}

	}

}
