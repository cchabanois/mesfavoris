package mesfavoris.gdrive.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Revision;

import mesfavoris.gdrive.operations.CreateFileOperation;
import mesfavoris.gdrive.operations.DownloadFileOperation;
import mesfavoris.gdrive.operations.GetFileMetadataOperation;
import mesfavoris.gdrive.operations.UpdateFileOperation;
import mesfavoris.gdrive.test.GDriveConnectionRule;

public class DownloadFileOperationTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionRule = new GDriveConnectionRule(true);

	@Test
	public void testDownloadFile() throws Exception {
		// Given
		File file = createFile("myFile.txt", "the contents");
		IProgressMonitor monitor = mock(IProgressMonitor.class);
		DownloadFileOperation downloadFileOperation = new DownloadFileOperation(gdriveConnectionRule.getDrive());

		// When
		byte[] contents = downloadFileOperation.downloadFile(file.getId(), monitor);

		// Then
		assertEquals("the contents", new String(contents, "UTF-8"));
		verify(monitor).beginTask(anyString(), anyInt());
		verify(monitor, atLeast(1)).worked(anyInt());
		verify(monitor, atLeast(1)).done();
	}

	@Test
	public void testETagIsNotTheSameWhenExecuteMedia() throws Exception {
		// Given
		File file = createFile("myFile.txt", "the contents");

		// When
		HttpResponse responseExecute = gdriveConnectionRule.getDrive().files().get(file.getId()).executeUnparsed();
		// when using the preferred method to download a file, URL parameter
		// "alt" is set to "media"
		HttpResponse responseExecuteMedia = gdriveConnectionRule.getDrive().files().get(file.getId()).executeMedia();

		// Then
		// unfortunately ...
		assertNotEquals(responseExecute.getHeaders().getETag(), responseExecuteMedia.getHeaders().getETag());

	}

	@Test
	public void testExecuteMediaETagIsNotRevisionETag() throws Exception {
		// Given
		File file = createFile("myFile.txt", "the contents");
		Revision revision = gdriveConnectionRule.getDrive().revisions().get(file.getId(), "head").execute();
		
		// When
		HttpResponse responseExecuteMedia = gdriveConnectionRule.getDrive().files().get(file.getId()).executeMedia();
		
		// Then
		assertNotEquals(revision.getEtag(), responseExecuteMedia.getHeaders().getETag());
	}

	@Test
	public void testDownloadUsingFileDownloadUrl() throws Exception {
		// Given
		File file = createFile("myFile.txt", "the contents");
		updateFile(file.getId(), "the new contents");

		// When
		byte[] contentsAsBytes = download(file.getDownloadUrl());

		// Then
		// this downloaded the latest version, not the one we expected ...
		assertEquals("the new contents", new String(contentsAsBytes, "UTF-8"));
	}

	@Test
	public void testDownloadUsingRevisionDownloadUrl() throws Exception {
		// Given
		File file = createFile("myFile.txt", "the contents");
		Revision revision = gdriveConnectionRule.getDrive().revisions().get(file.getId(), "head").execute();
		updateFile(file.getId(), "the new contents");

		// When
		byte[] contentsAsBytes = download(revision.getDownloadUrl());

		// Then
		assertEquals("the contents", new String(contentsAsBytes, "UTF-8"));
		// etag not the same for revision and file ...
		assertNotEquals(file.getEtag(), revision.getEtag());
	}

	private File createFile(String name, String contents) throws UnsupportedEncodingException, IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(gdriveConnectionRule.getDrive());
		File file = createFileOperation.createFile(gdriveConnectionRule.getApplicationFolderId(), name,
				contents.getBytes("UTF-8"), new NullProgressMonitor());
		return file;
	}

	private File getFileMetadata(String fileId) throws IOException {
		GetFileMetadataOperation getFileMetadataOperation = new GetFileMetadataOperation(
				gdriveConnectionRule.getDrive());
		return getFileMetadataOperation.getFileMetadata(fileId);
	}

	private File updateFile(String fileId, String newContents) throws UnsupportedEncodingException, IOException {
		UpdateFileOperation updateFileOperation = new UpdateFileOperation(gdriveConnectionRule.getDrive());
		return updateFileOperation.updateFile(fileId, newContents.getBytes("UTF-8"), null, new NullProgressMonitor());
	}

	private byte[] download(String downloadUrl) throws IOException {
		HttpRequest get = gdriveConnectionRule.getDrive().getRequestFactory()
				.buildGetRequest(new GenericUrl(downloadUrl));
		HttpResponse response = get.execute();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		response.download(baos);
		return baos.toByteArray();
	}

}
