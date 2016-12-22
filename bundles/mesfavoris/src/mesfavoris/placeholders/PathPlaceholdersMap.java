package mesfavoris.placeholders;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PathPlaceholdersMap implements IPathPlaceholders {
	private final Map<String, PathPlaceholder> map = new HashMap<>();
	
	@Override
	public Iterator<PathPlaceholder> iterator() {
		return map.values().iterator();
	}

	@Override
	public PathPlaceholder get(String name) {
		return map.get(name);
	}

	public void add(PathPlaceholder pathPlaceholder) {
		map.put(pathPlaceholder.getName(), pathPlaceholder);
	}
	
	public void remove(String name) {
		map.remove(name);
	}
	
}
