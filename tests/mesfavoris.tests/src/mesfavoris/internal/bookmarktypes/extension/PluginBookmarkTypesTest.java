package mesfavoris.internal.bookmarktypes.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.filebuffers.ResourceTextFileBufferManager;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor;
import mesfavoris.bookmarktype.BookmarkPropertyDescriptor.BookmarkPropertyType;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkType.PrioritizedElement;
import mesfavoris.internal.markers.GotoFileMarkerBookmark;
import mesfavoris.model.Bookmark;
import static org.assertj.core.api.Assertions.assertThat;

public class PluginBookmarkTypesTest {
	private PluginBookmarkTypes pluginBookmarkTypes;

	@Before
	public void setUp() {
		this.pluginBookmarkTypes = new PluginBookmarkTypes();
	}

	@Test
	public void testDefaultBookmarkType() {
		// When
		PluginBookmarkType bookmarkType = pluginBookmarkTypes.getBookmarkType("default");

		// Then
		assertNotNull(bookmarkType);
		assertEquals("default", bookmarkType.getName());
		assertEquals(0, getPrioritizedGotoBookmark(bookmarkType, GotoFileMarkerBookmark.class).getPriority());
		BookmarkPropertyDescriptor propertyDescriptor = bookmarkType.getPropertyDescriptor(Bookmark.PROPERTY_NAME);
		assertEquals(BookmarkPropertyType.STRING, propertyDescriptor.getType());
		assertFalse(propertyDescriptor.isUpdatable());

	}

	@Test
	public void testGetPropertyDescriptor() {
		// When
		BookmarkPropertyDescriptor propertyDescriptor = pluginBookmarkTypes
				.getPropertyDescriptor(Bookmark.PROPERTY_NAME);

		// Then
		assertEquals(BookmarkPropertyType.STRING, propertyDescriptor.getType());
		assertFalse(propertyDescriptor.isUpdatable());
	}

	@Test
	public void testNoErrorOrWarningLogWhileLoadingPropertiesProviders() {
		assertNoErrorOrWarningLog(pluginBookmarkTypes::getPropertiesProviders, BookmarksPlugin.PLUGIN_ID);
	}

	@Test
	public void testNoErrorOrWarningLogWhileLoadingLabelProviders() {
		assertNoErrorOrWarningLog(pluginBookmarkTypes::getLabelProviders, BookmarksPlugin.PLUGIN_ID);
	}	

	@Test
	public void testNoErrorOrWarningLogWhileLoadingLocationProviders() {
		assertNoErrorOrWarningLog(pluginBookmarkTypes::getLocationsProviders, BookmarksPlugin.PLUGIN_ID);
	}	

	@Test
	public void testNoErrorOrWarningLogWhileLoadingMarkerAttributesProviders() {
		assertNoErrorOrWarningLog(pluginBookmarkTypes::getMarkerAttributesProviders, BookmarksPlugin.PLUGIN_ID);
	}	

	@Test
	public void testNoErrorOrWarningLogWhileLoadingGotoBookmarks() {
		assertNoErrorOrWarningLog(pluginBookmarkTypes::getGotoBookmarks, BookmarksPlugin.PLUGIN_ID);
	}		

	@Test
	public void testNoErrorOrWarningLogWhileLoadingImportTeamProjects() {
		assertNoErrorOrWarningLog(pluginBookmarkTypes::getImportTeamProjects, BookmarksPlugin.PLUGIN_ID);
	}	

	@Test
	public void testNoErrorOrWarningLogWhileLoadingBookmarkDetailsParts() {
		assertNoErrorOrWarningLog(pluginBookmarkTypes::getBookmarkDetailParts, BookmarksPlugin.PLUGIN_ID);
	}
	
	private void assertNoErrorOrWarningLog(Runnable runnable, String pluginId) {
		List<IStatus> statuses = new ArrayList<>();
		ILogListener logListener = (status, plugin) -> statuses.add(status);
		WorkbenchPlugin.getDefault().getLog().addLogListener(logListener);
		try {
			runnable.run();
		} finally {
			WorkbenchPlugin.getDefault().getLog().removeLogListener(logListener);
		}
		assertThat(statuses.stream()
				.filter(status -> status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.WARNING))
						.allSatisfy(status -> assertThat(status.getPlugin()).isNotEqualTo(pluginId));
	}

	private PrioritizedElement<IGotoBookmark> getPrioritizedGotoBookmark(PluginBookmarkType bookmarkType,
			Class<? extends IGotoBookmark> type) {
		for (PrioritizedElement<IGotoBookmark> gotoBookmark : bookmarkType.getGotoBookmarks()) {
			if (type.isInstance(gotoBookmark.getElement())) {
				return gotoBookmark;
			}
		}
		return null;
	}
}
