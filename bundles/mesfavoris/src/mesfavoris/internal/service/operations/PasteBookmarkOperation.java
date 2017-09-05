package mesfavoris.internal.service.operations;

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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

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

public class PasteBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;

	public PasteBookmarkOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
	}

	public void paste(BookmarkId parentBookmarkId, IProgressMonitor monitor) throws BookmarksException {
		Display display = PlatformUI.getWorkbench().getDisplay();
		String clipboardContents = getClipboardContentsFromUIThread(display);
		BookmarksTree bookmarksTree = getBookmarksTree(clipboardContents);
		if (bookmarksTree != null) {
			paste(parentBookmarkId, bookmarksTree, monitor);
			return;
		}
		IStructuredSelection selection = getStructuredSelectionFromClipboardFromUIThread(display);
		if (!selection.isEmpty()) {
			paste(parentBookmarkId, selection, monitor);
			return;
		}
	}

	public void pasteAfter(BookmarkId parentBookmarkId, BookmarkId bookmarkId, IProgressMonitor monitor)
			throws BookmarksException {
		Display display = PlatformUI.getWorkbench().getDisplay();
		String clipboardContents = getClipboardContentsFromUIThread(display);
		BookmarksTree bookmarksTree = getBookmarksTree(clipboardContents);
		if (bookmarksTree != null) {
			pasteAfter(parentBookmarkId, bookmarkId, bookmarksTree, monitor);
			return;
		}
		IStructuredSelection selection = getStructuredSelectionFromClipboardFromUIThread(display);
		if (!selection.isEmpty()) {
			pasteAfter(parentBookmarkId, bookmarkId, selection, monitor);
			return;
		}
	}

	private String getClipboardContentsFromUIThread(Display display) {
		String[] result = new String[1];
		display.syncExec(() -> result[0] = getClipboardContents(display));
		return result[0];
	}

	private String getClipboardContents(Display display) {
		Clipboard clipboard = new Clipboard(display);
		String textData;
		try {
			TextTransfer textTransfer = TextTransfer.getInstance();
			textData = (String) clipboard.getContents(textTransfer);
		} finally {
			clipboard.dispose();
		}
		return textData;
	}

	private IStructuredSelection getStructuredSelectionFromClipboardFromUIThread(Display display) {
		IStructuredSelection[] result = new IStructuredSelection[1];
		display.syncExec(() -> result[0] = getStructuredSelectionFromClipboard(display));
		return result[0];
	}

	private IStructuredSelection getStructuredSelectionFromClipboard(Display display) {
		Clipboard clipboard = new Clipboard(display);
		try {
			String text = (String)clipboard.getContents(URLTransfer.getInstance());
			if (text == null) {
				text = (String) clipboard.getContents(TextTransfer.getInstance());
			}
			if (text != null) {
				try {
					URL url = new URL(text);
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

	private void paste(BookmarkId parentBookmarkId, BookmarksTree sourceBookmarksTree, IProgressMonitor monitor)
			throws BookmarksException {
		SubMonitor.convert(monitor, "Pasting bookmarks", 100);
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			BookmarksCopier bookmarksCopier = new BookmarksCopier(sourceBookmarksTree,
					new NonExistingBookmarkIdProvider(bookmarksTreeModifier.getCurrentTree()));
			List<BookmarkId> bookmarkIds = sourceBookmarksTree.getChildren(sourceBookmarksTree.getRootFolder().getId())
					.stream().map(b -> b.getId()).collect(Collectors.toList());
			bookmarksCopier.copy(bookmarksTreeModifier, parentBookmarkId, bookmarkIds);
		});
	}

	private void pasteAfter(BookmarkId parentBookmarkId, BookmarkId bookmarkId, BookmarksTree sourceBookmarksTree, IProgressMonitor monitor)
			throws BookmarksException {
		SubMonitor.convert(monitor, "Pasting bookmarks", 100);
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			BookmarksCopier bookmarksCopier = new BookmarksCopier(sourceBookmarksTree,
					new NonExistingBookmarkIdProvider(bookmarksTreeModifier.getCurrentTree()));
			List<BookmarkId> bookmarkIds = sourceBookmarksTree.getChildren(sourceBookmarksTree.getRootFolder().getId())
					.stream().map(b -> b.getId()).collect(Collectors.toList());
			bookmarksCopier.copyAfter(bookmarksTreeModifier, parentBookmarkId, bookmarkId, bookmarkIds);
		});
	}	
	
	private void paste(BookmarkId parentBookmarkId, IStructuredSelection selection, IProgressMonitor monitor)
			throws BookmarksException {
		List<Bookmark> bookmarks = getBookmarks(selection, monitor);
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarks(parentBookmarkId, bookmarks);
		});
	}

	private void pasteAfter(BookmarkId parentBookmarkId, BookmarkId bookmarkId, IStructuredSelection selection,
			IProgressMonitor monitor) throws BookmarksException {
		List<Bookmark> bookmarks = getBookmarks(selection, monitor);
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarksAfter(parentBookmarkId, bookmarkId, bookmarks);
		});
	}

	private List<Bookmark> getBookmarks(IStructuredSelection selection, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Getting bookmarks", selection.size());
		List<Bookmark> bookmarks = Lists.newArrayList();
		for (Iterator<Object> it = selection.iterator(); it.hasNext();) {
			Object object = it.next();
			if (object instanceof Bookmark) {
				Bookmark bookmark = (Bookmark) object;
				bookmarks.add(bookmark);
				subMonitor.worked(1);
			} else {
				Bookmark bookmark = createBookmark(object, subMonitor.newChild(1));
				if (bookmark != null) {
					bookmarks.add(bookmark);
				}
			}
		}
		return bookmarks;
	}

	private Bookmark createBookmark(Object object, IProgressMonitor monitor) {
		Map<String, String> bookmarkProperties = new HashMap<String, String>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, null, new StructuredSelection(object),
				monitor);
		Bookmark bookmark = new Bookmark(new BookmarkId(), bookmarkProperties);
		return bookmark;
	}

}
