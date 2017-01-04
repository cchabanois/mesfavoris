package mesfavoris.bookmarktype;

import java.util.List;

public interface IBookmarkPropertyDescriptors {

	/**
	 * Get descriptor for given bookmark property
	 * 
	 * @param propertyName
	 * @return
	 */
	BookmarkPropertyDescriptor getPropertyDescriptor(String propertyName);

	/**
	 * Get all property descriptors
	 * 
	 * @return
	 */
	List<BookmarkPropertyDescriptor> getPropertyDescriptors();

}