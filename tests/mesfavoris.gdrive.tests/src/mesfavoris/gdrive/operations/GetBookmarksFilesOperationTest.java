package mesfavoris.gdrive.operations;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.test.GDriveConnectionRule;

@Ignore
public class GetBookmarksFilesOperationTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionUser1 = new GDriveConnectionRule(GDriveTestUser.USER1, true);
	
	@Rule
	public GDriveConnectionRule gdriveConnectionUser2 = new GDriveConnectionRule(GDriveTestUser.USER2, true);
	
	
	@Test
	public void testGetSharedBookmarkFile() throws Exception {
		// Given
		File file = createFile(gdriveConnectionUser1, "user1File.txt", "the contents");
		share(gdriveConnectionUser1, file.getId(), GDriveTestUser.USER2.getEmail());
		GetBookmarkFilesOperation operation = new GetBookmarkFilesOperation(gdriveConnectionUser2.getDrive());
		
		// When
		List<File> bookmarkFiles = operation.getBookmarkFiles();
		
		// Then
		assertEquals(1, bookmarkFiles.size());
		assertEquals(file.getId(), bookmarkFiles.get(0).getId());
	}

	private File createFile(GDriveConnectionRule driveConnection, String name, String contents) throws UnsupportedEncodingException, IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(driveConnection.getDrive());
		File file = createFileOperation.createFile(driveConnection.getApplicationFolderId(), name,
				contents.getBytes("UTF-8"), new NullProgressMonitor());
		return file;
	}	
	
	private void share(GDriveConnectionRule driveConnection, String fileId, String userEmail) throws IOException {
		ShareFileOperation shareFileOperation = new ShareFileOperation(gdriveConnectionUser1.getDrive());
		shareFileOperation.shareWithUser(fileId,userEmail, true);
	}
	
}
