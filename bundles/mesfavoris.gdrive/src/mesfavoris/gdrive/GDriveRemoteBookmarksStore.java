package mesfavoris.gdrive;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.common.base.Charsets;

import mesfavoris.gdrive.changes.BookmarksFileChangeManager;
import mesfavoris.gdrive.changes.IBookmarksFileChangeListener;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.connection.IConnectionListener;
import mesfavoris.gdrive.mappings.BookmarkMappingsStore;
import mesfavoris.gdrive.mappings.IBookmarkMappingsListener;
import mesfavoris.gdrive.operations.CreateFileOperation;
import mesfavoris.gdrive.operations.DownloadHeadRevisionOperation;
import mesfavoris.gdrive.operations.DownloadHeadRevisionOperation.FileContents;
import mesfavoris.gdrive.operations.TrashFileOperation;
import mesfavoris.gdrive.operations.UpdateFileOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.persistence.IBookmarksTreeDeserializer;
import mesfavoris.persistence.IBookmarksTreeSerializer;
import mesfavoris.persistence.json.BookmarksTreeJsonDeserializer;
import mesfavoris.persistence.json.BookmarksTreeJsonSerializer;
import mesfavoris.remote.AbstractRemoteBookmarksStore;
import mesfavoris.remote.ConflictException;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksTree;
import mesfavoris.remote.UserInfo;

public class GDriveRemoteBookmarksStore extends AbstractRemoteBookmarksStore {
	private final GDriveConnectionManager gDriveConnectionManager;
	private final BookmarkMappingsStore bookmarkMappingsStore;
	private final BookmarksFileChangeManager bookmarksFileChangeManager;
	private final Duration durationForNewRevision;

	public GDriveRemoteBookmarksStore() {
		this((IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class),
				Activator.getDefault().getGDriveConnectionManager(), Activator.getDefault().getBookmarkMappingsStore(),
				Activator.getDefault().getBookmarksFileChangeManager());
	}

	public GDriveRemoteBookmarksStore(IEventBroker eventBroker, GDriveConnectionManager gDriveConnectionManager,
			BookmarkMappingsStore bookmarksMappingsStore, BookmarksFileChangeManager bookmarksFileChangeManager) {
		super(eventBroker);
		this.gDriveConnectionManager = gDriveConnectionManager;
		this.bookmarkMappingsStore = bookmarksMappingsStore;
		this.bookmarksFileChangeManager = bookmarksFileChangeManager;
		this.durationForNewRevision = Duration.ofMinutes(2);
		this.gDriveConnectionManager.addConnectionListener(new IConnectionListener() {

			@Override
			public void disconnected() {
				postDisconnected();
			}

			@Override
			public void connected() {
				postConnected();
			}
		});
		this.bookmarkMappingsStore.addListener(new IBookmarkMappingsListener() {

			@Override
			public void mappingRemoved(BookmarkId bookmarkFolderId) {
				postMappingRemoved(bookmarkFolderId);
			}

			@Override
			public void mappingAdded(BookmarkId bookmarkFolderId) {
				postMappingAdded(bookmarkFolderId);
			}
		});
		this.bookmarksFileChangeManager.addListener(new IBookmarksFileChangeListener() {

			@Override
			public void bookmarksFileChanged(BookmarkId bookmarkFolderId, Change change) {
				postRemoteBookmarksTreeChanged(bookmarkFolderId);
			}
		});
	}

	@Override
	public void connect(IProgressMonitor monitor) throws IOException {
		gDriveConnectionManager.connect(monitor);
	}

	@Override
	public void disconnect(IProgressMonitor monitor) throws IOException {
		gDriveConnectionManager.disconnect(monitor);
	}

	@Override
	public State getState() {
		return gDriveConnectionManager.getState();
	}

	@Override
	public RemoteBookmarksTree add(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, IProgressMonitor monitor)
			throws IOException {
		Drive drive = gDriveConnectionManager.getDrive();
		String bookmarkDirId = gDriveConnectionManager.getApplicationFolderId();
		if (drive == null || bookmarkDirId == null) {
			throw new IllegalStateException("Not connected");
		}
		BookmarkFolder bookmarkFolder = (BookmarkFolder) bookmarksTree.getBookmark(bookmarkFolderId);
		if (bookmarkFolder == null) {
			throw new IllegalArgumentException("Cannot find folder with id " + bookmarkFolderId);
		}
		try {
			monitor.beginTask("Saving bookmark folder", 100);
			CreateFileOperation createFileOperation = new CreateFileOperation(drive);
			byte[] content = serializeBookmarkFolder(bookmarksTree, bookmarkFolderId,
					new SubProgressMonitor(monitor, 20));
			com.google.api.services.drive.model.File file = createFileOperation.createFile(bookmarkDirId,
					bookmarkFolder.getPropertyValue(Bookmark.PROPERTY_NAME), content,
					new SubProgressMonitor(monitor, 80));
			bookmarkMappingsStore.add(bookmarkFolder.getId(), file);
			return new RemoteBookmarksTree(this, bookmarksTree.subTree(bookmarkFolderId), file.getEtag());
		} finally {
			monitor.done();
		}
	}

	private byte[] serializeBookmarkFolder(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId,
			IProgressMonitor monitor) throws IOException {
		IBookmarksTreeSerializer serializer = new BookmarksTreeJsonSerializer(true);
		StringWriter writer = new StringWriter();
		serializer.serialize(bookmarksTree, bookmarkFolderId, writer, monitor);
		byte[] content = writer.getBuffer().toString().getBytes(Charsets.UTF_8);
		return content;
	}

	@Override
	public void remove(BookmarkId bookmarkFolderId, IProgressMonitor monitor) throws IOException {
		Drive drive = gDriveConnectionManager.getDrive();
		if (drive == null) {
			throw new IllegalStateException("Not connected");
		}
		String fileId = bookmarkMappingsStore.getMapping(bookmarkFolderId).map(mapping -> mapping.getFileId())
				.orElseThrow(() -> new IllegalArgumentException("This folder has not been added to gDrive"));
		monitor.beginTask("Removing bookmark folder from gDrive", 100);
		try {
			bookmarkMappingsStore.remove(bookmarkFolderId);
			monitor.worked(10);
			TrashFileOperation trashFileOperation = new TrashFileOperation(drive);
			trashFileOperation.trashFile(fileId);
			monitor.worked(90);
		} finally {
			monitor.done();
		}
	}

	@Override
	public Set<RemoteBookmarkFolder> getRemoteBookmarkFolders() {
		return bookmarkMappingsStore.getMappings().stream()
				.map(mapping -> new RemoteBookmarkFolder(getDescriptor().getId(), mapping.getBookmarkFolderId(),
						mapping.getProperties()))
				.collect(Collectors.toSet());
	}

	@Override
	public Optional<RemoteBookmarkFolder> getRemoteBookmarkFolder(BookmarkId bookmarkFolderId) {
		return bookmarkMappingsStore.getMapping(bookmarkFolderId)
				.map(mapping -> new RemoteBookmarkFolder(getDescriptor().getId(), mapping.getBookmarkFolderId(),
						mapping.getProperties()));
	}

	@Override
	public RemoteBookmarksTree load(BookmarkId bookmarkFolderId, IProgressMonitor monitor) throws IOException {
		Drive drive = gDriveConnectionManager.getDrive();
		String bookmarkDirId = gDriveConnectionManager.getApplicationFolderId();
		if (drive == null || bookmarkDirId == null) {
			throw new IllegalStateException("Not connected");
		}
		String fileId = bookmarkMappingsStore.getMapping(bookmarkFolderId).map(mapping -> mapping.getFileId())
				.orElseThrow(() -> new IllegalArgumentException("This folder has not been added to gDrive"));
		try {
			monitor.beginTask("Loading bookmark folder", 100);
			DownloadHeadRevisionOperation downloadFileOperation = new DownloadHeadRevisionOperation(drive);
			FileContents contents = downloadFileOperation.downloadFile(fileId, new SubProgressMonitor(monitor, 80));
			bookmarkMappingsStore.update(contents.getFile());
			IBookmarksTreeDeserializer deserializer = new BookmarksTreeJsonDeserializer();
			BookmarksTree bookmarkFolder = deserializer.deserialize(
					new StringReader(new String(contents.getFileContents(), "UTF-8")),
					new SubProgressMonitor(monitor, 20));
			return new RemoteBookmarksTree(this, bookmarkFolder, contents.getFile().getEtag());
		} finally {
			monitor.done();
		}
	}

	@Override
	public RemoteBookmarksTree save(BookmarksTree bookmarksTree, BookmarkId bookmarkFolderId, String etag,
			IProgressMonitor monitor) throws IOException, ConflictException {
		Drive drive = gDriveConnectionManager.getDrive();
		String bookmarkDirId = gDriveConnectionManager.getApplicationFolderId();
		if (drive == null || bookmarkDirId == null) {
			throw new IllegalStateException("Not connected");
		}
		String fileId = bookmarkMappingsStore.getMapping(bookmarkFolderId).map(mapping -> mapping.getFileId())
				.orElseThrow(() -> new IllegalArgumentException("This folder has not been added to gDrive"));
		try {
			monitor.beginTask("Saving bookmark folder", 100);
			UpdateFileOperation updateFileOperation = new UpdateFileOperation(drive, durationForNewRevision);
			byte[] content = serializeBookmarkFolder(bookmarksTree, bookmarkFolderId,
					new SubProgressMonitor(monitor, 20));
			File file = updateFileOperation.updateFile(fileId, content, etag, new SubProgressMonitor(monitor, 80));
			bookmarkMappingsStore.update(file);
			return new RemoteBookmarksTree(this, bookmarksTree.subTree(bookmarkFolderId), file.getEtag());
		} catch (GoogleJsonResponseException e) {
			if (e.getStatusCode() == 412) {
				// Precondition Failed
				throw new ConflictException();
			} else {
				throw new IOException(e);
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public UserInfo getUserInfo() {
		return gDriveConnectionManager.getUserInfo();
	}

}
