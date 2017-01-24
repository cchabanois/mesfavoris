package mesfavoris.gdrive.handlers;

import java.util.Optional;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.gdrive.operations.ViewInGDriveOperation;
import mesfavoris.handlers.AbstractBookmarkHandler;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;

public class ViewInGDriveHandler extends AbstractBookmarkHandler {
	private final BookmarkMappingsStore bookmarkMappingsStore;

	public ViewInGDriveHandler() {
		this.bookmarkMappingsStore = Activator.getDefault().getBookmarkMappingsStore();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		BookmarkFolder bookmarkFolder = getSelectedBookmarkFolder(selection);
		if (bookmarkFolder == null) {
			return null;
		}
		Optional<String> fileId = bookmarkMappingsStore.getMapping(bookmarkFolder.getId()).map(mapping->mapping.getFileId());
		if (!fileId.isPresent()) {
			return null;
		}
		ViewInGDriveOperation operation = new ViewInGDriveOperation();
		operation.viewInGDrive(fileId.get());
		return null;
	}

	@Override
	public boolean isEnabled() {
		BookmarkFolder bookmarkFolder = getSelectedBookmarkFolder(getSelection());
		if (bookmarkFolder == null) {
			return false;
		}
		Optional<String> fileId = bookmarkMappingsStore.getMapping(bookmarkFolder.getId()).map(mapping->mapping.getFileId());
		if (!fileId.isPresent()) {
			return false;
		}
		return true;
	}

	private BookmarkFolder getSelectedBookmarkFolder(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return null;
		}
		Bookmark bookmark = (Bookmark) selection.getFirstElement();
		if (!(bookmark instanceof BookmarkFolder)) {
			return null;
		}
		BookmarkFolder bookmarkFolder = (BookmarkFolder) bookmark;
		return bookmarkFolder;
	}
}
