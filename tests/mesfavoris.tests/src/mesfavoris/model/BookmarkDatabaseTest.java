package mesfavoris.model;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class BookmarkDatabaseTest {
	private BookmarkDatabase bookmarkDatabase;
	private IBookmarksListener bookmarksListener = mock(IBookmarksListener.class);

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		bookmarkDatabase.addListener(bookmarksListener);
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"));

		return bookmarksTreeBuilder.build();
	}

	@Test
	public void testModify() throws Exception {
		// Given
		BookmarkId bookmarkId = new BookmarkId("folder1");
		BookmarksTree bookmarksTreeBefore = bookmarkDatabase.getBookmarksTree();

		// When
		bookmarkDatabase.modify((bookmarksTreeModifier -> {
			bookmarksTreeModifier.setPropertyValue(bookmarkId, Bookmark.PROPERTY_NAME, "folder1 renamed");
		}));

		// Then
		assertEquals("folder1 renamed",
				bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId).getPropertyValue(Bookmark.PROPERTY_NAME));
		verify(bookmarksListener)
				.bookmarksModified(Lists.newArrayList(new BookmarkPropertiesModification(bookmarksTreeBefore,
						bookmarkDatabase.getBookmarksTree(), bookmarkId)));
	}

	@Test
	public void testNestedModification() throws Exception {
		// Given

		// When
		Throwable thrown = catchThrowable(() -> {
			bookmarkDatabase.modify((bookmarksTreeModifier -> {
				createFolder(new BookmarkId("root"), new BookmarkId("folder3"), "folder3");
				createBookmark(new BookmarkId("folder3"), new BookmarkId("bookmark31"), "bookmark31");

			}));
		});

		// Then
		assertThat(thrown).isInstanceOf(BookmarksException.class)
				.hasMessageContaining("BookmarksDatabase.modify is not reentrant");
	}

	private void createFolder(BookmarkId parentId, BookmarkId bookmarkId, String name) throws BookmarksException {
		bookmarkDatabase.modify((bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarks(parentId, Lists.newArrayList(new BookmarkFolder(bookmarkId, name)));
		}));
	}

	private void createBookmark(BookmarkId parentId, BookmarkId bookmarkId, String name) throws BookmarksException {
		bookmarkDatabase.modify((bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarks(parentId,
					Lists.newArrayList(new Bookmark(bookmarkId, ImmutableMap.of(Bookmark.PROPERTY_NAME, name))));
		}));
	}

}
