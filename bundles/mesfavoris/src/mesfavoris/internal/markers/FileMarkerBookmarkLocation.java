package mesfavoris.internal.markers;

import org.eclipse.core.resources.IMarker;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class FileMarkerBookmarkLocation implements IBookmarkLocation {
	private final IMarker marker;

	public FileMarkerBookmarkLocation(IMarker marker) {
		this.marker = marker;
	}

	public IMarker getMarker() {
		return marker;
	}

}
