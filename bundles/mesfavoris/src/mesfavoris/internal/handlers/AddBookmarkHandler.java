package mesfavoris.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.internal.operations.AddBookmarkOperation;
import mesfavoris.internal.operations.ShowInBookmarksViewOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.workspace.DefaultBookmarkFolderManager;

public class AddBookmarkHandler extends AbstractHandler {
	private final DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final BookmarkDatabase bookmarkDatabase;
	private final ShowInBookmarksViewOperation showInBookmarksViewOperation;

	public AddBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
		this.defaultBookmarkFolderManager = BookmarksPlugin.getDefaultBookmarkFolderManager();
		this.showInBookmarksViewOperation = new ShowInBookmarksViewOperation(bookmarkDatabase);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part == null) {
			return null;
		}
		ISelection selection;
		if (part instanceof IEditorPart) {
			ITextEditor textEditor = AdapterUtils.getAdapter(part, ITextEditor.class);
			if (textEditor == null) {
				return null;
			}
			selection = textEditor.getSelectionProvider().getSelection();
			part = textEditor;
		} else if (part instanceof IViewPart) {
			selection = HandlerUtil.getCurrentSelection(event);
		} else {
			return null;
		}
		AddBookmarkOperation addBookmarkOperation = new AddBookmarkOperation(bookmarkDatabase,
				bookmarkPropertiesProvider, defaultBookmarkFolderManager);
		BookmarkId bookmarkId;
		try {
			bookmarkId = addBookmarkOperation.addBookmark(part, selection);
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not add bookmark", e);
		}
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		if (page != null) {
			displayBookmarkInBookmarksView(page, bookmarkId);
		}
		return null;
	}

	private void displayBookmarkInBookmarksView(IWorkbenchPage page, BookmarkId bookmarkId) {
		if (page == null) {
			return;
		}
		showInBookmarksViewOperation.showInBookmarksView(page, bookmarkId);
	}

}
