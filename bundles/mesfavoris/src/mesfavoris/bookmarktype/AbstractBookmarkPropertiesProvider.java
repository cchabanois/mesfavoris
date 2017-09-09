package mesfavoris.bookmarktype;

import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractBookmarkPropertiesProvider implements IBookmarkPropertiesProvider {

	protected void putIfAbsent(Map<String, String> bookmarkProperties, String name, String value) {
		putIfAbsent(bookmarkProperties, name, () -> value);
	}

	protected boolean isPresent(Map<String, String> bookmarkProperties, String name) {
		return bookmarkProperties.containsKey(name);
	}
	
	protected void putIfAbsent(Map<String, String> bookmarkProperties, String name, Supplier<String> valueProvider) {
		if (bookmarkProperties.containsKey(name)) {
			return;
		}
		String value = valueProvider.get();
		if (value != null) {
			bookmarkProperties.put(name, value);
		}
	}

}
