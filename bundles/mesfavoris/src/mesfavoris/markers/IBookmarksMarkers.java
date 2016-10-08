package mesfavoris.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.BookmarkId;

public interface IBookmarksMarkers {

	IMarker findMarker(BookmarkId bookmarkId, IProgressMonitor monitor);

	void refreshMarker(BookmarkId bookmarkId, IProgressMonitor monitor);

}