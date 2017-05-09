package mesfavoris.bookmarktype;

import java.util.Map;

import mesfavoris.model.Bookmark;

/**
 * Descriptor for a {@link Bookmark} property
 * 
 * @author cchabanois
 *
 */
public class BookmarkPropertyDescriptor implements IBookmarkPropertyObsolescenceSeverityProvider {
	private final String name;
	private final BookmarkPropertyType type;
	private final boolean updatable;
	private final String description;
	private final IBookmarkPropertyObsolescenceSeverityProvider bookmarkPropertyObsolescenceSeverityProvider;

	public enum BookmarkPropertyType {
		PATH, STRING, INT, INSTANT
	}

	public BookmarkPropertyDescriptor(String name, BookmarkPropertyType type, boolean updatable, String description,
			IBookmarkPropertyObsolescenceSeverityProvider bookmarkPropertyObsolescenceSeverityProvider) {
		this.name = name;
		this.type = type;
		this.updatable = updatable;
		this.description = description;
		this.bookmarkPropertyObsolescenceSeverityProvider = bookmarkPropertyObsolescenceSeverityProvider;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public BookmarkPropertyType getType() {
		return type;
	}

	@Override
	public ObsolescenceSeverity getObsolescenceSeverity(Bookmark bookmark, Map<String, String> obsoleteProperties, String propertyName) {
		return bookmarkPropertyObsolescenceSeverityProvider.getObsolescenceSeverity(bookmark, obsoleteProperties, propertyName);
	}

	public boolean isUpdatable() {
		return updatable;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + (updatable ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookmarkPropertyDescriptor other = (BookmarkPropertyDescriptor) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		if (updatable != other.updatable)
			return false;
		return true;
	}

}
