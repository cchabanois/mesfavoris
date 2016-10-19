package mesfavoris.gdrive.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.common.collect.Sets;

import mesfavoris.gdrive.GDriveTestUser;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.mappings.BookmarkMapping;
import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.gdrive.test.GDriveConnectionRule;
import mesfavoris.gdrive.test.IAuthorizationListener;
import mesfavoris.model.BookmarkId;

public class DeleteFileDataStoreOperationTest {

	@Rule
	public GDriveConnectionRule gdriveConnectionRule = new GDriveConnectionRule(GDriveTestUser.USER1, true);

	private DeleteFileDataStoreOperation operation;
	private IBookmarkMappings bookmarkMappings = mock(IBookmarkMappings.class);

	@Before
	public void setUp() {
		GDriveConnectionManager gdriveConnectionManager = gdriveConnectionRule.getGDriveConnectionManager();
		operation = new DeleteFileDataStoreOperation(gdriveConnectionManager.getDataStoreDir(), gdriveConnectionManager,
				bookmarkMappings);
	}

	@Test
	public void testCannotDeleteDataStoreIfConnected() throws IOException {
		// When
		Throwable thrown = catchThrowable(() -> {
			operation.deleteDefaultFileDataStore();
		});

		// Then
		assertThat(thrown).isInstanceOf(IOException.class).hasMessage("Cannot delete file store while connected");
	}

	@Test
	public void testCannotDeleteDataStoreIfMappingsExist() throws IOException {
		// Given
		gdriveConnectionRule.disconnect();
		when(bookmarkMappings.getMappings()).thenReturn(Sets
				.newHashSet(new BookmarkMapping(new BookmarkId("bookmarkFolderId"), "fileId", Collections.emptyMap())));

		// When
		Throwable thrown = catchThrowable(() -> {
			operation.deleteDefaultFileDataStore();
		});

		// Then
		assertThat(thrown).isInstanceOf(IOException.class).hasMessage("Cannot delete file store if there are mappings");
	}

	@Test
	public void testDeleteDataStore() throws Exception {
		// Given
		assertThat(dataStoreExists());
		gdriveConnectionRule.disconnect();
		IAuthorizationListener authorizationListener = mock(IAuthorizationListener.class);
		gdriveConnectionRule.setAuthorizationListener(authorizationListener);
		
		// When
		operation.deleteDefaultFileDataStore();

		// Then
		assertThat(!dataStoreExists());
		gdriveConnectionRule.connect();
		verify(authorizationListener).onAuthorization();
		assertThat(dataStoreExists());
	}

	private boolean dataStoreExists() {
		File file = new File(gdriveConnectionRule.getGDriveConnectionManager().getDataStoreDir(),
				StoredCredential.DEFAULT_DATA_STORE_ID);
		return file.exists();
	}

}
