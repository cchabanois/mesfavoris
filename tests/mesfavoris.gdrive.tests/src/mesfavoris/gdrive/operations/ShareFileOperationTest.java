package mesfavoris.gdrive.operations;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.operations.DownloadHeadRevisionOperation.FileContents;
import mesfavoris.gdrive.test.GDriveConnectionRule;

public class ShareFileOperationTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionUser1 = new GDriveConnectionRule(GDriveTestUser.USER1, true);
	
	@Rule
	public GDriveConnectionRule gdriveConnectionUser2 = new GDriveConnectionRule(GDriveTestUser.USER2, true);

	@Test
	public void testShareFile() throws Exception {
		// Given
		File file = createFile(gdriveConnectionUser1, "myFile.txt", "the contents");
		ShareFileOperation shareFileOperation = new ShareFileOperation(gdriveConnectionUser1.getDrive());
		
		// When
		shareFileOperation.shareWithUser(file.getId(), GDriveTestUser.USER2.getEmail(), true);
		
		// Then
		byte[] contentsAsBytes = downloadHeadRevision(gdriveConnectionUser2, file.getId());
		assertEquals("the contents", new String(contentsAsBytes, "UTF-8"));
	}
	
	
	private File createFile(GDriveConnectionRule driveConnection, String name, String contents) throws UnsupportedEncodingException, IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(driveConnection.getDrive());
		File file = createFileOperation.createFile(driveConnection.getApplicationFolderId(), name,
				contents.getBytes("UTF-8"), new NullProgressMonitor());
		return file;
	}	
	
	private byte[] downloadHeadRevision(GDriveConnectionRule driveConnection, String fileId) throws IOException {
		DownloadHeadRevisionOperation operation = new DownloadHeadRevisionOperation(driveConnection.getDrive());
		FileContents contents = operation.downloadFile(fileId, new NullProgressMonitor());
		return contents.getFileContents();
	}	
	
}
