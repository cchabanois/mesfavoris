package mesfavoris.model;

import java.util.Map;

import org.javimmutable.collections.tree.JImmutableTreeMap;

import com.google.common.collect.Maps;

public class BookmarkFolder extends Bookmark {
	
	public BookmarkFolder(BookmarkId id, String name) {
		super(id, singleValueMap(PROPERTY_NAME, name));
	}
	
	private static Map<String, String> singleValueMap(String name, String value) {
		Map<String, String> map = Maps.newHashMapWithExpectedSize(1);
		map.put(name, value);
		return map;
	}

	public BookmarkFolder(BookmarkId id, Map<String, String> properties) {
		super(id, properties);
	}

	BookmarkFolder(BookmarkId id, JImmutableTreeMap<String,String> properties) { 
		super(id, properties);
	}	
	
	@Override
	public String toString() {
		return "BookmarkFolder [id=" + id + ", name="+getPropertyValue(PROPERTY_NAME)+"]";
	}	
	
}
