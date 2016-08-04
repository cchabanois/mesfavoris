package mesfavoris.internal.operations;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.URLTransfer;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.model.copy.BookmarksCopier;
import mesfavoris.internal.model.copy.NonExistingBookmarkIdProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import mesfavoris.validation.IBookmarkModificationValidator;

public class PasteBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public PasteBookmarkOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void paste(BookmarkId parentBookmarkId) throws BookmarksException {
		String clipboardContents = getClipboardContents();
		BookmarksTree bookmarksTree = getBookmarksTree(clipboardContents);
		if (bookmarksTree != null) {
			paste(parentBookmarkId, bookmarksTree);
			return;
		}
		IStructuredSelection selection = getStructuredSelectionFromClipboard();
		if (!selection.isEmpty()) {
			paste(parentBookmarkId, selection);
			return;
		}
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

	private IStructuredSelection getStructuredSelectionFromClipboard() {
		Clipboard clipboard = new Clipboard(null);
		try {
			URL url = (URL) clipboard.getContents(URLTransfer.getInstance());
			if (url != null) {
				return new StructuredSelection(url);
			}
			String text = (String) clipboard.getContents(TextTransfer.getInstance());
			if (text != null) {
				try {
					url = new URL(text);
					return new StructuredSelection(url);
				} catch (MalformedURLException e) {

				}
			}
			String[] paths = (String[]) clipboard.getContents(FileTransfer.getInstance());
			if (paths != null) {
				return new StructuredSelection(Arrays.stream(paths).map(Path::new).collect(Collectors.toList()));
			}
			return new StructuredSelection();
		} finally {
			clipboard.dispose();
		}
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

	private void paste(BookmarkId parentBookmarkId, IStructuredSelection selection) throws BookmarksException {
		List<Bookmark> bookmarks = getBookmarks(selection);
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			IStatus status = bookmarkModificationValidator.validateModification(bookmarksTreeModifier.getCurrentTree(),
					parentBookmarkId);
			if (!status.isOK()) {
				throw new BookmarksException(status);
			}
			bookmarksTreeModifier.addBookmarks(parentBookmarkId, bookmarks);
		});
	}

	private List<Bookmark> getBookmarks(IStructuredSelection selection) {
		List<Bookmark> bookmarks = Lists.newArrayList();
		for (Iterator<Object> it = selection.iterator(); it.hasNext();) {
			Object object = it.next();
			if (object instanceof Bookmark) {
				Bookmark bookmark = (Bookmark) object;
				bookmarks.add(bookmark);
			} else {
				Bookmark bookmark = createBookmark(object);
				if (bookmark != null) {
					bookmarks.add(bookmark);
				}
			}
		}
		return bookmarks;
	}

	private Bookmark createBookmark(Object object) {
		Map<String, String> bookmarkProperties = new HashMap<String, String>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, null, new StructuredSelection(object));
		Bookmark bookmark = new Bookmark(new BookmarkId(), bookmarkProperties);
		return bookmark;
	}

}
