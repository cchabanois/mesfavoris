package mesfavoris.gdrive.connection;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mesfavoris.remote.UserInfo;

public class UserInfoPersisterTest {
	private UserInfoPersister gDriveUserInfoPersister;
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Before
	public void setUp() throws IOException {
		gDriveUserInfoPersister = new UserInfoPersister(temporaryFolder.newFile("user.json"));
	}
	
	@Test
	public void testSaveGDriveUser() throws IOException {
		// Given
		UserInfo user = new UserInfo("user@gmail.com", "Mr User");
		
		// When
		gDriveUserInfoPersister.saveUser(user, new NullProgressMonitor());
		UserInfo loadedUser = gDriveUserInfoPersister.loadUser(new NullProgressMonitor());
		
		// Then
		assertEquals(user.getDisplayName(), loadedUser.getDisplayName());
		assertEquals(user.getEmailAddress(), loadedUser.getEmailAddress());
	}
	
}
