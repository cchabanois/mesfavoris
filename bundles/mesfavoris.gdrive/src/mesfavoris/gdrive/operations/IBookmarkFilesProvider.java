package mesfavoris.gdrive.operations;

import java.io.IOException;
import java.util.List;

import com.google.api.services.drive.model.File;

public interface IBookmarkFilesProvider {

	public List<File> getBookmarkFiles() throws IOException;
	
}