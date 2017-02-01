package mesfavoris.internal.service.operations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.problems.IBookmarkProblems;

public class CheckBookmarkPropertiesOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IBookmarkProblems bookmarkProblems;

	public CheckBookmarkPropertiesOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider, IBookmarkProblems bookmarkProblems) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.bookmarkProblems = bookmarkProblems;
	}

	/**
	 * Check if bookmark properties need update. Add a {@link BookmarkProblem}
	 * if it is the case.
	 * 
	 * @param bookmarkId
	 * @param part
	 * @param selection
	 */
	public void checkBookmarkPropertiesProblem(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection) {
		new CheckBookmarkProperties(bookmarkId, part, selection).schedule();
	}

	public Map<String, String> getPropertiesNeedingUpdate(BookmarkId bookmarkId, IWorkbenchPart part,
			ISelection selection, IProgressMonitor monitor) {
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		Map<String, String> bookmarkProperties = new HashMap<>();
		Map<String, String> propertiesNeedingUpdate = new HashMap<>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, part, selection, monitor);
		for (String propName : bookmarkProperties.keySet()) {
			String newValue = bookmarkProperties.get(propName);
			if (!newValue.equals(bookmark.getPropertyValue(propName))) {
				propertiesNeedingUpdate.put(propName, newValue);
			}
		}
		return propertiesNeedingUpdate;
	}

	private void addPropertiesNeedUpdateBookmarkProblem(BookmarkId bookmarkId,
			Map<String, String> propertiesNeedingUpdate) {
		BookmarkProblem problem = new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				Severity.WARNING, propertiesNeedingUpdate);
		bookmarkProblems.add(problem);
	}
	
	private void removePropertiesNeedUpdateBookmarkProblem(BookmarkId bookmarkId) {
		bookmarkProblems.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE)
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
			Map<String, String> propertiesNeedingUpdate = getPropertiesNeedingUpdate(bookmarkId, part, selection,
					monitor);
			if (!propertiesNeedingUpdate.isEmpty()) {
				addPropertiesNeedUpdateBookmarkProblem(bookmarkId, propertiesNeedingUpdate);
			} else {
				removePropertiesNeedUpdateBookmarkProblem(bookmarkId);
			}
			return Status.OK_STATUS;
		}

	}

}
