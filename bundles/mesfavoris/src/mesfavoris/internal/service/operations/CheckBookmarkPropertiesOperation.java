package mesfavoris.internal.service.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.placeholders.PathPlaceholderGuesser;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.placeholders.IPathPlaceholderResolver;
import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.problems.IBookmarkProblems;

public class CheckBookmarkPropertiesOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IBookmarkProblems bookmarkProblems;
	private final Set<String> nonUpdatableProperties;
	private final IPathPlaceholderResolver pathPlaceholderResolver;
	private final PathPlaceholderGuesser pathPlaceholderGuesser;

	public CheckBookmarkPropertiesOperation(BookmarkDatabase bookmarkDatabase, Set<String> nonUpdatableProperties,
			Set<String> pathProperties, IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			IPathPlaceholderResolver pathPlaceholderResolver, IBookmarkProblems bookmarkProblems) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.nonUpdatableProperties = nonUpdatableProperties;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.bookmarkProblems = bookmarkProblems;
		this.pathPlaceholderResolver = pathPlaceholderResolver;
		this.pathPlaceholderGuesser = new PathPlaceholderGuesser(pathPlaceholderResolver, pathProperties);
	}

	/**
	 * Check if bookmark properties need update. Add a {@link BookmarkProblem}
	 * if it is the case.
	 * 
	 * @param bookmarkId
	 * @param part
	 * @param selection
	 */
	public void checkBookmarkPropertiesProblems(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection) {
		new CheckBookmarkProperties(bookmarkId, part, selection).schedule();
	}

	public Set<BookmarkProblem> getBookmarkPropertiesProblems(BookmarkId bookmarkId, IWorkbenchPart part,
			ISelection selection, IProgressMonitor monitor) {
		Set<BookmarkProblem> bookmarkProblems = new HashSet<>();
		Map<String, String> propertiesNeedingUpdate = getPropertiesNeedingUpdate(bookmarkId, part, selection, monitor);
		Map<String, String> propertiesUsingUndefinedPlaceholder = getPropertiesUsingUndefinedPlaceholder(bookmarkId);
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
		return bookmarkProblems;
	}

	private Map<String, String> getPropertiesNeedingUpdate(BookmarkId bookmarkId, IWorkbenchPart part,
			ISelection selection, IProgressMonitor monitor) {
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		Map<String, String> bookmarkProperties = new HashMap<>();
		Map<String, String> propertiesNeedingUpdate = new HashMap<>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, part, selection, monitor);
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

	private BookmarkProblem getPropertiesNeedUpdateBookmarkProblem(BookmarkId bookmarkId,
			Map<String, String> propertiesNeedingUpdate) {
		BookmarkProblem problem = new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				Severity.WARNING, propertiesNeedingUpdate);
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
				Severity.WARNING, properties);
		return problem;
	}

	private void removePlaceholderBookmarkProblem(BookmarkId bookmarkId) {
		bookmarkProblems.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED)
				.ifPresent(problem -> bookmarkProblems.delete(problem));
	}

	private class CheckBookmarkProperties extends Job {
		private final BookmarkId bookmarkId;
		private final IWorkbenchPart part;
		private final ISelection selection;

		public CheckBookmarkProperties(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection) {
			super("Checking bookmark properties");
			this.bookmarkId = bookmarkId;
			this.part = part;
			this.selection = selection;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			Set<BookmarkProblem> newBookmarkProblems = getBookmarkPropertiesProblems(bookmarkId, part, selection,
					monitor);
			newBookmarkProblems.forEach(bookmarkProblem -> bookmarkProblems.add(bookmarkProblem));
			if (!newBookmarkProblems.stream().filter(bookmarkProblem -> bookmarkProblem.getProblemType()
					.equals(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE)).findAny().isPresent()) {
				removePropertiesNeedUpdateBookmarkProblem(bookmarkId);
			}
			if (!newBookmarkProblems.stream().filter(bookmarkProblem -> bookmarkProblem.getProblemType()
					.equals(BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED)).findAny().isPresent()) {
				removePlaceholderBookmarkProblem(bookmarkId);
			}
			return Status.OK_STATUS;
		}

	}

}