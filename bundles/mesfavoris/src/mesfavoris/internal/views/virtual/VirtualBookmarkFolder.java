package mesfavoris.internal.views.virtual;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;

import mesfavoris.StatusHelper;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

/**
 * A virtual bookmark folder. This folder does not exist in the BookmarkDatabase. 
 * 
 * @author cchabanois
 *
 */
public abstract class VirtualBookmarkFolder implements IAdaptable {
	protected final BookmarkId parentId;
	protected final BookmarkFolder bookmarkFolder;
	protected final ListenerList listenerList = new ListenerList();
	
	public VirtualBookmarkFolder(BookmarkId parentId, String name) {
		this.parentId = parentId;
		this.bookmarkFolder = new BookmarkFolder(new BookmarkId(), name);
	}
	
	public BookmarkId getParentId() {
		return parentId;
	}

	public BookmarkFolder getBookmarkFolder() {
		return bookmarkFolder;
	}

	public abstract List<BookmarkLink> getChildren();
	
	public synchronized void addListener(IVirtualBookmarkFolderListener listener) {
		if (listenerList.isEmpty()) {
			initListening();
		}
		listenerList.add(listener);
	}

	protected abstract void initListening();
	
	protected abstract void stopListening();
	
	public synchronized void removeListener(IVirtualBookmarkFolderListener listener) {
		listenerList.remove(listener);
		if (listenerList.isEmpty()) {
			stopListening();
		}
	}

	protected void fireChildrenChanged() {
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IVirtualBookmarkFolderListener listener = (IVirtualBookmarkFolderListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.childrenChanged(VirtualBookmarkFolder.this);
				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error while firing children changed event", exception);
				}
			});
		}
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == Bookmark.class || adapter == BookmarkFolder.class) {
			return getBookmarkFolder();
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarkFolder == null) ? 0 : bookmarkFolder.hashCode());
		result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VirtualBookmarkFolder other = (VirtualBookmarkFolder) obj;
		if (bookmarkFolder == null) {
			if (other.bookmarkFolder != null)
				return false;
		} else if (!bookmarkFolder.equals(other.bookmarkFolder))
			return false;
		if (parentId == null) {
			if (other.parentId != null)
				return false;
		} else if (!parentId.equals(other.parentId))
			return false;
		return true;
	}	

}