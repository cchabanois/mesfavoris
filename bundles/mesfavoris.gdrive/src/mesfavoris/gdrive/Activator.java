package mesfavoris.gdrive;

import java.io.File;
import java.time.Duration;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import mesfavoris.MesFavoris;
import mesfavoris.gdrive.changes.BookmarksFileChangeManager;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.mappings.BookmarkMappingsPersister;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.model.BookmarkDatabase;
import static mesfavoris.gdrive.preferences.IPreferenceConstants.POLL_CHANGES_INTERVAL_PREF;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "mesfavoris.gdrive"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private BookmarkDatabase bookmarkDatabase;
	private GDriveConnectionManager gDriveConnectionManager;
	private BookmarkMappingsStore bookmarkMappingsStore;
	private BookmarksFileChangeManager bookmarksFileChangeManager;

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
		bookmarkDatabase = MesFavoris.getBookmarkDatabase();
		bookmarkDatabase.addListener(bookmarkMappingsStore);
		bookmarksFileChangeManager = new BookmarksFileChangeManager(gDriveConnectionManager, bookmarkMappingsStore,
				() -> Duration.ofSeconds(getPreferenceStore().getInt(POLL_CHANGES_INTERVAL_PREF)));
		bookmarksFileChangeManager.init();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// listener is not OSGI friendly ...
		bookmarkDatabase.removeListener(bookmarkMappingsStore);
		bookmarkMappingsStore.close();
		try {
			gDriveConnectionManager.close();
			bookmarksFileChangeManager.close();
		} finally {
			plugin = null;
			super.stop(context);
		}
	}

	public GDriveConnectionManager getGDriveConnectionManager() {
		return gDriveConnectionManager;
	}

	public BookmarkMappingsStore getBookmarkMappingsStore() {
		return bookmarkMappingsStore;
	}

	public BookmarksFileChangeManager getBookmarksFileChangeManager() {
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

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

}
