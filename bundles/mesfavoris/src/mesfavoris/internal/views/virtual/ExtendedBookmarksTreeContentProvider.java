package mesfavoris.internal.views.virtual;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.collect.Lists;

import mesfavoris.internal.views.BookmarksTreeContentProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarksTree;

/**
 * Add {@link VirtualBookmarkFolder} and {@link BookmarkLink} support to
 * {@link BookmarksTree}
 * 
 * @author cchabanois
 *
 */
public class ExtendedBookmarksTreeContentProvider extends BookmarksTreeContentProvider {
	private final List<VirtualBookmarkFolder> virtualBookmarkFolders;
	private ColumnViewer viewer;
	private IVirtualBookmarkFolderListener listener;

	public ExtendedBookmarksTreeContentProvider(BookmarkDatabase bookmarkDatabase,
			List<VirtualBookmarkFolder> virtualBookmarkFolders) {
		super(bookmarkDatabase);
		this.virtualBookmarkFolders = virtualBookmarkFolders;
		this.listener = virtualBookmarkFolder -> viewer.getControl().getDisplay().asyncExec(() -> {
			viewer.refresh(virtualBookmarkFolder);
		});
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		this.viewer = (ColumnViewer) viewer;
		removeListener();
		addListener();
	}

	@Override
	public void dispose() {
		removeListener();
		super.dispose();
	}

	private void addListener() {
		virtualBookmarkFolders.forEach(virtualBookmarkFolder -> virtualBookmarkFolder.addListener(listener));
	}

	private void removeListener() {
		virtualBookmarkFolders.forEach(virtualBookmarkFolder -> virtualBookmarkFolder.removeListener(listener));
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof BookmarkFolder) {
			BookmarkFolder bookmarkFolder = (BookmarkFolder) inputElement;
			List<Object> elements = Lists.newArrayList(super.getElements(inputElement));
			elements.addAll(virtualBookmarkFolders.stream()
					.filter(virtualBookmarkFolder -> virtualBookmarkFolder.getParentId().equals(bookmarkFolder.getId()))
					.collect(Collectors.toList()));
			return elements.toArray();
		}
		if (inputElement instanceof VirtualBookmarkFolder) {
			VirtualBookmarkFolder virtualBookmarkFolder = (VirtualBookmarkFolder) inputElement;
			return virtualBookmarkFolder.getChildren().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (parent instanceof BookmarkFolder) {
			BookmarkFolder bookmarkFolder = (BookmarkFolder) parent;
			List<Object> elements = Lists.newArrayList(super.getChildren(parent));
			elements.addAll(virtualBookmarkFolders.stream()
					.filter(virtualBookmarkFolder -> virtualBookmarkFolder.getParentId().equals(bookmarkFolder.getId()))
					.collect(Collectors.toList()));
			return elements.toArray();
		}
		if (parent instanceof VirtualBookmarkFolder) {
			VirtualBookmarkFolder virtualBookmarkFolder = (VirtualBookmarkFolder) parent;
			return virtualBookmarkFolder.getChildren().toArray();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object child) {
		if (child instanceof Bookmark) {
			return super.getParent(child);
		}
		if (child instanceof VirtualBookmarkFolder) {
			VirtualBookmarkFolder virtualBookmarkFolder = (VirtualBookmarkFolder) child;
			return bookmarkDatabase.getBookmarksTree().getBookmark(virtualBookmarkFolder.getParentId());
		}
		if (child instanceof BookmarkLink) {
			BookmarkLink bookmarkLink = (BookmarkLink) child;
			return bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkLink.getParentId());
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object parent) {
		if (parent instanceof BookmarkFolder) {
			return super.hasChildren(parent);
		}
		if (parent instanceof VirtualBookmarkFolder) {
			return true;
		}
		return false;
	}

}
