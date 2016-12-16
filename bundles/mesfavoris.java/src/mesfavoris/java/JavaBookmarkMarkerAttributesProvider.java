package mesfavoris.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;

import mesfavoris.bookmarktype.AbstractBookmarkMarkerPropertiesProvider;
import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.model.Bookmark;

public class JavaBookmarkMarkerAttributesProvider extends AbstractBookmarkMarkerPropertiesProvider {

	private final JavaTypeMemberBookmarkLocationProvider locationProvider;

	public JavaBookmarkMarkerAttributesProvider() {
		this(new JavaTypeMemberBookmarkLocationProvider());
	}

	public JavaBookmarkMarkerAttributesProvider(JavaTypeMemberBookmarkLocationProvider locationProvider) {
		this.locationProvider = locationProvider;
	}

	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark, IProgressMonitor monitor) {
		JavaTypeMemberBookmarkLocation location = locationProvider.getBookmarkLocation(bookmark, monitor);
		if (location == null) {
			return null;
		}
		if (location.getLineNumber() == null) {
			return null;
		}
		Map attributes = new HashMap();
		JavaCore.addJavaElementMarkerAttributes(attributes, location.getMember());
		attributes.put(IMarker.LINE_NUMBER, new Integer(location.getLineNumber() + 1));
		if (location.getLineOffset() != null) {
			attributes.put(IMarker.CHAR_START, Integer.valueOf(location.getLineOffset()));
			attributes.put(IMarker.CHAR_END, Integer.valueOf(location.getLineOffset()));
		}
		getMessage(bookmark).ifPresent(message->attributes.put(IMarker.MESSAGE, message));
		IResource resource = getMarkerResource(location.getMember());
		return new BookmarkMarkerDescriptor(resource, attributes);
	}

	private IResource getMarkerResource(IMember member) {
		ICompilationUnit cu = member.getCompilationUnit();
		if (cu != null && cu.isWorkingCopy()) {
			member = (IMember) member.getPrimaryElement();
		}
		IResource res = member.getResource();
		if (res == null || !res.getProject().exists()) {
			res = ResourcesPlugin.getWorkspace().getRoot();
		}
		return res;
	}

}
