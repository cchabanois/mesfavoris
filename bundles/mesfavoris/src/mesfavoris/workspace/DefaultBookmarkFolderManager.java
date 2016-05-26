package mesfavoris.workspace;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.osgi.service.prefs.Preferences;

import mesfavoris.StatusHelper;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public class DefaultBookmarkFolderManager {
	private static final String DEFAULT_FOLDER_KEY = "defaultFolder";
	private final ListenerList listenerList = new ListenerList();
	private final BookmarkDatabase bookmarkDatabase;
	private final Preferences preferences;

	public DefaultBookmarkFolderManager(BookmarkDatabase bookmarkDatabase,
			Preferences preferences) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.preferences = preferences;
	}

	public void setDefaultFolder(BookmarkFolder defaultFolder) {
		preferences.put(DEFAULT_FOLDER_KEY, defaultFolder.getId().toString());
		fireDefaultFolderModified();
	}

	public BookmarkFolder getDefaultFolder() {
		String id = preferences.get(DEFAULT_FOLDER_KEY, null);
		if (id == null) {
			return null;
		}
		// this may return null if the folder has been removed
		return (BookmarkFolder) bookmarkDatabase.getBookmarksTree().getBookmark(new BookmarkId(id));
	}

	public void addListener(IDefaultBookmarkFolderListener listener) {
		listenerList.add(listener);
	}

	public void removeListener(IDefaultBookmarkFolderListener listener) {
		listenerList.remove(listener);
	}

	private void fireDefaultFolderModified() {
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IDefaultBookmarkFolderListener listener = (IDefaultBookmarkFolderListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.defaultBookmarkFolderChanged();
				}

				public void handleException(Throwable exception) {
					StatusHelper.logError(
							"Error while firing default folder event",
							exception);
				}
			});
		}
	}	
	
}
