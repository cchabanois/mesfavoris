package mesfavoris.gdrive.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Changes;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;

public class GetChangesOperation extends AbstractGDriveOperation {

	public GetChangesOperation(Drive drive) {
		super(drive);
	}

	public Long getLargestChangeId() throws IOException {
		Changes.List request = drive.changes().list();
		request.setMaxResults(1);
		ChangeList changeList = request.execute();
		return changeList.getLargestChangeId();		
	}
	
	public List<Change> getChanges(Long startChangeId) throws IOException {
		List<Change> changes = new ArrayList<Change>();
		Changes.List request = drive.changes().list();
		if (startChangeId != null) {
			request.setStartChangeId(startChangeId);
		}
		do {
			ChangeList changeList = request.execute();
			changes.addAll(changeList.getItems());
			request.setPageToken(changeList.getNextPageToken());
		} while (request.getPageToken() != null && request.getPageToken().length() > 0);
		return changes;
	}
	
}
