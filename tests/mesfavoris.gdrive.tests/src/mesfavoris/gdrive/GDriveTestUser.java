package mesfavoris.gdrive;

import java.util.Optional;

public enum GDriveTestUser {

	USER1("mesfavoris.test"),
	USER2("mesfavoris.test2");
	
	private final String userName;
	
	private GDriveTestUser(String userName) {
		this.userName = userName;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getEmail() {
		return userName+"@gmail.com";
	}
	
	public String getPassword() {
		String password = System.getenv(name() +"_GDRIVE_PASSWORD");
		if (password == null) {
			throw new IllegalStateException("Could not get test password from env for "+getEmail());
		}
		return password;
	}
	
	public Optional<String> getRecoveryEmail() {
		String recoveryEmail = System.getenv(name() +"_RECOVERY_EMAIL");
		return Optional.ofNullable(recoveryEmail);
	}
	
}
