package org.chabanois.mesfavoris.bookmarktype;

import java.util.Map;

import org.eclipse.core.resources.IResource;

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
