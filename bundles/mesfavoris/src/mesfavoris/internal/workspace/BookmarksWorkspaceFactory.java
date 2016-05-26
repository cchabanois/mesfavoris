package mesfavoris.internal.workspace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;

import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksTreeDeserializer;

public class BookmarksWorkspaceFactory {
	private final IBookmarksTreeDeserializer bookmarksDeserializer;

	public BookmarksWorkspaceFactory(IBookmarksTreeDeserializer bookmarksDeserializer) {
		this.bookmarksDeserializer = bookmarksDeserializer;
	}

	public BookmarkDatabase load(File file, IProgressMonitor monitor) throws FileNotFoundException, IOException {
		BookmarksTree bookmarksTree = bookmarksDeserializer.deserialize(new FileReader(file), monitor);
		return new BookmarkDatabase("main", bookmarksTree);
	}

	public BookmarkDatabase create() {
		BookmarkFolder rootFolder = new BookmarkFolder(new BookmarkId("root"), "Root");
		BookmarksTree bookmarksTree = new BookmarksTree(rootFolder);
		BookmarkFolder defaultFolder = new BookmarkFolder(new BookmarkId("default"), "default");
		bookmarksTree = bookmarksTree.addBookmarks(rootFolder.getId(), Lists.newArrayList(defaultFolder));
		return new BookmarkDatabase("main", bookmarksTree);
	}

}
