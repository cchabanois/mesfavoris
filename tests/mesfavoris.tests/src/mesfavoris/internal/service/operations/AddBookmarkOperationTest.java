package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableMap;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.service.operations.AddBookmarkOperation;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;
import mesfavoris.validation.IBookmarkModificationValidator;

public class AddBookmarkOperationTest {
	private AddBookmarkOperation operation;
	private BookmarkDatabase bookmarkDatabase;
	private IBookmarkPropertiesProvider bookmarkPropertiesProvider = mock(IBookmarkPropertiesProvider.class);
	private DefaultBookmarkFolderProvider defaultBookmarkFolderProvider = mock(DefaultBookmarkFolderProvider.class);
	private IBookmarkModificationValidator bookmarkModificationValidator = mock(IBookmarkModificationValidator.class);
	private IWorkbenchPart workbenchPart = mock(IWorkbenchPart.class);
	private IWorkbenchPage workbenchPage = mock(IWorkbenchPage.class);

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		operation = new AddBookmarkOperation(bookmarkDatabase, bookmarkPropertiesProvider,
				defaultBookmarkFolderProvider, bookmarkModificationValidator);
		IWorkbenchPartSite workbenchPartSite = mock(IWorkbenchPartSite.class);
		when(workbenchPart.getSite()).thenReturn(workbenchPartSite);
		when(workbenchPartSite.getPage()).thenReturn(workbenchPage);
		when(bookmarkModificationValidator.validateModification(any(BookmarksTree.class), any(BookmarkId.class)))
				.thenReturn(Status.OK_STATUS);
	}

	@Test
	public void testAddBookmark() throws Exception {
		// Given
		ISelection selection = mock(ISelection.class);
		when(defaultBookmarkFolderProvider.getDefaultBookmarkFolder(workbenchPage))
				.thenReturn(new BookmarkId("folder1"));
		doPutPropertiesWhenAddBookmarkPropertiesCalled(bookmarkPropertiesProvider, selection,
				ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark12", Bookmark.PROPERTY_COMMENT,
						"comment for bookmark12", "customProp", "customValue"));

		// When
		operation.addBookmark(workbenchPart, selection, new NullProgressMonitor());

		// Then
		Bookmark bookmark = getBookmarkWithName(new BookmarkId("folder1"), "bookmark12").get();
		assertEquals("bookmark12", bookmark.getPropertyValue(Bookmark.PROPERTY_NAME));
		assertEquals("comment for bookmark12", bookmark.getPropertyValue(Bookmark.PROPERTY_COMMENT));
		assertEquals("customValue", bookmark.getPropertyValue("customProp"));
	}

	private BookmarksTree createBookmarksTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("root");
		bookmarksTreeBuilder.addBookmarks("root", bookmarkFolder("folder1"), bookmarkFolder("folder2"));
		bookmarksTreeBuilder.addBookmarks("folder1", bookmarkFolder("folder11"), bookmark("bookmark11"));

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

	private Optional<Bookmark> getBookmarkWithName(BookmarkId parentId, String name) {
		return bookmarkDatabase.getBookmarksTree().getChildren(parentId).stream()
				.filter(bookmark -> name.equals(bookmark.getPropertyValue(Bookmark.PROPERTY_NAME))).findAny();
	}

}
