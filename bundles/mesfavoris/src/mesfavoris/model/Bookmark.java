package mesfavoris.model;

import java.util.Map;

import org.javimmutable.collections.tree.JImmutableTreeMap;

import com.google.common.base.Preconditions;

public class Bookmark {
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_COMMENT = "comment";
	public static final String PROPERTY_CREATED = "created";
	
	protected final BookmarkId id;	
	protected final JImmutableTreeMap<String, String> properties;
	
	public Bookmark(BookmarkId id) {
		Preconditions.checkNotNull(id);
		this.id = id;
		this.properties = JImmutableTreeMap.<String, String>of();
	}
	
	public Bookmark(BookmarkId id, Map<String, String> properties) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(properties);
		this.id = id;
		this.properties = JImmutableTreeMap.<String, String>of(properties);
	}
	
	protected Bookmark(BookmarkId id, JImmutableTreeMap<String,String> properties) { 
		this.id = id;
		this.properties = properties;
	}
	
	public BookmarkId getId() {
		return id;
	}
	
	public String getPropertyValue(String propertyName) {
		String value = properties.get(propertyName);
		return value;
	}
	
	public Map<String,String> getProperties() {
		return properties.getMap();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Bookmark other = (Bookmark) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Bookmark [");
		sb.append("id="+id);
		for (Map.Entry<String, String> entry : getProperties().entrySet()) {
			sb.append(", ").append(entry.getKey()).append("=").append(entry.getValue());
		}
		sb.append(']');
		return sb.toString();
	}
	
}
