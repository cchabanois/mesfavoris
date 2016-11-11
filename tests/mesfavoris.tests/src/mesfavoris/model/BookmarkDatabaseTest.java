package mesfavoris.model;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class BookmarkDatabaseTest {
	private BookmarkDatabase bookmarkDatabase;
	private IBookmarksListener bookmarksListener = mock(IBookmarksListener.class);
	private ExecutorService executorService;

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		bookmarkDatabase.addListener(bookmarksListener);
		executorService = Executors.newCachedThreadPool();
	}

	@After
	public void tearDown() {
		executorService.shutdown();
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

	@Test
	public void testModifyWithPessimisticLocking() throws Exception {
		// Given
		int bookmarksCount = bookmarkDatabase.getBookmarksTree().size();

		// When
		Callable<Void> callable = () -> {
			for (int j = 0; j < 100; j++) {
				try {
					bookmarkDatabase.modify(LockMode.PESSIMISTIC, (bookmarksTreeModifier) -> {
						bookmarksTreeModifier.addBookmarks(new BookmarkId("root"),
								Lists.newArrayList(new Bookmark(new BookmarkId())));
					});
					Thread.sleep(10);
				} catch (BookmarksException | InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			return null;
		};
		List<Callable<Void>> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(callable);
		}
		executorService.invokeAll(list);

		// Then
		assertEquals(bookmarksCount + 1000, bookmarkDatabase.getBookmarksTree().size());
	}

	@Test
	public void testModifyWithOptimisticLocking() throws Exception {
		// Given
		int bookmarksCount = bookmarkDatabase.getBookmarksTree().size();

		// When
		Callable<Void> callable = () -> {
			for (int j = 0; j < 100; j++) {
				try {
					Bookmark bookmark = new Bookmark(new BookmarkId());
					while (true) {
						try {
							bookmarkDatabase.modify(LockMode.OPTIMISTIC, (bookmarksTreeModifier) -> {
								bookmarksTreeModifier.addBookmarks(new BookmarkId("root"),
										Lists.newArrayList(bookmark));
							});
							break;
						} catch (OptimisticLockException e) {
							Thread.sleep(10);
						}
					}
					Thread.sleep(10);
				} catch (BookmarksException | InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			return null;
		};
		List<Callable<Void>> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(callable);
		}
		executorService.invokeAll(list);

		// Then
		assertEquals(bookmarksCount + 1000, bookmarkDatabase.getBookmarksTree().size());
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
