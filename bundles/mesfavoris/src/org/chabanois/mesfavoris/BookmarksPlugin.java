package org.chabanois.mesfavoris;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.chabanois.mesfavoris.bookmarktype.IBookmarkLabelProvider;
import org.chabanois.mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import org.chabanois.mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import org.chabanois.mesfavoris.bookmarktype.IGotoBookmark;
import org.chabanois.mesfavoris.internal.adapters.BookmarkAdapterFactory;
import org.chabanois.mesfavoris.internal.bookmarktypes.ImportTeamProjectProvider;
import org.chabanois.mesfavoris.internal.bookmarktypes.PluginBookmarkLabelProvider;
import org.chabanois.mesfavoris.internal.bookmarktypes.PluginBookmarkMarkerAttributesProvider;
import org.chabanois.mesfavoris.internal.bookmarktypes.PluginBookmarkPropertiesProvider;
import org.chabanois.mesfavoris.internal.bookmarktypes.PluginGotoBookmark;
import org.chabanois.mesfavoris.internal.markers.BookmarksMarkers;
import org.chabanois.mesfavoris.internal.persistence.BookmarksAutoSaver;
import org.chabanois.mesfavoris.internal.persistence.LocalBookmarksSaver;
import org.chabanois.mesfavoris.internal.persistence.RemoteBookmarksSaver;
import org.chabanois.mesfavoris.internal.remote.RemoteBookmarksStoreLoader;
import org.chabanois.mesfavoris.internal.service.BookmarksService;
import org.chabanois.mesfavoris.internal.workspace.BookmarksWorkspaceFactory;
import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import org.chabanois.mesfavoris.persistence.json.BookmarksTreeJsonSerializer;
import org.chabanois.mesfavoris.remote.RemoteBookmarksStoreManager;
import org.chabanois.mesfavoris.service.IBookmarksService;
import org.chabanois.mesfavoris.validation.BookmarkModificationValidator;
import org.chabanois.mesfavoris.workspace.DefaultBookmarkFolderManager;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.Preferences;

/**
 * The activator class controls the plug-in life cycle
 */
public class BookmarksPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.chabanois.mesfavoris"; //$NON-NLS-1$

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

	private final BookmarkAdapterFactory bookmarkAdapterFactory = new BookmarkAdapterFactory();

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
		bookmarksSaver.close();
		IAdapterManager adapterManager = Platform.getAdapterManager();
		adapterManager.unregisterAdapters(bookmarkAdapterFactory);

		bookmarksMarkers.close();

		plugin = null;
		bookmarkDatabase = null;
		bookmarksSaver = null;
		bookmarkLabelProvider = null;
		bookmarkMarkerAttributesProvider = null;
		bookmarkPropertiesProvider = null;
		gotoBookmark = null;
		importTeamProjectProvider = null;
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
