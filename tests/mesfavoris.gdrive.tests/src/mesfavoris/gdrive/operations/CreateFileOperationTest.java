package mesfavoris.gdrive.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.test.GDriveConnectionRule;

public class CreateFileOperationTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionRule = new GDriveConnectionRule(GDriveTestUser.USER1, true);

	@Test
	public void testCreateFile() throws Exception {
		// Given
		CreateFileOperation createFileOperation = new CreateFileOperation(gdriveConnectionRule.getDrive());
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		byte[] contents = "the content".getBytes("UTF-8");

		// When
		File file = createFileOperation.createFile(gdriveConnectionRule.getApplicationFolderId(), "myFile.txt",
				contents, monitor);

		// Then
		assertNotNull(file);
		verify(monitor).beginTask(anyString(), anyInt());
		verify(monitor, atLeast(1)).worked(anyInt());
		verify(monitor, atLeast(1)).done();
		assertEquals("the content", new String(downloadFile(file.getId()), "UTF-8"));
	}

	private byte[] downloadFile(String fileId) throws IOException {
		DownloadFileOperation downloadFileOperation = new DownloadFileOperation(gdriveConnectionRule.getDrive());
		return downloadFileOperation.downloadFile(fileId, new NullProgressMonitor());
	}

}
