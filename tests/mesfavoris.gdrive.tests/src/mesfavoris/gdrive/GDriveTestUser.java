package mesfavoris.gdrive;

import java.util.Optional;

public enum GDriveTestUser {

	USER1,
	USER2;
	
	public String getUserName() {
		String userName = System.getenv(name()+"_GDRIVE_USERNAME");
		if (userName == null) {
			throw new IllegalStateException("Could not get test username from env for "+name());
		}
		return userName;
	}
	
	public String getEmail() {
		return getUserName()+"@gmail.com";
	}
	
	public String getPassword() {
		String password = System.getenv(name() +"_GDRIVE_PASSWORD");
		if (password == null) {
			throw new IllegalStateException("Could not get test password from env for "+name());
		}
		return password;
	}
	
	public Optional<String> getRecoveryEmail() {
		String recoveryEmail = System.getenv(name() +"_RECOVERY_EMAIL");
		return Optional.ofNullable(recoveryEmail);
	}
	
}
