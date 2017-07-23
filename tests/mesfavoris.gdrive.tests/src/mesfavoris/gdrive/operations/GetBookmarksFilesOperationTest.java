package mesfavoris.gdrive.operations;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.test.GDriveConnectionRule;
import mesfavoris.tests.commons.waits.Waiter;
import static mesfavoris.gdrive.operations.BookmarkFileConstants.MESFAVORIS_MIME_TYPE;

public class GetBookmarksFilesOperationTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionUser1 = new GDriveConnectionRule(GDriveTestUser.USER1, true);

	@Rule
	public GDriveConnectionRule gdriveConnectionUser2 = new GDriveConnectionRule(GDriveTestUser.USER2, true);

	@Test
	public void testGetSharedBookmarkFile() throws Exception {
		// Given
		File file = createBookmarksFile(gdriveConnectionUser1, "user1File.txt", "the contents");
		share(gdriveConnectionUser1, file.getId(), GDriveTestUser.USER2.getEmail());
		GetBookmarkFilesOperation operation = new GetBookmarkFilesOperation(gdriveConnectionUser2.getDrive());

		// Then
		Waiter.waitUntil("Bookmark files does not contain shared file", () -> operation.getBookmarkFiles().stream()
				.map(f -> f.getId()).collect(Collectors.toList()).contains(file.getId()), Duration.ofSeconds(10));
	}

	private File createBookmarksFile(GDriveConnectionRule driveConnection, String name, String contents)
			throws UnsupportedEncodingException, IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(driveConnection.getDrive());
		File file = createFileOperation.createFile(driveConnection.getApplicationFolderId(), name, MESFAVORIS_MIME_TYPE,
				contents.getBytes("UTF-8"), new NullProgressMonitor());
		return file;
	}

	private void share(GDriveConnectionRule driveConnection, String fileId, String userEmail) throws IOException {
		ShareFileOperation shareFileOperation = new ShareFileOperation(driveConnection.getDrive());
		shareFileOperation.shareWithUser(fileId, userEmail, true);
	}

}
