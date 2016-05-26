package mesfavoris.internal.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksTreeSerializer;

public class LocalBookmarksSaver {
	private final File file;
	private final IBookmarksTreeSerializer bookmarksSerializer;
	
	public LocalBookmarksSaver(File file, IBookmarksTreeSerializer bookmarksSerializer) {
		this.file = file;
		this.bookmarksSerializer = bookmarksSerializer;
	}
	
	public void saveBookmarks(BookmarksTree bookmarksTree, IProgressMonitor monitor) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			bookmarksSerializer.serialize(bookmarksTree,
					bookmarksTree.getRootFolder().getId(), writer, monitor);
		} catch (IOException e) {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e1) {
				}
			}
		}
	}	
	
}
