package mesfavoris.bookmarktype;

import java.util.List;

public interface IBookmarkPropertyDescriptors {

	BookmarkPropertyDescriptor getPropertyDescriptor(String propertyName);
	
	List<BookmarkPropertyDescriptor> getPropertyDescriptors();

}