package mesfavoris.internal.views.dnd;

import static mesfavoris.tests.commons.bookmarks.BookmarksTreeTestUtil.getBookmarkFolder;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.views.dnd.BookmarksViewerDropListener;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksMovedModification;
import mesfavoris.tests.commons.bookmarks.BookmarksListener;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeGenerator;
import mesfavoris.tests.commons.bookmarks.IncrementalIDGenerator;
import mesfavoris.validation.IBookmarkModificationValidator;

public class BookmarksViewerDropListenerTest {
	private BookmarkDatabase bookmarkDatabase;
	private IBookmarkModificationValidator bookmarkModificationValidator = mock(IBookmarkModificationValidator.class);
	private Viewer viewer = mock(Viewer.class);
	private IBookmarkPropertiesProvider bookmarkPropertiesProvider = mock(IBookmarkPropertiesProvider.class);
	private BookmarksListener bookmarksListener = new BookmarksListener();

	@Before
	public void setUp() {
		BookmarksTree bookmarksTree = new BookmarksTreeGenerator(new IncrementalIDGenerator(), 5, 3, 2).build();
		bookmarkDatabase = new BookmarkDatabase("main", bookmarksTree);
		bookmarkDatabase.addListener(bookmarksListener);
		when(bookmarkModificationValidator.validateModification(any(BookmarksTree.class), any(BookmarkId.class)))
				.thenReturn(Status.OK_STATUS);
	}

	@Test
	@Ignore
	public void testDropOn() {
		// Given
		BookmarkFolder bookmarkFolderToMoved = getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 1, 1, 1);
		IStructuredSelection data = new StructuredSelection(bookmarkFolderToMoved);
		BookmarkFolder currentTarget = getBookmarkFolder(bookmarkDatabase.getBookmarksTree(), 1, 1);
		BookmarksViewerDropListener listener = getBookmarksViewerDropListener(ViewerDropAdapter.LOCATION_ON,
				currentTarget);

		// When
		DropTargetEvent dropTargetEvent = mock(DropTargetEvent.class);
		listener.dragEnter(dropTargetEvent);
		listener.dragOver(dropTargetEvent);
		listener.dropAccept(dropTargetEvent);
		listener.drop(dropTargetEvent);
//		assertTrue(listener.validateDrop(currentTarget, ViewerDropAdapter.LOCATION_ON, /* transferType */ null));
//		boolean result = listener.performDrop(data);

		// Then
//		assertTrue(result);
		assertEquals(1, bookmarksListener.getModifications().size());
		assertIsBookmarksMovedModification(bookmarksListener.getModifications().get(0), currentTarget.getId(),
				Lists.newArrayList(bookmarkFolderToMoved.getId()));
	}

	private void assertIsBookmarksMovedModification(BookmarksModification bookmarksModification, BookmarkId newParentId,
			List<BookmarkId> bookmarkIds) {
		assertThat(bookmarksListener.getModifications().get(0), instanceOf(BookmarksMovedModification.class));
		BookmarksMovedModification bookmarksMovedModification = (BookmarksMovedModification) bookmarksModification;
		assertEquals(newParentId, bookmarksMovedModification.getNewParentId());
		assertEquals(bookmarkIds, bookmarksMovedModification.getBookmarkIds());
	}

	private BookmarksViewerDropListener getBookmarksViewerDropListener(final int currentLocation,
			final Object currentTarget) {
		return new BookmarksViewerDropListener(viewer, bookmarkDatabase, bookmarkModificationValidator,
				bookmarkPropertiesProvider) {
			@Override
			protected int getCurrentLocation() {
				return currentLocation;
			}

			@Override
			protected Object getCurrentTarget() {
				return currentTarget;
			};

		};
	}

}
