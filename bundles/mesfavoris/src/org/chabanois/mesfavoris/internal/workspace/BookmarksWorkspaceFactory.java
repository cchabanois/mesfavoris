package org.chabanois.mesfavoris.internal.workspace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.chabanois.mesfavoris.model.BookmarkDatabase;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.persistence.IBookmarksTreeDeserializer;
import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;

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
