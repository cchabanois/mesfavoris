package mesfavoris.internal;

import static mesfavoris.internal.Constants.PLACEHOLDER_HOME_NAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertyDescriptorProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.internal.adapters.BookmarkAdapterFactory;
import mesfavoris.internal.bookmarktypes.ImportTeamProjectProvider;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkLabelProvider;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkLocationProvider;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkMarkerAttributesProvider;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkPropertiesProvider;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkTypes;
import mesfavoris.internal.bookmarktypes.extension.PluginGotoBookmark;
import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.internal.numberedbookmarks.NumberedBookmarks;
import mesfavoris.internal.persistence.BookmarksAutoSaver;
import mesfavoris.internal.persistence.LocalBookmarksSaver;
import mesfavoris.internal.persistence.RemoteBookmarksSaver;
import mesfavoris.internal.placeholders.PathPlaceholdersStore;
import mesfavoris.internal.recent.RecentBookmarksDatabase;
import mesfavoris.internal.remote.RemoteBookmarksStoreLoader;
import mesfavoris.internal.remote.RemoteBookmarksTreeChangeEventHandler;
import mesfavoris.internal.service.BookmarksService;
import mesfavoris.internal.service.operations.RefreshRemoteFolderOperation;
import mesfavoris.internal.validation.BookmarksModificationValidator;
import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.internal.visited.VisitedBookmarksDatabase;
import mesfavoris.internal.workspace.BookmarksWorkspaceFactory;
import mesfavoris.internal.workspace.DefaultBookmarkFolderProvider;
import mesfavoris.markers.IBookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.modification.IBookmarksModificationValidator;
import mesfavoris.persistence.IBookmarksDirtyStateTracker;
import mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import mesfavoris.persistence.json.BookmarksTreeJsonSerializer;
import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.service.IBookmarksService;

/**
 * The activator class controls the plug-in life cycle
 */
public class BookmarksPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "mesfavoris"; //$NON-NLS-1$

	// The shared instance
	private static BookmarksPlugin plugin;
	private BookmarkDatabase bookmarkDatabase;
	private BookmarksMarkers bookmarksMarkers;
	private BookmarksAutoSaver bookmarksSaver;
	private IBookmarkLabelProvider bookmarkLabelProvider;
	private IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private IBookmarkLocationProvider bookmarkLocationProvider;
	private IGotoBookmark gotoBookmark;
	private ImportTeamProjectProvider importTeamProjectProvider;
	private RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	private IBookmarksService bookmarksService;
	private VisitedBookmarksDatabase mostVisitedBookmarks;
	private NumberedBookmarks numberedBookmarks;
	private RecentBookmarksDatabase recentBookmarks;
	private PathPlaceholdersStore pathPlaceholdersStore;
	private PluginBookmarkTypes pluginBookmarkTypes;
	
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

		RemoteBookmarksStoreLoader remoteBookmarksStoreLoader = new RemoteBookmarksStoreLoader();
		remoteBookmarksStoreManager = new RemoteBookmarksStoreManager(remoteBookmarksStoreLoader);
		File bookmarksFile = new File(getStateLocation().toFile(), "bookmarks.json");
		BookmarksModificationValidator bookmarksModificationValidator = new BookmarksModificationValidator(
				remoteBookmarksStoreManager);
		bookmarkDatabase = loadBookmarkDatabase(bookmarksFile, bookmarksModificationValidator);
		pluginBookmarkTypes = new PluginBookmarkTypes();
		bookmarkLabelProvider = new PluginBookmarkLabelProvider(pluginBookmarkTypes);
		PluginBookmarkMarkerAttributesProvider bookmarkMarkerAttributesProvider = new PluginBookmarkMarkerAttributesProvider(pluginBookmarkTypes);
		bookmarkPropertiesProvider = new PluginBookmarkPropertiesProvider(pluginBookmarkTypes);
		bookmarkLocationProvider = new PluginBookmarkLocationProvider(pluginBookmarkTypes);
		gotoBookmark = new PluginGotoBookmark(pluginBookmarkTypes);
		bookmarksMarkers = new BookmarksMarkers(bookmarkDatabase, bookmarkMarkerAttributesProvider);
		importTeamProjectProvider = new ImportTeamProjectProvider(pluginBookmarkTypes);
		bookmarksMarkers.init();
		LocalBookmarksSaver localBookmarksSaver = new LocalBookmarksSaver(bookmarksFile,
				new BookmarksTreeJsonSerializer(true));
		RemoteBookmarksSaver remoteBookmarksSaver = new RemoteBookmarksSaver(remoteBookmarksStoreManager);
		bookmarksSaver = new BookmarksAutoSaver(bookmarkDatabase, localBookmarksSaver, remoteBookmarksSaver);
		bookmarksSaver.init();
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
		DefaultBookmarkFolderProvider defaultBookmarkFolderProvider = new DefaultBookmarkFolderProvider(
				bookmarkDatabase);
		File mostVisitedBookmarksFile = new File(getStateLocation().toFile(), "mostVisitedBookmarks.json");
		IEventBroker eventBroker = (IEventBroker) getWorkbench().getService(IEventBroker.class);
		mostVisitedBookmarks = new VisitedBookmarksDatabase(eventBroker, bookmarkDatabase, mostVisitedBookmarksFile);
		mostVisitedBookmarks.init();
		remoteBookmarksTreeChangeEventHandler = new RemoteBookmarksTreeChangeEventHandler(eventBroker,
				new RefreshRemoteFolderOperation(bookmarkDatabase, remoteBookmarksStoreManager, bookmarksSaver));
		remoteBookmarksTreeChangeEventHandler.subscribe();
		numberedBookmarks = new NumberedBookmarks((IEclipsePreferences) preferences.node("numberedBookmarks"),
				eventBroker);
		numberedBookmarks.init();
		File recentBookmarksFile = new File(getStateLocation().toFile(), "recentBookmarks.json");
		recentBookmarks = new RecentBookmarksDatabase(eventBroker, bookmarkDatabase, recentBookmarksFile,
				Duration.ofDays(5));
		recentBookmarks.init();
		bookmarksService = new BookmarksService(bookmarkDatabase, bookmarkPropertiesProvider,
				defaultBookmarkFolderProvider, remoteBookmarksStoreManager, bookmarksSaver, bookmarkLocationProvider,
				gotoBookmark, numberedBookmarks);
		File storeFile = new File(getStateLocation().toFile(), "placeholders.json");
		pathPlaceholdersStore = new PathPlaceholdersStore(storeFile);
		pathPlaceholdersStore.init();
		if (pathPlaceholdersStore.get(PLACEHOLDER_HOME_NAME) == null) {
			IPath userHome = getUserHome();
			if (userHome != null) {
				pathPlaceholdersStore.add(new PathPlaceholder(PLACEHOLDER_HOME_NAME, userHome));
			}
		}
	}

	private BookmarkDatabase loadBookmarkDatabase(File bookmarksFile,
			IBookmarksModificationValidator bookmarksModificationValidator) {
		BookmarksWorkspaceFactory bookmarksWorkspaceFactory = new BookmarksWorkspaceFactory(
				new BookmarksTreeJsonDeserializer(), bookmarksModificationValidator);
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

	private IPath getUserHome() {
		String userHome = System.getProperty("user.home");
		if (userHome == null) {
			return null;
		}
		return new Path(userHome);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		remoteBookmarksTreeChangeEventHandler.unsubscribe();
		bookmarksSaver.close();
		IAdapterManager adapterManager = Platform.getAdapterManager();
		adapterManager.unregisterAdapters(bookmarkAdapterFactory);

		pathPlaceholdersStore.close();
		bookmarksMarkers.close();
		mostVisitedBookmarks.close();
		numberedBookmarks.close();
		recentBookmarks.close();

		plugin = null;
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

	public BookmarkDatabase getBookmarkDatabase() {
		return bookmarkDatabase;
	}

	public IBookmarksMarkers getBookmarksMarkers() {
		return bookmarksMarkers;
	}

	public IBookmarksService getBookmarksService() {
		return bookmarksService;
	}

	public IBookmarkLabelProvider getBookmarkLabelProvider() {
		return bookmarkLabelProvider;
	}

	public IBookmarkLocationProvider getBookmarkLocationProvider() {
		return bookmarkLocationProvider;
	}

	public IGotoBookmark getGotoBookmark() {
		return gotoBookmark;
	}

	public IBookmarkPropertiesProvider getBookmarkPropertiesProvider() {
		return bookmarkPropertiesProvider;
	}

	public ImportTeamProjectProvider getImportTeamProjectProvider() {
		return importTeamProjectProvider;
	}

	public RemoteBookmarksStoreManager getRemoteBookmarksStoreManager() {
		return remoteBookmarksStoreManager;
	}

	public VisitedBookmarksDatabase getMostVisitedBookmarks() {
		return mostVisitedBookmarks;
	}

	public RecentBookmarksDatabase getRecentBookmarks() {
		return recentBookmarks;
	}

	public IBookmarksDirtyStateTracker getBookmarksDirtyStateTracker() {
		return bookmarksSaver;
	}

	public NumberedBookmarks getNumberedBookmarks() {
		return numberedBookmarks;
	}

	public PathPlaceholdersStore getPathPlaceholdersStore() {
		return pathPlaceholdersStore;
	}

	public IBookmarkPropertyDescriptorProvider getBookmarkPropertyDescriptorProvider() {
		return pluginBookmarkTypes;
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
