package mesfavoris.gdrive.mappings;

import java.util.Map;

import com.google.api.services.drive.model.File;

import mesfavoris.model.BookmarksTree;

public interface IBookmarkMappingPropertiesProvider {

	/**
	 * Get the properties for the mapping
	 * 
	 * @param file
	 *            gdrive file containg the bookmarks
	 * @param bookmarksTree
	 *            the contents of the file
	 * @return the mapping properties
	 */
	Map<String, String> getBookmarkMappingProperties(File file, BookmarksTree bookmarksTree);

}