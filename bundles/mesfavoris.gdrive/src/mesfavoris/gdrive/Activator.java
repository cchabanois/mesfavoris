package mesfavoris.gdrive;

import java.io.File;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import mesfavoris.BookmarksPlugin;
import mesfavoris.gdrive.changes.BookmarksFileChangeManager;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.mappings.BookmarkMappingsPersister;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "mesfavoris.gdrive"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static GDriveConnectionManager gDriveConnectionManager;
	private static BookmarkMappingsStore bookmarkMappingsStore;
	private static BookmarksFileChangeManager bookmarksFileChangeManager;

	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		File dataStoreDir = new File(getStateLocation().toFile(), "gdrive-datastore");
		dataStoreDir.mkdir();
		gDriveConnectionManager = new GDriveConnectionManager(dataStoreDir, "mes favoris", "eclipse-bookmarks");
		gDriveConnectionManager.init();
		File storeFile = new File(getStateLocation().toFile(), "bookmarksStore.json");
		bookmarkMappingsStore = new BookmarkMappingsStore(new BookmarkMappingsPersister(storeFile));
		bookmarkMappingsStore.init();
		BookmarksPlugin.getBookmarkDatabase().addListener(bookmarkMappingsStore);
		bookmarksFileChangeManager = new BookmarksFileChangeManager(gDriveConnectionManager, bookmarkMappingsStore);
		bookmarksFileChangeManager.init();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		BookmarksPlugin.getBookmarkDatabase().removeListener(bookmarkMappingsStore);
		bookmarkMappingsStore.close();
		try {
			gDriveConnectionManager.close();
			bookmarksFileChangeManager.close();
		} finally {
			plugin = null;
			super.stop(context);
		}
	}

	public static GDriveConnectionManager getGDriveConnectionManager() {
		return gDriveConnectionManager;
	}

	public static BookmarkMappingsStore getBookmarkMappingsStore() {
		return bookmarkMappingsStore;
	}

	public static BookmarksFileChangeManager getBookmarksFileChangeManager() {
		return bookmarksFileChangeManager;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
