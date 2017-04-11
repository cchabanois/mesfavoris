package mesfavoris.gdrive.mappings;

import java.util.HashMap;
import java.util.Map;

import com.google.api.services.drive.model.File;

import mesfavoris.model.BookmarksTree;

public class BookmarkMappingPropertiesProvider implements IBookmarkMappingPropertiesProvider {

	/**
	 * Get the properties for the mapping
	 * 
	 * @param file
	 *            gdrive file containg the bookmarks
	 * @param bookmarksTree
	 *            the contents of the file
	 * @return the mapping properties
	 */
	@Override
	public Map<String, String> getBookmarkMappingProperties(File file, BookmarksTree bookmarksTree) {
		Map<String, String> properties = new HashMap<>();
		if (Boolean.FALSE.equals(file.getEditable())) {
			properties.put(BookmarkMapping.PROP_READONLY, Boolean.TRUE.toString());
		}
		if (file.getSharingUser() != null) {
			properties.put(BookmarkMapping.PROP_SHARING_USER, file.getSharingUser().getDisplayName());
		}
		properties.put(BookmarkMapping.PROP_BOOKMARKS_COUNT, Integer.toString(bookmarksTree.size()-1));
		return properties;
	}


}
