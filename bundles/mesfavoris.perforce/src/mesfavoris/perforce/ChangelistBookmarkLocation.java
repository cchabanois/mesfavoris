package mesfavoris.perforce;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class ChangelistBookmarkLocation implements IBookmarkLocation {

	private final IP4Connection connection;
	private final IP4Changelist changelist;

	public ChangelistBookmarkLocation(IP4Connection connection, IP4Changelist changelist) {
		this.connection = connection;
		this.changelist = changelist;
	}

	public IP4Changelist getChangelist() {
		return changelist;
	}

	public IP4Connection getConnection() {
		return connection;
	}

}
