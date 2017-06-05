package mesfavoris.internal.views.properties;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

import mesfavoris.BookmarksException;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.problems.BookmarkProblemsDatabase;
import mesfavoris.internal.problems.extension.BookmarkProblemDescriptors;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

public class BookmarkPropertySourceTest {
	private BookmarkProblemsDatabase bookmarkProblemsDatabase;
	private BookmarkDatabase bookmarkDatabase;
	private File file;
	private IEventBroker eventBroker;
	private BookmarkPropertySource bookmarkPropertySource;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		this.eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		bookmarkDatabase = new BookmarkDatabase("main", getInitialTree());
		file = temporaryFolder.newFile();
		bookmarkProblemsDatabase = new BookmarkProblemsDatabase(eventBroker, bookmarkDatabase,
				new BookmarkProblemDescriptors(), file);
		bookmarkProblemsDatabase.init();
		bookmarkPropertySource = new BookmarkPropertySource(bookmarkDatabase, bookmarkProblemsDatabase,
				BookmarksPlugin.getDefault().getBookmarkProblemDescriptors(), new BookmarkId("bookmark1"));
	}

	@After
	public void tearDown() throws Exception {
		bookmarkProblemsDatabase.close();
	}

	@Test
	public void testPropertiesWithoutProblems() throws Exception {
		// Given
		addBookmark(new BookmarkId("rootFolder"),
				bookmark("bookmark1").withProperty(PROP_LINE_CONTENT, "first content").build());

		// When
		IPropertyDescriptor[] propertyDescriptors = bookmarkPropertySource.getPropertyDescriptors();

		// Then
		assertThat(propertyDescriptors).hasSize(2);
		IPropertyDescriptor namePropertyDescriptor = getPropertyDescriptor(propertyDescriptors, Bookmark.PROPERTY_NAME);
		assertThat(namePropertyDescriptor.getCategory()).isEqualTo("default");
		assertThat(bookmarkPropertySource.getPropertyValue(Bookmark.PROPERTY_NAME)).isEqualTo("bookmark1");
		IPropertyDescriptor contentPropertyDescriptor = getPropertyDescriptor(propertyDescriptors, PROP_LINE_CONTENT);
		assertThat(contentPropertyDescriptor.getCategory()).isEqualTo("texteditor");
		assertThat(bookmarkPropertySource.getPropertyValue(PROP_LINE_CONTENT)).isEqualTo("first content");
	}

	@Test
	public void testPropertyWithPropertyNeedingUpdate() throws Exception {
		// Given
		addBookmark(new BookmarkId("rootFolder"), bookmark("bookmark1").withProperty(PROP_LINE_NUMBER, "10").build());
		bookmarkProblemsDatabase.add(new BookmarkProblem(new BookmarkId("bookmark1"),
				BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE, ImmutableMap.of(PROP_LINE_NUMBER, "120")));

		// When
		IPropertyDescriptor[] propertyDescriptors = bookmarkPropertySource.getPropertyDescriptors();
		IPropertyDescriptor propertyDescriptor = getPropertyDescriptor(propertyDescriptors, PROP_LINE_NUMBER);
		Object propertyValue = bookmarkPropertySource.getPropertyValue(PROP_LINE_NUMBER);

		// Then
		assertThat(propertyDescriptor.getDisplayName()).isEqualTo(PROP_LINE_NUMBER);
		assertThat(propertyValue).isInstanceOf(IPropertySource.class);
		IPropertySource valuePropertySource = (IPropertySource) propertyValue;
		IPropertyDescriptor updatePropertyDescriptor = getPropertyDescriptor(
				valuePropertySource.getPropertyDescriptors(), PROP_LINE_NUMBER);
		assertThat(valuePropertySource.getPropertyValue(PROP_LINE_NUMBER)).isEqualTo("120");
		assertThat(updatePropertyDescriptor.getDisplayName()).isEqualTo("Updated value");
	}

	@Test
	public void testPropertyWithPropertyMayUpdate() throws Exception {
		// Given
		addBookmark(new BookmarkId("rootFolder"), bookmark("bookmark1").withProperty(PROP_LINE_NUMBER, "10").build());
		bookmarkProblemsDatabase.add(new BookmarkProblem(new BookmarkId("bookmark1"),
				BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE, ImmutableMap.of(PROP_LINE_NUMBER, "11")));

		// When
		IPropertyDescriptor[] propertyDescriptors = bookmarkPropertySource.getPropertyDescriptors();
		IPropertyDescriptor propertyDescriptor = getPropertyDescriptor(propertyDescriptors, PROP_LINE_NUMBER);
		Object propertyValue = bookmarkPropertySource.getPropertyValue(PROP_LINE_NUMBER);

		// Then
		assertThat(propertyDescriptor.getDisplayName()).isEqualTo(PROP_LINE_NUMBER);
		assertThat(propertyValue).isInstanceOf(IPropertySource.class);
		IPropertySource valuePropertySource = (IPropertySource) propertyValue;
		IPropertyDescriptor updatePropertyDescriptor = getPropertyDescriptor(
				valuePropertySource.getPropertyDescriptors(), PROP_LINE_NUMBER);
		assertThat(valuePropertySource.getPropertyValue(PROP_LINE_NUMBER)).isEqualTo("11");
		assertThat(updatePropertyDescriptor.getDisplayName()).isEqualTo("Updated value");
	}

	@Test
	public void testNewPropertyValue() throws Exception {
		// Given
		addBookmark(new BookmarkId("rootFolder"), bookmark("bookmark1").build());
		bookmarkProblemsDatabase.add(new BookmarkProblem(new BookmarkId("bookmark1"),
				BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE, ImmutableMap.of(PROP_LINE_NUMBER, "0")));

		// When
		IPropertyDescriptor[] propertyDescriptors = bookmarkPropertySource.getPropertyDescriptors();
		IPropertyDescriptor propertyDescriptor = getPropertyDescriptor(propertyDescriptors, PROP_LINE_NUMBER);
		Object propertyValue = bookmarkPropertySource.getPropertyValue(PROP_LINE_NUMBER);

		// Then
		assertThat(propertyDescriptor.getDisplayName()).isEqualTo("lineNumber (New value)");
		assertThat(propertyValue).isEqualTo("0");
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		return bookmarksTreeBuilder.build();
	}

	private void addBookmark(BookmarkId parentId, Bookmark... bookmark) throws BookmarksException {
		bookmarkDatabase
				.modify(bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Arrays.asList(bookmark)));
	}

	private IPropertyDescriptor getPropertyDescriptor(IPropertyDescriptor[] propertyDescriptors, String id) {
		for (IPropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (id.equals(propertyDescriptor.getId())) {
				return propertyDescriptor;
			}
		}
		return null;
	}

}
