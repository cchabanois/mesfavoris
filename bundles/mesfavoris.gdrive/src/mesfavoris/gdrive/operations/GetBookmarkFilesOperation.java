package mesfavoris.gdrive.operations;

import java.io.IOException;
import java.util.List;

import com.google.api.client.util.Lists;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class GetBookmarkFilesOperation extends AbstractGDriveOperation {

	public GetBookmarkFilesOperation(Drive drive) {
		super(drive);
	}

	public List<File> getBookmarkFiles() throws IOException {
		List<File> files = Lists.newArrayList();
		String pageToken = null;
		do {
		    FileList result = drive.files().list()
		            .setQ("mimeType='"+MIME_TYPE+"' and trashed=false")
		            .setSpaces("drive")
		            .setPageToken(pageToken)
		            .execute();
		    files.addAll(result.getItems());
		    pageToken = result.getNextPageToken();
		} while (pageToken != null);
		return files;
	}
	
}
