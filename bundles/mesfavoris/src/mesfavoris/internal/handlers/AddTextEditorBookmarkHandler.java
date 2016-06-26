package mesfavoris.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
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

public class AddTextEditorBookmarkHandler extends AbstractHandler {
	private final DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final BookmarkDatabase bookmarkDatabase;
	private final ShowInBookmarksViewOperation showInBookmarksViewOperation;
	
	public AddTextEditorBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
		this.defaultBookmarkFolderManager = BookmarksPlugin.getDefaultBookmarkFolderManager();
		this.showInBookmarksViewOperation = new ShowInBookmarksViewOperation(bookmarkDatabase);
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
		displayBookmarkInBookmarksView(page, bookmarkId);
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

	private void displayBookmarkInBookmarksView(IWorkbenchPage page, BookmarkId bookmarkId) {
		if (page == null) {
			return;
		}
		showInBookmarksViewOperation.showInBookmarksView(page, bookmarkId);
	}

}
