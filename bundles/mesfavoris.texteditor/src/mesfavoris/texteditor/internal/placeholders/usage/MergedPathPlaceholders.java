package mesfavoris.texteditor.internal.placeholders.usage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import mesfavoris.texteditor.placeholders.IPathPlaceholders;
import mesfavoris.texteditor.placeholders.PathPlaceholder;

public class MergedPathPlaceholders implements IPathPlaceholders {
	private final List<IPathPlaceholders> pathPlaceholdersList;
	
	public MergedPathPlaceholders(IPathPlaceholders... pathPlaceholders) {
		this.pathPlaceholdersList = Lists.newArrayList(pathPlaceholders);
	}
	
	@Override
	public Iterator<PathPlaceholder> iterator() {
		Map<String, PathPlaceholder> map = new HashMap<>();
		for (IPathPlaceholders pathPlaceholders : pathPlaceholdersList) {
			for (PathPlaceholder pathPlaceholder : pathPlaceholders) {
				map.put(pathPlaceholder.getName(), pathPlaceholder);
			}
		}
		return map.values().iterator();
	}

	@Override
	public PathPlaceholder get(String name) {
		PathPlaceholder pathPlaceholder = null;
		for (IPathPlaceholders pathPlaceholders : pathPlaceholdersList) {
			PathPlaceholder currentPathPlaceholder = pathPlaceholders.get(name);
			if (currentPathPlaceholder != null) {
				pathPlaceholder = currentPathPlaceholder;
			}
		}
		return pathPlaceholder;
	}

}
