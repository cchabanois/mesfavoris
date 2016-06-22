package mesfavoris.internal.handlers;

import java.time.Duration;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.commons.core.jobs.ConditionJob;
import mesfavoris.internal.operations.AddBookmarkOperation;
import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.workspace.DefaultBookmarkFolderManager;

public class AddTextEditorBookmarkHandler extends AbstractHandler {
	private final DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final BookmarkDatabase bookmarkDatabase;

	public AddTextEditorBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
		this.defaultBookmarkFolderManager = BookmarksPlugin.getDefaultBookmarkFolderManager();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ITextEditor textEditor = getActiveEditor(page);
		AddBookmarkOperation addBookmarkOperation = new AddBookmarkOperation(bookmarkDatabase,
				bookmarkPropertiesProvider, defaultBookmarkFolderManager);
		BookmarkId bookmarkId;
		try {
			bookmarkId = addBookmarkOperation.addBookmark(textEditor);
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not add bookmark", e);
		}
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		displayBookmarkInBookmarksView(page, bookmark);
		return null;
	}

	private ITextEditor getActiveEditor(IWorkbenchPage page) {
		if (page != null) {
			IEditorPart editor = page.getActiveEditor();
			ITextEditor textEditor = AdapterUtils.getAdapter(editor, ITextEditor.class);
			if (textEditor != null) {
				return textEditor;
			}
		}
		return null;
	}

	private void displayBookmarkInBookmarksView(IWorkbenchPage page, Bookmark bookmark) {
		if (page == null) {
			return;
		}
		// we have to use a job because added bookmark is not yet in the TreeViewer
		new SelectBookmarkJob(page, bookmark).schedule();
	}

	private static class SelectBookmarkJob extends ConditionJob {
		private final IWorkbenchPage page;
		private final Bookmark bookmark;

		public SelectBookmarkJob(IWorkbenchPage page, Bookmark bookmark) {
			super("Select added bookmark", Duration.ofSeconds(2));
			this.page = page;
			this.bookmark = bookmark;
		}

		@Override
		protected boolean condition() throws Exception {
			ISelection selection = new StructuredSelection(bookmark);
			setSelection(selection);
			return selection.equals(getSelection());
		}

		private ISelection getSelection() {
			final ISelection[] selection = new ISelection[] { new StructuredSelection() };
			Display.getDefault().syncExec(() -> {
				BookmarksView bookmarksView;
				try {
					bookmarksView = (BookmarksView) page.showView(BookmarksView.ID);
					selection[0] = bookmarksView.getSite().getSelectionProvider().getSelection();
				} catch (PartInitException e) {
				}
			});
			return selection[0];
		}

		private void setSelection(final ISelection selection) {
			Display.getDefault().asyncExec(() -> {
				BookmarksView bookmarksView;
				try {
					bookmarksView = (BookmarksView) page.showView(BookmarksView.ID);
					bookmarksView.getSite().getSelectionProvider().setSelection(selection);
				} catch (PartInitException e) {
				}
			});
		}

	}

}
