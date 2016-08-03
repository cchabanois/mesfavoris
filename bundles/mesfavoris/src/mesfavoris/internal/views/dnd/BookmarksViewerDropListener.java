package mesfavoris.internal.views.dnd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.validation.IBookmarkModificationValidator;

public class BookmarksViewerDropListener extends ViewerDropAdapter {

	private final Viewer viewer;
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;

	public BookmarksViewerDropListener(Viewer viewer, BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider) {
		super(viewer);
		this.viewer = viewer;
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
	}

	@Override
	public boolean performDrop(Object data) {
		int location = getCurrentLocation();
		Bookmark target = (Bookmark) getCurrentTarget();
		if (target == null) {
			return false;
		}
		IStructuredSelection selection = getStructuredSelection(data, getCurrentEvent().currentDataType);
		List<Bookmark> bookmarks = getBookmarks(selection);
		boolean result = false;
		switch (location) {
		case LOCATION_BEFORE:
			result = addBefore(target, bookmarks);
			break;
		case LOCATION_AFTER:
			result = addAfter(target, bookmarks);
			break;
		case LOCATION_ON:
			result = addOn(target, bookmarks);
			break;
		case LOCATION_NONE:
			return false;
		default:
			return false;
		}
		viewer.refresh();
		return result;
	}

	private boolean addOn(final Bookmark target, final List<Bookmark> bookmarks) {
		if (!(target instanceof BookmarkFolder)) {
			return false;
		}
		try {
			bookmarkDatabase.modify(bookmarksTreeModifier -> {
				List<Bookmark> existingBookmarks = Lists.newArrayList();
				List<Bookmark> nonExistingBookmarks = Lists.newArrayList();
				sortBookmarks(bookmarks, bookmarksTreeModifier.getCurrentTree(), existingBookmarks,
						nonExistingBookmarks);
				bookmarksTreeModifier.move(existingBookmarks.stream().map(Bookmark::getId).collect(Collectors.toList()),
						target.getId());
				bookmarksTreeModifier.addBookmarks(target.getId(), nonExistingBookmarks);
			});
			return true;
		} catch (BookmarksException e) {
			return false;
		}
	}

	private void sortBookmarks(List<Bookmark> bookmarks, BookmarksTree bookmarksTree, List<Bookmark> existingBookmarks,
			List<Bookmark> nonExistingBookmarks) {
		for (Bookmark bookmark : bookmarks) {
			if (bookmarksTree.getBookmark(bookmark.getId()) != null) {
				existingBookmarks.add(bookmark);
			} else {
				nonExistingBookmarks.add(bookmark);
			}
		}
	}

	private boolean addAfter(final Bookmark target, final List<Bookmark> bookmarks) {
		try {
			bookmarkDatabase.modify(bookmarksTreeModifier -> {
				BookmarkFolder parentFolder = bookmarksTreeModifier.getCurrentTree().getParentBookmark(target.getId());
				if (parentFolder == null) {
					return;
				}
				List<Bookmark> existingBookmarks = Lists.newArrayList();
				List<Bookmark> nonExistingBookmarks = Lists.newArrayList();
				sortBookmarks(bookmarks, bookmarksTreeModifier.getCurrentTree(), existingBookmarks,
						nonExistingBookmarks);
				bookmarksTreeModifier.moveAfter(
						existingBookmarks.stream().map(Bookmark::getId).collect(Collectors.toList()),
						parentFolder.getId(), target.getId());
				bookmarksTreeModifier.addBookmarksAfter(parentFolder.getId(), target.getId(), nonExistingBookmarks);

			});
			return true;
		} catch (BookmarksException e) {
			return false;
		}
	}

	private boolean addBefore(final Bookmark target, final List<Bookmark> bookmarks) {
		try {
			bookmarkDatabase.modify(bookmarksTreeModifier -> {
				BookmarkFolder parentFolder = bookmarksTreeModifier.getCurrentTree().getParentBookmark(target.getId());
				if (parentFolder == null) {
					return;
				}
				List<Bookmark> existingBookmarks = Lists.newArrayList();
				List<Bookmark> nonExistingBookmarks = Lists.newArrayList();
				sortBookmarks(bookmarks, bookmarksTreeModifier.getCurrentTree(), existingBookmarks,
						nonExistingBookmarks);
				bookmarksTreeModifier.moveBefore(
						existingBookmarks.stream().map(Bookmark::getId).collect(Collectors.toList()),
						parentFolder.getId(), target.getId());
				bookmarksTreeModifier.addBookmarksBefore(parentFolder.getId(), target.getId(), nonExistingBookmarks);

			});
			return true;
		} catch (BookmarksException e) {
			return false;
		}
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

	private IStructuredSelection getStructuredSelection(Object data, TransferData currentDataType) {
		if (LocalSelectionTransfer.getTransfer().isSupportedType(currentDataType)) {
			return (IStructuredSelection) data;
		}
		if (URLTransfer.getInstance().isSupportedType(currentDataType)) {
			try {
				return new StructuredSelection(new URL((String) data));
			} catch (MalformedURLException e) {
				// ignore
			}
		}
		if (FileTransfer.getInstance().isSupportedType(currentDataType)) {
			return new StructuredSelection(Arrays.stream((String[]) data).map(Path::new).collect(Collectors.toList()));
		}
		return new StructuredSelection();
	}

	private Bookmark createBookmark(Object object) {
		Map<String, String> bookmarkProperties = new HashMap<String, String>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, null, new StructuredSelection(object));
		Bookmark bookmark = new Bookmark(new BookmarkId(), bookmarkProperties);
		return bookmark;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		int location = getCurrentLocation();
		if (!(target instanceof Bookmark)) {
			return false;
		}
		Bookmark targetBookmark = (Bookmark) target;
		BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
		switch (location) {
		case LOCATION_BEFORE:
		case LOCATION_AFTER:
			BookmarkFolder parent = bookmarksTree.getParentBookmark(targetBookmark.getId());
			return parent != null
					&& bookmarkModificationValidator.validateModification(bookmarksTree, parent.getId()).isOK();
		case LOCATION_ON:
			if (!(target instanceof BookmarkFolder)) {
				return false;
			}
			BookmarkFolder bookmarkFolder = (BookmarkFolder) target;
			return bookmarkModificationValidator.validateModification(bookmarksTree, bookmarkFolder.getId()).isOK();
		case LOCATION_NONE:
			return false;
		default:
			return false;
		}
	}

}