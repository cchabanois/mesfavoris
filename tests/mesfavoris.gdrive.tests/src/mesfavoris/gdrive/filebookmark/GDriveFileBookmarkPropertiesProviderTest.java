package mesfavoris.gdrive.filebookmark;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.operations.CreateFileOperation;
import mesfavoris.gdrive.test.GDriveConnectionRule;
import mesfavoris.model.Bookmark;
import mesfavoris.url.UrlBookmarkProperties;

public class GDriveFileBookmarkPropertiesProviderTest {
	private GDriveFileBookmarkPropertiesProvider propertiesProvider;
	private Map<String, String> bookmarkProperties = new HashMap<>();

	@Rule
	public GDriveConnectionRule gdriveConnectionRule = new GDriveConnectionRule(GDriveTestUser.USER1, true);

	@Before
	public void setUp() {
		propertiesProvider = new GDriveFileBookmarkPropertiesProvider(gdriveConnectionRule.getGDriveConnectionManager());
	}

	@Test
	public void testAddBookmarkProperties() throws Exception {
		// Given
		File file = createFile("myFile.pdf", "application/pdf", "Supposed to be pdf content");
		URL url = new URL("https://drive.google.com/open?id="+file.getId());
		ISelection selection = new StructuredSelection(url);

		// When
		propertiesProvider.addBookmarkProperties(bookmarkProperties, null, selection, new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties.get(GDriveBookmarkProperties.PROP_FILE_ID)).isEqualTo(file.getId());
		assertThat(bookmarkProperties.get(UrlBookmarkProperties.PROP_ICON)).isNotEmpty();
		assertThat(bookmarkProperties.get(Bookmark.PROPERTY_NAME)).isEqualTo("myFile.pdf");
	}

	private File createFile(String name, String mimeType, String contents) throws IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(gdriveConnectionRule.getDrive());
		byte[] bytes = contents.getBytes("UTF-8");
		File file = createFileOperation.createFile(gdriveConnectionRule.getApplicationFolderId(), name, mimeType, bytes,
				new NullProgressMonitor());
		return file;
	}
}
