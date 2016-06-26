package mesfavoris.internal.operations;

import java.time.Duration;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import mesfavoris.commons.core.jobs.ConditionJob;
import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;

public class ShowInBookmarksViewOperation {
	private final BookmarkDatabase bookmarkDatabase;

	public ShowInBookmarksViewOperation(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	public void showInBookmarksView(IWorkbenchPage page, BookmarkId bookmarkId) {
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		Display.getDefault().asyncExec(() -> {
			ISelection selection = new StructuredSelection(bookmark);
			setBookmarksViewSelection(page, selection);
			if (!selection.equals(getBookmarksViewSelection(page))) {
				// we have to use a job because bookmark is not yet in the TreeViewer if
				// just added
				SelectBookmarkJob job = new SelectBookmarkJob(page, bookmark);
				job.schedule();				
			}
		});
		
	}

	private ISelection getBookmarksViewSelection(IWorkbenchPage page) {
		try {
			BookmarksView bookmarksView = (BookmarksView) page.showView(BookmarksView.ID);
			return bookmarksView.getSite().getSelectionProvider().getSelection();
		} catch (PartInitException e) {
			return new StructuredSelection();
		}
	}

	private void setBookmarksViewSelection(IWorkbenchPage page, final ISelection selection) {
		try {
			BookmarksView bookmarksView = (BookmarksView) page.showView(BookmarksView.ID);
			bookmarksView.getSite().getSelectionProvider().setSelection(selection);
		} catch (PartInitException e) {
		}
	}	
	
	private class SelectBookmarkJob extends ConditionJob {
		private final IWorkbenchPage page;
		private final Bookmark bookmark;

		public SelectBookmarkJob(IWorkbenchPage page, Bookmark bookmark) {
			super("Show bookmark", Duration.ofSeconds(2));
			this.page = page;
			this.bookmark = bookmark;
		}

		@Override
		protected boolean condition() throws Exception {
			final boolean[] result = new boolean[] { false };
			Display.getDefault().syncExec(() -> {
				ISelection selection = new StructuredSelection(bookmark);
				setBookmarksViewSelection(page, selection);
				result[0] = selection.equals(getBookmarksViewSelection(page));
			});
			return result[0];
		}

	}

}
