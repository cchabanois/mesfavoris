package mesfavoris.bookmarktype;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.Bookmark;

public interface IBookmarkMarkerAttributesProvider {

	/**
	 * Get descriptor to create a {@link IMarker} from a {@link Bookmark}
	 * 
	 * @param bookmark
	 * @param monitor
	 * @return
	 */
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark, IProgressMonitor monitor);

}
