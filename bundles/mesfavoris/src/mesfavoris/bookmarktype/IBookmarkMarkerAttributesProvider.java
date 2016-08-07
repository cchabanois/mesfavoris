package mesfavoris.bookmarktype;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.model.Bookmark;

public interface IBookmarkMarkerAttributesProvider {
	
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark, IProgressMonitor monitor);
	
}
