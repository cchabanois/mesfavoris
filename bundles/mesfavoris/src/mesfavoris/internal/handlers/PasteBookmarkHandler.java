package mesfavoris.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.operations.PasteBookmarkOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.validation.BookmarkModificationValidator;

public class PasteBookmarkHandler extends AbstractBookmarkHandler {
	private final BookmarkDatabase bookmarkDatabase;
	private final BookmarkModificationValidator bookmarkModificationValidator;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;

	public PasteBookmarkHandler() {
		this.bookmarkDatabase = BookmarksPlugin.getBookmarkDatabase();
		this.bookmarkModificationValidator = new BookmarkModificationValidator(
				BookmarksPlugin.getRemoteBookmarksStoreManager());
		this.bookmarkPropertiesProvider = BookmarksPlugin.getBookmarkPropertiesProvider();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.size() != 1 || !(selection.getFirstElement() instanceof BookmarkFolder)) {
			return null;
		}
		BookmarkFolder bookmarkFolder = (BookmarkFolder) selection.getFirstElement();
		PasteBookmarkOperation operation = new PasteBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider,
				bookmarkModificationValidator);
		try {
			operation.paste(bookmarkFolder.getId());
		} catch (BookmarksException e) {
			throw new ExecutionException("Could not paste bookmarks", e);
		}

		return null;
	}

}
