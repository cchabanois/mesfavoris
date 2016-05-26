package mesfavoris.internal.operations;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;

import mesfavoris.BookmarksException;
import mesfavoris.internal.model.copy.BookmarksCopier;
import mesfavoris.internal.model.copy.NonExistingBookmarkIdProvider;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import mesfavoris.validation.IBookmarkModificationValidator;

public class PasteBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public PasteBookmarkOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void paste(BookmarkId parentBookmarkId) throws BookmarksException {
		String clipboardContents = getClipboardContents();
		BookmarksTree bookmarksTree = getBookmarksTree(clipboardContents);
		if (bookmarksTree == null) {
			return;
		}
		paste(parentBookmarkId, bookmarksTree);
	}

	private String getClipboardContents() {
		Clipboard clipboard = new Clipboard(null);
		String textData;
		try {
			TextTransfer textTransfer = TextTransfer.getInstance();
			textData = (String) clipboard.getContents(textTransfer);
		} finally {
			clipboard.dispose();
		}
		return textData;
	}

	private BookmarksTree getBookmarksTree(String clipboardContents) throws BookmarksException {
		BookmarksTreeJsonDeserializer deserializer = new BookmarksTreeJsonDeserializer();
		try {
			return deserializer.deserialize(new StringReader(clipboardContents), new NullProgressMonitor());
		} catch (IOException e) {
			// clipboard does not contain bookmarks
			return null;
		}
	}

	private void paste(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			IStatus status = bookmarkModificationValidator.validateModification(bookmarksTreeModifier.getCurrentTree(),
					parentBookmarkId);
			if (!status.isOK()) {
				throw new BookmarksException(status);
			}
			BookmarksCopier bookmarksCopier = new BookmarksCopier(sourceBookmarksTree,
					new NonExistingBookmarkIdProvider(bookmarksTreeModifier.getCurrentTree()));
			List<BookmarkId> bookmarkIds = sourceBookmarksTree.getChildren(sourceBookmarksTree.getRootFolder().getId())
					.stream().map(b -> b.getId()).collect(Collectors.toList());
			bookmarksCopier.copy(bookmarksTreeModifier, parentBookmarkId, bookmarkIds);
		});
	}

}
