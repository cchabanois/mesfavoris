package org.chabanois.mesfavoris.internal.handlers;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.BookmarksPlugin;
import org.chabanois.mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import org.chabanois.mesfavoris.internal.operations.AddBookmarkOperation;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.utils.AdapterUtils;
import org.chabanois.mesfavoris.workspace.DefaultBookmarkFolderManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class AddTextEditorBookmarkHandler extends AbstractHandler {
	private final DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final BookmarkDatabase bookmarkDatabase;

	public AddTextEditorBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
		this.defaultBookmarkFolderManager = BookmarksPlugin
				.getDefaultBookmarkFolderManager();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ITextEditor textEditor = getActiveEditor();
		AddBookmarkOperation addBookmarkOperation = new AddBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider, defaultBookmarkFolderManager);
		try {
			addBookmarkOperation.addBookmark(textEditor);
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not add bookmark", e);
		}
		return null;
	}

	private ITextEditor getActiveEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IEditorPart editor = page.getActiveEditor();
				ITextEditor textEditor = AdapterUtils.getAdapter(editor, ITextEditor.class);
				if (textEditor != null) {
					return textEditor;
				}
			}
		}
		return null;
	}

}
