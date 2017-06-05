package mesfavoris.internal.problems;

import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.internal.bookmarktypes.extension.BookmarkPropertyDescriptors;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.internal.placeholders.PathPlaceholdersStore;
import mesfavoris.internal.problems.extension.BookmarkProblemDescriptors;
import mesfavoris.internal.remote.InMemoryRemoteBookmarksStore;
import mesfavoris.internal.service.operations.CheckBookmarkPropertiesOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;
import mesfavoris.tests.commons.waits.Waiter;

public class BookmarkProblemsAutoUpdaterTest {
	private BookmarkProblemsDatabase bookmarkProblemsDatabase;
	private BookmarkDatabase bookmarkDatabase;
	private BookmarkProblemsAutoUpdater bookmarkProblemsAutoUpdater;
	private Set<String> pathProperties = Sets.newHashSet("git.repositoryDir", "folderPath", "filePath");
	private PathPlaceholdersStore pathPlaceholdersStore;
	private IBookmarkPropertiesProvider bookmarkPropertiesProvider = mock(IBookmarkPropertiesProvider.class);
	private CheckBookmarkPropertiesOperation checkBookmarkPropertiesOperation;
	private InMemoryRemoteBookmarksStore remoteBookmarksStore;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		bookmarkDatabase = new BookmarkDatabase("main", getInitialTree());
		bookmarkProblemsDatabase = new BookmarkProblemsDatabase(eventBroker, bookmarkDatabase,
				new BookmarkProblemDescriptors(), temporaryFolder.newFile());
		bookmarkProblemsDatabase.init();
		pathPlaceholdersStore = new PathPlaceholdersStore(eventBroker, temporaryFolder.newFile());
		pathPlaceholdersStore.init();
		PathPlaceholderResolver pathPlaceholderResolver = new PathPlaceholderResolver(pathPlaceholdersStore);
		this.remoteBookmarksStore = new InMemoryRemoteBookmarksStore(eventBroker);
		RemoteBookmarksStoreManager remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(
				() -> Lists.newArrayList(remoteBookmarksStore));
		BookmarkPropertyDescriptors propertyDescriptors = new BookmarkPropertyDescriptors();
		checkBookmarkPropertiesOperation = new CheckBookmarkPropertiesOperation(bookmarkDatabase,
				remoteBookmarksStoreManager, propertyDescriptors, bookmarkPropertiesProvider, pathPlaceholderResolver,
				bookmarkProblemsDatabase);
		bookmarkProblemsAutoUpdater = new BookmarkProblemsAutoUpdater(eventBroker, bookmarkDatabase,
				bookmarkProblemsDatabase, () -> pathProperties, checkBookmarkPropertiesOperation);
		bookmarkProblemsAutoUpdater.init();
	}

	@After
	public void tearDown() throws Exception {
		bookmarkProblemsAutoUpdater.close();
		bookmarkProblemsDatabase.close();
		pathPlaceholdersStore.close();
	}

	@Test
	public void testBookmarkWithUndefinedPlaceholderAdded() throws Exception {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		Bookmark bookmark = new Bookmark(bookmarkId, ImmutableMap.of(Bookmark.PROPERTY_NAME,
				"bookmark with placeholder", "filePath", "${PROJECT}/file.txt"));

		// When
		addBookmark(new BookmarkId("rootFolder"), bookmark);

		// Then
		Waiter.waitUntil("Not placeholder bookmark problem", () -> {
			assertThat(bookmarkProblemsDatabase.getBookmarkProblems(bookmarkId))
					.hasOnlyOneElementSatisfying(bookmarkProblem -> bookmarkProblem.getProblemType()
							.equals(BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED));
			return true;
		});
	}

	@Test
	public void testBookmarkProblemRemovedWhenPlaceholderDefined() throws Exception {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		Bookmark bookmark = new Bookmark(bookmarkId, ImmutableMap.of(Bookmark.PROPERTY_NAME,
				"bookmark with placeholder", "filePath", "${PROJECT}/file.txt"));
		addBookmark(new BookmarkId("rootFolder"), bookmark);
		Waiter.waitUntil("Not placeholder bookmark problem", () -> {
			return bookmarkProblemsDatabase.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED)
					.isPresent();
		});

		// When
		pathPlaceholdersStore.add(new PathPlaceholder("PROJECT", new Path("/home/cedric/myProject")));

		// Then
		Waiter.waitUntil("Placeholder bookmark problem still present", () -> {
			return !bookmarkProblemsDatabase.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED)
					.isPresent();
		});
	}

	@Test
	public void testBookmarkProblemUpdatedWhenPropertyModified() throws BookmarksException {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		Bookmark bookmark = new Bookmark(bookmarkId, ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark", "prop1",
				"value1", "prop2", "value2", "prop3", "value3"));
		addBookmark(new BookmarkId("rootFolder"), bookmark);
		bookmarkProblemsDatabase.add(new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				ImmutableMap.of("prop1", "newValue1", "prop2", "newValue2")));

		// When
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarksTreeModifier.setPropertyValue(bookmarkId, "prop1", "newValue1"));

		// Then
		BookmarkProblem bookmarkProblem = bookmarkProblemsDatabase
				.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE).get();
		assertThat(bookmarkProblem.getProperties()).containsOnly(entry("prop2", "newValue2"));
	}

	@Test
	public void testBookmarkProblemUpdatedWhenPropertyAdded() throws BookmarksException {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		Bookmark bookmark = new Bookmark(bookmarkId, ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark"));
		addBookmark(new BookmarkId("rootFolder"), bookmark);
		bookmarkProblemsDatabase.add(new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				ImmutableMap.of("prop1", "newValue1", "prop2", "newValue2")));

		// When
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarksTreeModifier.setPropertyValue(bookmarkId, "prop1", "newValue1"));

		// Then
		BookmarkProblem bookmarkProblem = bookmarkProblemsDatabase
				.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE).get();
		assertThat(bookmarkProblem.getProperties()).containsOnly(entry("prop2", "newValue2"));
	}

	@Test
	public void testBookmarkProblemDeletedWhenPropertyModified() throws BookmarksException {
		// Given
		BookmarkId bookmarkId = new BookmarkId();
		Bookmark bookmark = new Bookmark(bookmarkId, ImmutableMap.of(Bookmark.PROPERTY_NAME, "bookmark", "prop1",
				"value1", "prop2", "value2", "prop3", "value3"));
		addBookmark(new BookmarkId("rootFolder"), bookmark);
		bookmarkProblemsDatabase.add(new BookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE,
				ImmutableMap.of("prop1", "newValue1")));

		// When
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarksTreeModifier.setPropertyValue(bookmarkId, "prop1", "newValue1"));

		// Then
		assertThat(bookmarkProblemsDatabase.getBookmarkProblem(bookmarkId, BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE))
				.isEmpty();
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		return bookmarksTreeBuilder.build();
	}

	private void addBookmark(BookmarkId parentId, Bookmark bookmark) throws BookmarksException {
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Lists.newArrayList(bookmark)));
	}

}
