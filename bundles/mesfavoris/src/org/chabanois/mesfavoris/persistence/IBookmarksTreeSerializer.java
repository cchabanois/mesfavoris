package org.chabanois.mesfavoris.persistence;

import java.io.IOException;
import java.io.Writer;

import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Serialize a {@link BookmarksTree}
 * 
 * @author cchabanois
 *
 */
public interface IBookmarksTreeSerializer {

	/**
	 * Serialize the given bookmarks subTree
	 * 
	 * @param bookmarksTree
	 * @param bookmarkFolderId
	 *            the subtree to serialize
	 * @param writer
	 * @param monitor
	 * @throws IOException
	 */
	public void serialize(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, Writer writer, IProgressMonitor monitor)
			throws IOException;

}