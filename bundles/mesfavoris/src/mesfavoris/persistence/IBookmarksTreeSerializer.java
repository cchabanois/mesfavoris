package mesfavoris.persistence;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

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