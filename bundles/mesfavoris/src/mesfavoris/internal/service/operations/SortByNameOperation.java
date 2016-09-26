package mesfavoris.internal.service.operations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.validation.IBookmarkModificationValidator;

public class SortByNameOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public SortByNameOperation(BookmarkDatabase bookmarkDatabase, IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}
	
	public void sortByName(BookmarkId bookmarkFolderId) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			IStatus status = bookmarkModificationValidator.validateModification(bookmarksTreeModifier.getCurrentTree(),
					bookmarkFolderId);
			if (!status.isOK()) {
				throw new BookmarksException(status);
			}
			List<Bookmark> children = new ArrayList<>(
					bookmarksTreeModifier.getCurrentTree().getChildren(bookmarkFolderId));
			List<BookmarkId> bookmarkIds = children.stream().sorted(new BookmarkComparator())
					.map(bookmark -> bookmark.getId()).collect(Collectors.toList());
			bookmarksTreeModifier.move(bookmarkIds, bookmarkFolderId);
		});

	}

	private class BookmarkComparator implements Comparator<Bookmark> {

		@Override
		public int compare(Bookmark b1, Bookmark b2) {
			String name1 = b1.getPropertyValue(Bookmark.PROPERTY_NAME);
			String name2 = b2.getPropertyValue(Bookmark.PROPERTY_NAME);
			if (b1 instanceof BookmarkFolder) {
				if (b2 instanceof BookmarkFolder) {
					return compareName(name1, name2);
				} else {
					return -1;
				}
			}
			if (b2 instanceof BookmarkFolder) {
				return 1;
			}
			return compareName(name1, name2);
		}

		private int compareName(String name1, String name2) {
			if (name1 == null) {
				if (name2 == null) {
					return 0;
				} else {
					return -1;
				}
			}
			if (name2 == null) {
				return 1;
			}
			return name1.compareToIgnoreCase(name2);
		}

	}

}
