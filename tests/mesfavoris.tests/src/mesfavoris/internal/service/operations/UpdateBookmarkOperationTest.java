package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class UpdateBookmarkOperationTest {
	private UpdateBookmarkOperation updateBookmarkOperation;
	private BookmarkDatabase bookmarkDatabase;
	private IBookmarkPropertiesProvider bookmarkPropertiesProvider = mock(IBookmarkPropertiesProvider.class);
	private Set<String> nonUpdatableProperties = Sets.newHashSet(Bookmark.PROPERTY_NAME, Bookmark.PROPERTY_COMMENT,
			Bookmark.PROPERTY_CREATED);

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		updateBookmarkOperation = new UpdateBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider,
				() -> nonUpdatableProperties);
	}

	@Test
	public void testUpdateDoesNotUpdateUserEditableProperties() throws Exception {
		// Given
		ISelection selection = mock(ISelection.class);
		IWorkbenchPart part = mock(IWorkbenchPart.class);
		doPutPropertiesWhenAddBookmarkPropertiesCalled(bookmarkPropertiesProvider, selection,
				ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark12 renamed", Bookmark.PROPERTY_COMMENT,
						"comment for bookmark12 modified", "customProperty", "custom value modified", "newProperty",
						"newCustomValue"));

		// When
		updateBookmarkOperation.updateBookmark(new BookmarkId("bookmark12"), part, selection,
				new NullProgressMonitor());

		// Then
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(new BookmarkId("bookmark12"));
		assertEquals("bookmark12", bookmark.getPropertyValue(Bookmark.PROPERTY_NAME));
		assertEquals("comment for bookmark12", bookmark.getPropertyValue(Bookmark.PROPERTY_COMMENT));
		assertEquals("custom value modified", bookmark.getPropertyValue("customProperty"));
		assertEquals("newCustomValue", bookmark.getPropertyValue("newProperty"));
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"),
				bookmark("bookmark12").withProperty(Bookmark.PROPERTY_COMMENT, "comment for bookmark12")
						.withProperty("customProperty", "custom value"));

		return bookmarksTreeBuilder.build();
	}

	private void doPutPropertiesWhenAddBookmarkPropertiesCalled(IBookmarkPropertiesProvider mock, ISelection selection,
			Map<String, String> propertiesToAdd) {
		Mockito.doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Map<String, String> bookmarkProperties = (Map<String, String>) args[0];
				bookmarkProperties.putAll(propertiesToAdd);
				return null;
			}
		}).when(mock).addBookmarkProperties(anyMap(), any(IWorkbenchPart.class), eq(selection),
				any(IProgressMonitor.class));

	}

}
