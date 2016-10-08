package mesfavoris.java;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.JavaCore;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class JavaMarkerBookmarkLocation implements IBookmarkLocation {
	/**
	 * Name of the handle id attribute in a Java marker.
	 * 
	 * @see JavaCore
	 */
	public static final String ATT_HANDLE_ID = "org.eclipse.jdt.internal.core.JavaModelManager.handleId";

	
	private final IMarker marker;

	public JavaMarkerBookmarkLocation(IMarker marker) {
		this.marker = marker;
	}

	public IMarker getMarker() {
		return marker;
	}

	public String getHandle() {
		return marker.getAttribute(JavaMarkerBookmarkLocation.ATT_HANDLE_ID, null);
	}
	
}
