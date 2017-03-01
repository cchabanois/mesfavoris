package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmarkFolder;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.assertj.core.util.Lists;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.internal.placeholders.PathPlaceholdersMap;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblems;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;
import mesfavoris.tests.commons.waits.Waiter;

public class CheckBookmarkPropertiesOperationTest {
	private CheckBookmarkPropertiesOperation operation;
	private BookmarkDatabase bookmarkDatabase;
	private IBookmarkPropertiesProvider bookmarkPropertiesProvider = mock(IBookmarkPropertiesProvider.class);
	private IWorkbenchPart workbenchPart = mock(IWorkbenchPart.class);
	private IWorkbenchPage workbenchPage = mock(IWorkbenchPage.class);
	private ISelection selection = mock(ISelection.class);
	private IBookmarkProblems bookmarkProblems = mock(IBookmarkProblems.class);
	private PathPlaceholdersMap pathPlaceholders = new PathPlaceholdersMap();
	private Set<String> nonUpdatableProperties = Sets.newHashSet(Bookmark.PROPERTY_NAME, Bookmark.PROPERTY_COMMENT,
			Bookmark.PROPERTY_COMMENT);
	private Set<String> pathProperties = Sets.newHashSet("git.repositoryDir", "folderPath", "filePath");

	@Before
	public void setUp() {
		bookmarkDatabase = new BookmarkDatabase("test", createBookmarksTree());
		PathPlaceholderResolver pathPlaceholderResolver = new PathPlaceholderResolver(pathPlaceholders);
		operation = new CheckBookmarkPropertiesOperation(bookmarkDatabase, () -> nonUpdatableProperties,
				() -> pathProperties, bookmarkPropertiesProvider, pathPlaceholderResolver, bookmarkProblems);
		IWorkbenchPartSite workbenchPartSite = mock(IWorkbenchPartSite.class);
		when(workbenchPart.getSite()).thenReturn(workbenchPartSite);
		when(workbenchPartSite.getPage()).thenReturn(workbenchPage);
	}

	@Test
	public void testPropertiesNeedingUpdate() throws Exception {
		// Given
		BookmarkId bookmarkId = new BookmarkId("bookmark12");
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarks(new BookmarkId("folder1"), Lists.newArrayList(new Bookmark(bookmarkId,
					ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark12", "prop1", "value1", "prop2", "value2"))));
		});
		doPutPropertiesWhenAddBookmarkPropertiesCalled(bookmarkPropertiesProvider, selection,
				ImmutableMap.of("prop1", "value1", "prop2", "newValue2", "prop3", "value3"));

		// When
		Set<BookmarkProblem> newBookmarkProblems = operation.getBookmarkPropertiesProblems(bookmarkId, workbenchPart,
				selection, new NullProgressMonitor());

		// Then
		assertThat(newBookmarkProblems).hasSize(1);
		BookmarkProblem bookmarkProblem = newBookmarkProblems.iterator().next();
		assertThat(bookmarkProblem.getProblemType()).isEqualTo(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		assertThat(bookmarkProblem.getProperties()).containsExactly(entry("prop2", "newValue2"),
				entry("prop3", "value3"));
	}

	@Test
	public void testPropertiesUsingUndefinedPlaceholder() throws BookmarksException {
		// Given
		pathPlaceholders.add(new PathPlaceholder("PLACEHOLDER2", new Path("/var/workspace/project2")));
		BookmarkId bookmarkId = new BookmarkId("bookmark12");
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarks(new BookmarkId("folder1"),
					Lists.newArrayList(new Bookmark(bookmarkId, ImmutableMap.of("filePath",
							"${PLACEHOLDER1}/myFile.txt", "folderPath", "${PLACEHOLDER2}/myFolder"))));
		});
		whenAddBookmarkPropertiesThenPutAll(ImmutableMap.of("filePath", "/var/workspace/project1/myFile.txt"));

		// When
		Set<BookmarkProblem> newBookmarkProblems = operation.getBookmarkPropertiesProblems(bookmarkId, workbenchPart,
				selection, new NullProgressMonitor());

		// Then
		assertThat(newBookmarkProblems).hasSize(1);
		BookmarkProblem bookmarkProblem = newBookmarkProblems.iterator().next();
		assertThat(bookmarkProblem.getProblemType()).isEqualTo(BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED);
		assertThat(bookmarkProblem.getProperties()).containsOnly(entry("filePath", "${PLACEHOLDER1}/myFile.txt"),
				entry("${PLACEHOLDER1}", "/var/workspace/project1"));

	}

	private void whenAddBookmarkPropertiesThenPutAll(Map<String, String> properties) {
		doAnswer(invocation -> {
			Map<String, String> map = invocation.getArgumentAt(0, Map.class);
			map.putAll(properties);
			return null;
		}).when(bookmarkPropertiesProvider).addBookmarkProperties(any(Map.class), eq(workbenchPart), eq(selection),
				any(IProgressMonitor.class));
	}

	@Test
	public void testCheckBookmarkPropertiesProblems() throws Exception {
		// Given
		BookmarkId bookmarkId = new BookmarkId("bookmark12");
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			bookmarksTreeModifier.addBookmarks(new BookmarkId("folder1"),
					Lists.newArrayList(new Bookmark(bookmarkId, ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark12",
							Bookmark.PROPERTY_COMMENT, "comment", "prop1", "value1", "prop2", "value2"))));
		});
		doPutPropertiesWhenAddBookmarkPropertiesCalled(bookmarkPropertiesProvider, selection, ImmutableMap.of(
				Bookmark.PROPERTY_COMMENT, "new comment", "prop1", "value1", "prop2", "newValue2", "prop3", "value3"));

		// When
		operation.checkBookmarkPropertiesProblems(bookmarkId, workbenchPart, selection);

		// Then
		BookmarkProblem bookmarkProblem = waitUntilBookmarkProblemAdded();
		assertThat(bookmarkProblem.getBookmarkId()).isEqualTo(bookmarkId);
		assertThat(bookmarkProblem.getProblemType()).isEqualTo(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		assertThat(bookmarkProblem.getProperties()).containsExactly(entry("prop2", "newValue2"),
				entry("prop3", "value3"));
	}

	private BookmarkProblem waitUntilBookmarkProblemAdded() throws TimeoutException {
		ArgumentCaptor<BookmarkProblem> captor = ArgumentCaptor.forClass(BookmarkProblem.class);
		Waiter.waitUntil("No bookmark problem added", () -> {
			verify(bookmarkProblems).add(captor.capture());
			return true;
		});
		return captor.getValue();
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

}
