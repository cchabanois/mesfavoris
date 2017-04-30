package mesfavoris.internal.bookmarktypes.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor;
import mesfavoris.bookmarktype.BookmarkPropertyDescriptor.BookmarkPropertyType;
import mesfavoris.bookmarktype.IBookmarkPropertyDescriptors;
import mesfavoris.bookmarktype.IBookmarkPropertyObsolescenceSeverityProvider.ObsolescenceSeverity;
import mesfavoris.model.Bookmark;

public class BookmarkPropertyDescriptors implements IBookmarkPropertyDescriptors {

	private final Map<String, BookmarkPropertyDescriptor> propertyDescriptors = new HashMap<>();

	public BookmarkPropertyDescriptors() {
		addBookmarkPropertyDescriptor("filePath", BookmarkPropertyType.PATH, true, ObsolescenceSeverity.WARNING);
		addBookmarkPropertyDescriptor(Bookmark.PROPERTY_NAME, BookmarkPropertyType.STRING, false,
				ObsolescenceSeverity.IGNORE);
		addBookmarkPropertyDescriptor(Bookmark.PROPERTY_COMMENT, BookmarkPropertyType.STRING, false,
				ObsolescenceSeverity.IGNORE);
		addBookmarkPropertyDescriptor("lineNumber", BookmarkPropertyType.INT,
				true, ObsolescenceSeverity.INFO);
	}

	public BookmarkPropertyDescriptors addBookmarkPropertyDescriptor(BookmarkPropertyDescriptor propertyDescriptor) {
		propertyDescriptors.put(propertyDescriptor.getName(), propertyDescriptor);
		return this;
	}

	public BookmarkPropertyDescriptors addBookmarkPropertyDescriptor(String name, BookmarkPropertyType type,
			boolean updatable, ObsolescenceSeverity obsolescenceSeverity) {
		return addBookmarkPropertyDescriptor(new BookmarkPropertyDescriptor(name, type, updatable, "",
				(bookmark, propertyName, newValue) -> obsolescenceSeverity));
	}

	@Override
	public BookmarkPropertyDescriptor getPropertyDescriptor(String propertyName) {
		return propertyDescriptors.get(propertyName);
	}

	@Override
	public List<BookmarkPropertyDescriptor> getPropertyDescriptors() {
		return new ArrayList<>(propertyDescriptors.values());
	}

}
