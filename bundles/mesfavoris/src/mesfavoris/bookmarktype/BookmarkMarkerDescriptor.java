package mesfavoris.bookmarktype;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import mesfavoris.model.Bookmark;

/**
 * Descriptor used to create a {@link IMarker} for a {@link Bookmark}
 * 
 * @author cchabanois
 *
 */
public class BookmarkMarkerDescriptor {
	private final IResource resource;
	private final Map<String, ? extends Object> attributes;

	public BookmarkMarkerDescriptor(IResource resource, Map<String, ? extends Object> attributes) {
		this.resource = resource;
		this.attributes = attributes;
	}

	public Map<String, ? extends Object> getAttributes() {
		return attributes;
	}

	public IResource getResource() {
		return resource;
	}

}
