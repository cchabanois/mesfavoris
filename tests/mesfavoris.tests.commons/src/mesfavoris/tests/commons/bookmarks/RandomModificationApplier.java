package mesfavoris.tests.commons.bookmarks;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksTreeModifier;

public class RandomModificationApplier {
	private final Random random = new Random();
	private final IDGenerator idGenerator;
	private final Predicate<Bookmark> bookmarkFilter;

	public RandomModificationApplier(IDGenerator idGenerator) {
		this.idGenerator = idGenerator;
		bookmarkFilter = bookmark -> true;
	}

	public RandomModificationApplier(IDGenerator idGenerator, Predicate<Bookmark> bookmarkFilter) {
		this.idGenerator = idGenerator;
		this.bookmarkFilter = bookmarkFilter;
	}

	public BookmarksTree applyRandomModification(BookmarksTree bookmarksTree, PrintWriter printWriter) {
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(bookmarksTree);
		applyRandomModification(bookmarksTreeModifier, printWriter);
		return bookmarksTreeModifier.getCurrentTree();
	}

	public void applyRandomModification(BookmarksTreeModifier bookmarksTreeModifier, PrintWriter printWriter) {
		do {
			try {
				IRandomModification randomModification = getRandomModification();
				randomModification.run(bookmarksTreeModifier, printWriter);
				return;
			} catch (IllegalArgumentException e) {
			}
		} while (true);
	}

	private IRandomModification getRandomModification() {
		List<IRandomModification> possibleModifications = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			possibleModifications.addAll(Arrays.asList(getRandomSetProperty(),
					getRandomAddNonFolderBookmarkAfterModification(), getRandomAddNonFolderBookmarkAfterModification(),
					getRandomAddNonFolderBookmarkBeforeModification(),
					getRandomAddNonFolderBookmarkBeforeModification(), getRandomAddFolderBookmarkAfterModification(),
					getRandomAddFolderBookmarkBeforeModification(), getRandomDeleteModification(),
					getRandomMoveAfterModification(), getRandomMoveBeforeModification(),
					getRandomSameFolderMoveAfterModification(), getRandomSameFolderMoveBeforeModification()));
		}
		possibleModifications.addAll(Arrays.asList(getRandomRecursiveDeleteModification()));
		int index = random.nextInt(possibleModifications.size());
		return possibleModifications.get(index);
	}

	private Bookmark randomNonFolderBookmark(BookmarksTree bookmarksTree) {
		List<BookmarkId> nonFolderBookmarks = Lists.newArrayList();
		for (Bookmark bookmark : bookmarksTree) {
			if (!(bookmark instanceof BookmarkFolder)) {
				if (bookmarkFilter.test(bookmark)) {
					nonFolderBookmarks.add(bookmark.getId());
				}
			}
		}
		int index = random.nextInt(nonFolderBookmarks.size());
		return bookmarksTree.getBookmark(nonFolderBookmarks.get(index));
	}

	private Bookmark randomBookmark(BookmarksTree bookmarksTree, boolean canBeRootFolder) {
		List<BookmarkId> allBookmarks = Lists.newArrayList();
		for (Bookmark bookmark : bookmarksTree) {
			if (!canBeRootFolder && bookmarksTree.getRootFolder().equals(bookmark)) {
				// root folder not accepted
			} else {
				if (bookmarkFilter.test(bookmark)) {
					allBookmarks.add(bookmark.getId());
				}
			}
		}
		int index = random.nextInt(allBookmarks.size());
		return bookmarksTree.getBookmark(allBookmarks.get(index));
	}

	private BookmarkFolder randomFolderBookmark(BookmarksTree bookmarksTree, boolean canBeRootFolder,
			boolean canBeEmpty) {
		List<BookmarkId> folderBookmarks = Lists.newArrayList();
		for (Bookmark bookmark : bookmarksTree) {
			if (bookmark instanceof BookmarkFolder) {
				if (!canBeRootFolder && bookmarksTree.getRootFolder().equals(bookmark)) {
					// root folder not accepted
				} else if (!canBeEmpty && bookmarksTree.getChildren(bookmark.getId()).isEmpty()) {
					// cannot be empty
				} else {
					if (bookmarkFilter.test(bookmark)) {
						folderBookmarks.add(bookmark.getId());
					}
				}
			}
		}
		if (folderBookmarks.isEmpty()) {
			return null;
		}
		int index = random.nextInt(folderBookmarks.size());
		return (BookmarkFolder) bookmarksTree.getBookmark(folderBookmarks.get(index));
	}

	private Bookmark randomChild(BookmarksTree bookmarksTree, BookmarkId parentId) {
		List<Bookmark> children = bookmarksTree.getChildren(parentId);
		if (children.size() == 0) {
			return null;
		}
		return children.get(random.nextInt(children.size()));
	}

	private IRandomModification getRandomSetProperty() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			Bookmark bookmark = randomBookmark(bookmarksTree, false);
			printWriter.println("Setting bookmark property for " + bookmark.getId());
			String propertyName = "property" + random.nextInt(5);
			String propertyValue = "value" + random.nextInt(5);
			bookmarksTreeModifier.setPropertyValue(bookmark.getId(), propertyName, propertyValue);
		};
	}

	private IRandomModification getRandomDeleteModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			Bookmark bookmark = randomNonFolderBookmark(bookmarksTree);
			printWriter.println("Deleting bookmark " + bookmark.getId());
			bookmarksTreeModifier.deleteBookmark(bookmark.getId(), false);
		};
	}

	private IRandomModification getRandomRecursiveDeleteModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder bookmarkFolder = randomFolderBookmark(bookmarksTree, false, true);
			if (bookmarkFolder == null) {
				throw new IllegalArgumentException();
			}
			printWriter.println("Deleting bookmark folder recursively " + bookmarkFolder.getId());
			bookmarksTreeModifier.deleteBookmark(bookmarkFolder.getId(), true);
		};
	}

	private IRandomModification getRandomAddNonFolderBookmarkAfterModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder parent = randomFolderBookmark(bookmarksTree, true, true);
			Bookmark existingBookmark = randomChild(bookmarksTree, parent.getId());
			BookmarkId id = idGenerator.newId();
			Bookmark newBookmark = new Bookmark(id);
			printWriter.println("Adding bookmark " + newBookmark.getId() + " after");
			bookmarksTreeModifier.addBookmarksAfter(parent.getId(),
					existingBookmark == null ? null : existingBookmark.getId(), Lists.newArrayList(newBookmark));
		};
	}

	private IRandomModification getRandomAddNonFolderBookmarkBeforeModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder parent = randomFolderBookmark(bookmarksTree, true, true);
			Bookmark existingBookmark = randomChild(bookmarksTree, parent.getId());
			BookmarkId id = idGenerator.newId();
			Bookmark newBookmark = new Bookmark(id);
			printWriter.println("Adding bookmark " + newBookmark.getId() + " before");
			bookmarksTreeModifier.addBookmarksBefore(parent.getId(),
					existingBookmark == null ? null : existingBookmark.getId(), Lists.newArrayList(newBookmark));
		};
	}

	private IRandomModification getRandomAddFolderBookmarkAfterModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder parent = randomFolderBookmark(bookmarksTree, true, true);
			Bookmark existingBookmark = randomChild(bookmarksTree, parent.getId());
			BookmarkId id = idGenerator.newId();
			BookmarkFolder newBookmark = new BookmarkFolder(id, "addedFolder" + id);
			printWriter.println("Adding bookmark folder " + newBookmark.getId() + " after");
			bookmarksTreeModifier.addBookmarksAfter(parent.getId(),
					existingBookmark == null ? null : existingBookmark.getId(), Lists.newArrayList(newBookmark));
		};
	}

	private IRandomModification getRandomAddFolderBookmarkBeforeModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder parent = randomFolderBookmark(bookmarksTree, true, true);
			Bookmark existingBookmark = randomChild(bookmarksTree, parent.getId());
			BookmarkId id = idGenerator.newId();
			BookmarkFolder newBookmark = new BookmarkFolder(id, "addedFolder" + id);
			printWriter.println("Adding bookmark folder " + newBookmark.getId() + " before");
			bookmarksTreeModifier.addBookmarksBefore(parent.getId(),
					existingBookmark == null ? null : existingBookmark.getId(), Lists.newArrayList(newBookmark));
		};
	}

	private IRandomModification getRandomMoveAfterModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder parent = randomFolderBookmark(bookmarksTree, true, true);
			Bookmark movedBookmark = randomBookmark(bookmarksTree, false);
			Bookmark existingBookmark = randomChild(bookmarksTree, parent.getId());
			printWriter.println("Moving bookmark " + movedBookmark.getId() + " to folder " + parent.getId() + " after "
					+ (existingBookmark == null ? "null" : existingBookmark.getId()));
			bookmarksTreeModifier.moveAfter(Lists.newArrayList(movedBookmark.getId()), parent.getId(),
					existingBookmark == null ? null : existingBookmark.getId());
		};
	}

	private IRandomModification getRandomMoveBeforeModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder parent = randomFolderBookmark(bookmarksTree, true, true);
			Bookmark movedBookmark = randomBookmark(bookmarksTree, false);
			Bookmark existingBookmark = randomChild(bookmarksTree, parent.getId());
			printWriter.println("Moving bookmark " + movedBookmark.getId() + " to folder " + parent.getId() + " before "
					+ (existingBookmark == null ? "null" : existingBookmark.getId()));
			bookmarksTreeModifier.moveBefore(Lists.newArrayList(movedBookmark.getId()), parent.getId(),
					existingBookmark == null ? null : existingBookmark.getId());
		};
	}

	private IRandomModification getRandomSameFolderMoveAfterModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder parent;
			Bookmark movedBookmark;
			parent = randomFolderBookmark(bookmarksTree, true, false);
			if (parent == null) {
				// no parent with child ...
				throw new IllegalArgumentException();
			}
			movedBookmark = randomChild(bookmarksTree, parent.getId());
			Bookmark existingBookmark = randomChild(bookmarksTree, parent.getId());
			printWriter.println("Moving bookmark " + movedBookmark.getId() + " to same folder " + parent.getId()
					+ " after " + (existingBookmark == null ? "null" : existingBookmark.getId()));
			bookmarksTreeModifier.moveAfter(Lists.newArrayList(movedBookmark.getId()), parent.getId(),
					existingBookmark == null ? null : existingBookmark.getId());
		};
	}

	private IRandomModification getRandomSameFolderMoveBeforeModification() {
		return (bookmarksTreeModifier, printWriter) -> {
			BookmarksTree bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			BookmarkFolder parent;
			Bookmark movedBookmark;
			parent = randomFolderBookmark(bookmarksTree, true, false);
			if (parent == null) {
				// no parent with child ...
				throw new IllegalArgumentException();
			}
			movedBookmark = randomChild(bookmarksTree, parent.getId());
			Bookmark existingBookmark = randomChild(bookmarksTreeModifier.getCurrentTree(), parent.getId());
			printWriter.println("Moving bookmark " + movedBookmark.getId() + " to same folder before "
					+ (existingBookmark == null ? "null" : existingBookmark.getId()));
			bookmarksTreeModifier.moveBefore(Lists.newArrayList(movedBookmark.getId()), parent.getId(),
					existingBookmark == null ? null : existingBookmark.getId());
		};
	}

	private static interface IRandomModification {

		public void run(BookmarksTreeModifier bookmarksTreeModifier, PrintWriter printWriter);
	}

}
