package mesfavoris.gdrive.operations;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.google.api.services.drive.Drive;

import mesfavoris.BookmarksException;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.gdrive.operations.DownloadHeadRevisionOperation.Contents;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksTreeDeserializer;
import mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import mesfavoris.service.IBookmarksService;

public class ImportBookmarkFileOperation extends AbstractGDriveOperation {
	private final BookmarkMappingsStore bookmarkMappingsStore;
	private final IBookmarksService bookmarksService;

	public ImportBookmarkFileOperation(Drive drive, BookmarkMappingsStore bookmarkMappingsStore,
			IBookmarksService bookmarksService) {
		super(drive);
		this.bookmarkMappingsStore = bookmarkMappingsStore;
		this.bookmarksService = bookmarksService;
	}

	public void importBookmarkFile(BookmarkId parentId, String fileId, IProgressMonitor monitor)
			throws BookmarksException, IOException {
		try {
			monitor.beginTask("Importing bookmark folder", 100);
			DownloadHeadRevisionOperation downloadFileOperation = new DownloadHeadRevisionOperation(drive);
			Contents contents = downloadFileOperation.downloadFile(fileId, new SubProgressMonitor(monitor, 80));
			IBookmarksTreeDeserializer deserializer = new BookmarksTreeJsonDeserializer();
			BookmarksTree bookmarksTree = deserializer.deserialize(
					new StringReader(new String(contents.getFileContents(), "UTF-8")),
					new SubProgressMonitor(monitor, 20));
			bookmarksService.addBookmarksTree(parentId, bookmarksTree,
					newBookmarksTree -> bookmarkMappingsStore.add(bookmarksTree.getRootFolder().getId(), fileId));
		} finally {
			monitor.done();
		}

	}

}
