package mesfavoris.perforce;

import static mesfavoris.perforce.PerforceBookmarkProperties.PROP_CHANGELIST;
import static mesfavoris.perforce.PerforceBookmarkProperties.PROP_PORT;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;

public class ChangelistBookmarkLocationProvider implements IBookmarkLocationProvider {

	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		String p4Port = bookmark.getPropertyValue(PROP_PORT);
		if (p4Port == null) {
			return null;
		}
		String changeListAsString = bookmark.getPropertyValue(PROP_CHANGELIST);
		if (changeListAsString == null) {
			return null;
		}
		int changeListId;
		try {
			changeListId = Integer.parseInt(changeListAsString);
		} catch (NumberFormatException e) {
			return null;
		}
		return getChangelistBookmarkLocation(p4Port, changeListId);
	}

	private ChangelistBookmarkLocation getChangelistBookmarkLocation(String p4Port, int changeListId) {
		for (IP4Connection connection : P4ConnectionManager.getManager().getConnections()) {
			if (p4Port.equalsIgnoreCase(connection.getParameters().getPort())) {
				IP4Changelist changelist = connection.getChangelistById(changeListId);
				if (changelist != null) {
					return new ChangelistBookmarkLocation(connection, changelist);
				}
			}
		}
		return null;
	}

}
