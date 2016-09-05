package mesfavoris.gdrive.mappings;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IBookmarkMappingsPersister {

	Set<BookmarkMapping> load() throws IOException;

	void save(Set<BookmarkMapping> mappings, IProgressMonitor monitor) throws IOException;

}