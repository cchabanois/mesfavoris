package mesfavoris.bookmarktype;

import java.util.List;

public interface IBookmarkPropertyDescriptorProvider {

	BookmarkPropertyDescriptor getPropertyDescriptor(String propertyName);
	
	List<BookmarkPropertyDescriptor> getPropertyDescriptors();

}