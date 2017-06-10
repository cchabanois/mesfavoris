package mesfavoris.gdrive.operations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.operations.DownloadHeadRevisionOperation.FileContents;
import mesfavoris.gdrive.test.GDriveConnectionRule;

public class DownloadHeadRevisionOperationTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionRule = new GDriveConnectionRule(GDriveTestUser.USER1, true);

	@Test
	public void testDownloadFile() throws Exception {
		// Given
		File file = createTextFile("myFile.txt", "the contents");
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		DownloadHeadRevisionOperation downloadFileOperation = new DownloadHeadRevisionOperation(
				gdriveConnectionRule.getDrive());

		// When
		FileContents contents = downloadFileOperation.downloadFile(file.getId(), monitor);

		// Then
		assertEquals("the contents", new String(contents.getFileContents(), "UTF-8"));
		// don't assert etag because sometimes it changes just after creation
		// assertEquals(file.getEtag(), contents.getFile().getEtag());
		verify(monitor).beginTask(anyString(), anyInt());
		verify(monitor, atLeast(1)).done();
	}

	private File createTextFile(String name, String contents) throws UnsupportedEncodingException, IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(gdriveConnectionRule.getDrive());
		File file = createFileOperation.createFile(gdriveConnectionRule.getApplicationFolderId(), name, "text/plain",
				contents.getBytes("UTF-8"), new NullProgressMonitor());
		return file;
	}

}
