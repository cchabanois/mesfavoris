package mesfavoris.gdrive.dialogs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.services.drive.model.File;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.gdrive.operations.CreateFileOperation;
import mesfavoris.gdrive.operations.GetBookmarkFilesOperation;
import mesfavoris.gdrive.operations.ShareFileOperation;
import mesfavoris.gdrive.test.GDriveConnectionRule;
import mesfavoris.tests.commons.ui.AbstractDialogTest;

public class ImportBookmarksFileDialogTest extends AbstractDialogTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionUser1 = new GDriveConnectionRule(GDriveTestUser.USER1, true);

	@Rule
	public GDriveConnectionRule gdriveConnectionUser2 = new GDriveConnectionRule(GDriveTestUser.USER2, true);

	private IBookmarkMappings bookmarkMappings = mock(IBookmarkMappings.class);
	private ImportBookmarksFileDialog dialog;

	@Before
	public void setUp() {
		when(bookmarkMappings.getMapping(anyString())).thenReturn(Optional.empty());
	}

	@Test
	public void testSelectBookmarksFile() throws Exception {
		// Given
		File file = createFile(gdriveConnectionUser1, "bookmarks1", "any");
		openDialog(shell -> createDialog(shell, gdriveConnectionUser1,
				Optional.of(gdriveConnectionUser1.getApplicationFolderId())));

		// When
		SWTBotTable botTable = bot.table();
		assertEquals(1, botTable.rowCount());
		botTable.select(0);
		clickOkButton();

		// Then
		assertEquals(file.getId(), dialog.getFile().getId());
	}

	@Test
	public void testAddLinkAndSelectBookmarksFile() throws Exception {
		// Given
		File file = createFile(gdriveConnectionUser2, "bookmarks from user2", "any");
		shareWithAnyoneWithLink(gdriveConnectionUser2, file.getId());
		openDialog(shell -> createDialog(shell, gdriveConnectionUser1,
				Optional.of(gdriveConnectionUser1.getApplicationFolderId())));

		// When
		SWTBotTable botTable = bot.table();
		assertEquals(0, botTable.rowCount());
		addLink(file.getAlternateLink());
		botTable.getTableItem(0).select();
		clickOkButton();

		// Then
		assertEquals(file.getId(), dialog.getFile().getId());
	}

	private void addLink(String link) {
		bot.button("Add link...").click();
		bot.shell("Add a link to a gdrive bookmarks file").activate();
		bot.text().setText(link);
		bot.button("OK").click();
	}

	private File createFile(GDriveConnectionRule driveConnection, String name, String contents)
			throws UnsupportedEncodingException, IOException {
		CreateFileOperation createFileOperation = new CreateFileOperation(driveConnection.getDrive());
		File file = createFileOperation.createFile(driveConnection.getApplicationFolderId(), name,
				contents.getBytes("UTF-8"), new NullProgressMonitor());
		return file;
	}

	private ImportBookmarksFileDialog createDialog(Shell shell, GDriveConnectionRule driveConnection,
			Optional<String> folderId) {
		dialog = new ImportBookmarksFileDialog(shell, driveConnection.getDrive(),
				driveConnection.getApplicationFolderId(), bookmarkMappings,
				new GetBookmarkFilesOperation(driveConnection.getDrive(), folderId));
		return dialog;
	}

	private void shareWithAnyoneWithLink(GDriveConnectionRule driveConnection, String fileId) throws IOException {
		ShareFileOperation shareFileOperation = new ShareFileOperation(driveConnection.getDrive());
		shareFileOperation.shareWithAnyone(fileId, false, true);
	}

}
