package mesfavoris;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.Preferences;

import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.adapters.BookmarkAdapterFactory;
import mesfavoris.internal.bookmarktypes.ImportTeamProjectProvider;
import mesfavoris.internal.bookmarktypes.PluginBookmarkLabelProvider;
import mesfavoris.internal.bookmarktypes.PluginBookmarkMarkerAttributesProvider;
import mesfavoris.internal.bookmarktypes.PluginBookmarkPropertiesProvider;
import mesfavoris.internal.bookmarktypes.PluginGotoBookmark;
import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.internal.operations.RefreshRemoteFolderOperation;
import mesfavoris.internal.persistence.BookmarksAutoSaver;
import mesfavoris.internal.persistence.LocalBookmarksSaver;
import mesfavoris.internal.persistence.RemoteBookmarksSaver;
import mesfavoris.internal.remote.RemoteBookmarksStoreLoader;
import mesfavoris.internal.remote.RemoteBookmarksTreeChangeEventHandler;
import mesfavoris.internal.service.BookmarksService;
import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.internal.visited.VisitedBookmarksDatabase;
import mesfavoris.internal.workspace.BookmarksWorkspaceFactory;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.persistence.IBookmarksDatabaseDirtyStateTracker;
import mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import mesfavoris.persistence.json.BookmarksTreeJsonSerializer;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.service.IBookmarksService;
import mesfavoris.validation.BookmarkModificationValidator;
import mesfavoris.workspace.DefaultBookmarkFolderManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class BookmarksPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "mesfavoris"; //$NON-NLS-1$

	// The shared instance
	private static BookmarksPlugin plugin;
	private static BookmarkDatabase bookmarkDatabase;
	private static BookmarksMarkers bookmarksMarkers;
	private static BookmarksAutoSaver bookmarksSaver;
	private static DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	private static IBookmarkLabelProvider bookmarkLabelProvider;
	private static IBookmarkMarkerAttributesProvider bookmarkMarkerAttributesProvider;
	private static IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private static IGotoBookmark gotoBookmark;
	private static ImportTeamProjectProvider importTeamProjectProvider;
	private static RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private static IBookmarksService bookmarksService;
	private static VisitedBookmarksDatabase mostVisitedBookmarks;
	
	private final BookmarkAdapterFactory bookmarkAdapterFactory = new BookmarkAdapterFactory();
	private RemoteBookmarksTreeChangeEventHandler remoteBookmarksTreeChangeEventHandler;
	
	/**
	 * The constructor
	 */
	public BookmarksPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IAdapterManager adapterManager = Platform.getAdapterManager();
		adapterManager.registerAdapters(bookmarkAdapterFactory, Bookmark.class);
		adapterManager.registerAdapters(bookmarkAdapterFactory, BookmarkLink.class);
		
		File bookmarksFile = new File(getStateLocation().toFile(), "bookmarks.json");
		bookmarkDatabase = loadBookmarkDatabase(bookmarksFile);
		RemoteBookmarksStoreLoader remoteBookmarksStoreLoader = new RemoteBookmarksStoreLoader();
		remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(remoteBookmarksStoreLoader);
		bookmarkLabelProvider = new PluginBookmarkLabelProvider();
		bookmarkMarkerAttributesProvider = new PluginBookmarkMarkerAttributesProvider();
		bookmarkPropertiesProvider = new PluginBookmarkPropertiesProvider();
		gotoBookmark = new PluginGotoBookmark();
		bookmarksMarkers = new BookmarksMarkers(bookmarkDatabase, bookmarkMarkerAttributesProvider);
		importTeamProjectProvider = new ImportTeamProjectProvider();
		bookmarksMarkers.init();
		LocalBookmarksSaver localBookmarksSaver = new LocalBookmarksSaver(bookmarksFile, new BookmarksTreeJsonSerializer(true));
		RemoteBookmarksSaver remoteBookmarksSaver = new RemoteBookmarksSaver(remoteBookmarksStoreManager);
		bookmarksSaver = new BookmarksAutoSaver(bookmarkDatabase, localBookmarksSaver, remoteBookmarksSaver);
		bookmarksSaver.init();
		Preferences preferences = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		defaultBookmarkFolderManager = new DefaultBookmarkFolderManager(bookmarkDatabase, preferences);
		bookmarksService = new BookmarksService(bookmarkDatabase,
				new BookmarkModificationValidator(remoteBookmarksStoreManager));
		File mostVisitedBookmarksFile = new File(getStateLocation().toFile(), "mostVisitedBookmarks.json");
		IEventBroker eventBroker = (IEventBroker) getWorkbench().getService(IEventBroker.class);
		mostVisitedBookmarks = new VisitedBookmarksDatabase(eventBroker, bookmarkDatabase, mostVisitedBookmarksFile);
		mostVisitedBookmarks.init();
		remoteBookmarksTreeChangeEventHandler = new RemoteBookmarksTreeChangeEventHandler(eventBroker, new RefreshRemoteFolderOperation(bookmarkDatabase, remoteBookmarksStoreManager, bookmarksSaver));
		remoteBookmarksTreeChangeEventHandler.subscribe();
	}

	private BookmarkDatabase loadBookmarkDatabase(File bookmarksFile) {
		BookmarksWorkspaceFactory bookmarksWorkspaceFactory = new BookmarksWorkspaceFactory(
				new BookmarksTreeJsonDeserializer());
		if (bookmarksFile.exists()) {
			try {
				return bookmarksWorkspaceFactory.load(bookmarksFile, new NullProgressMonitor());
			} catch (FileNotFoundException e) {
				return bookmarksWorkspaceFactory.create();
			} catch (IOException e) {
				return bookmarksWorkspaceFactory.create();
			}
		} else {
			return bookmarksWorkspaceFactory.create();
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		remoteBookmarksTreeChangeEventHandler.unsubscribe();
		bookmarksSaver.close();
		IAdapterManager adapterManager = Platform.getAdapterManager();
		adapterManager.unregisterAdapters(bookmarkAdapterFactory);

		bookmarksMarkers.close();
		mostVisitedBookmarks.close();
		
		plugin = null;
		bookmarkDatabase = null;
		bookmarksSaver = null;
		bookmarkLabelProvider = null;
		bookmarkMarkerAttributesProvider = null;
		bookmarkPropertiesProvider = null;
		gotoBookmark = null;
		importTeamProjectProvider = null;
		bookmarksMarkers = null;
		mostVisitedBookmarks = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BookmarksPlugin getDefault() {
		return plugin;
	}

	public static BookmarkDatabase getBookmarkDatabase() {
		return bookmarkDatabase;
	}

	public static DefaultBookmarkFolderManager getDefaultBookmarkFolderManager() {
		return defaultBookmarkFolderManager;
	}

	public static BookmarksMarkers getBookmarksMarkers() {
		return bookmarksMarkers;
	}

	public static IBookmarksService getBookmarksService() {
		return bookmarksService;
	}
	
	public static IBookmarkLabelProvider getBookmarkLabelProvider() {
		return bookmarkLabelProvider;
	}

	public static IBookmarkMarkerAttributesProvider getBookmarkMarkerAttributesProvider() {
		return bookmarkMarkerAttributesProvider;
	}

	public static IGotoBookmark getGotoBookmark() {
		return gotoBookmark;
	}

	public static IBookmarkPropertiesProvider getBookmarkPropertiesProvider() {
		return bookmarkPropertiesProvider;
	}

	public static ImportTeamProjectProvider getImportTeamProjectProvider() {
		return importTeamProjectProvider;
	}

	public static RemoteBookmarksStoreManager getRemoteBookmarksStoreManager() {
		return remoteBookmarksStoreManager;
	}

	public static VisitedBookmarksDatabase getMostVisitedBookmarks() {
		return mostVisitedBookmarks;
	}
	
	public static IBookmarksDatabaseDirtyStateTracker getBookmarksDatabaseDirtyStateTracker() {
		return bookmarksSaver;
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
