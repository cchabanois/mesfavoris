package mesfavoris.gdrive.operations;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.Charsets;
import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.test.GDriveConnectionRule;

public class UpdateFileOperationTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionRule = new GDriveConnectionRule(GDriveTestUser.USER1, true);

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testUpdateFile() throws Exception {
		// Given
		File file = createFile("myFile.txt", "the contents");
		UpdateFileOperation updateFileOperation = new UpdateFileOperation(gdriveConnectionRule.getDrive());

		// When
		updateFileOperation.updateFile(file.getId(), "the new contents".getBytes(Charsets.UTF_8), file.getEtag(),
				new NullProgressMonitor());

		// Then
		assertEquals("the new contents", new String(downloadFile(file.getId()), Charsets.UTF_8));
	}

	@Test
	public void testCannotUpdateFileThatHasBeenUpdatedSince() throws Exception {
		// Given
		File file = createFile("myFile.txt", "the contents");
		UpdateFileOperation updateFileOperation = new UpdateFileOperation(gdriveConnectionRule.getDrive());
		updateFileOperation.updateFile(file.getId(), "the new contents".getBytes(Charsets.UTF_8), file.getEtag(),
				new NullProgressMonitor());
		exception.expect(GoogleJsonResponseException.class);
		exception.expectMessage("412 Precondition Failed");

		// When
		updateFileOperation.updateFile(file.getId(), "the newest contents".getBytes(Charsets.UTF_8), file.getEtag(),
				new NullProgressMonitor());

		// Then
	}

	private File createFile(String name, String contents) throws UnsupportedEncodingException, IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(gdriveConnectionRule.getDrive());
		File file = createFileOperation.createFile(gdriveConnectionRule.getApplicationFolderId(), name,
				contents.getBytes("UTF-8"), new NullProgressMonitor());
		return file;
	}

	private byte[] downloadFile(String fileId) throws IOException {
		DownloadFileOperation downloadFileOperation = new DownloadFileOperation(gdriveConnectionRule.getDrive());
		return downloadFileOperation.downloadFile(fileId, new NullProgressMonitor());
	}

}
