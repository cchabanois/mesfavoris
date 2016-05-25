package org.chabanois.mesfavoris.bookmarktype;

import java.util.Map;

public interface IBookmarkPropertiesProvider {

	public abstract void addBookmarkProperties(
			Map<String, String> bookmarkProperties, Object selected);

	
}