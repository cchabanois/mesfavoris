package mesfavoris.remote;

public class UserInfo {
	private final String displayName;
	private final String emailAddress;
	
	public UserInfo(String emailAddress, String displayName) {
		this.displayName = displayName;
		this.emailAddress = emailAddress;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}

	@Override
	public String toString() {
		return emailAddress != null ? emailAddress : displayName;
	}
	
	
	
	  
}
