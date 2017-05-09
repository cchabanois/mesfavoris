package mesfavoris.texteditor.internal;

import java.util.Map;

import mesfavoris.bookmarktype.IBookmarkPropertyObsolescenceSeverityProvider;
import mesfavoris.model.Bookmark;

public class LineNumberObsolescenceSeverityProvider implements IBookmarkPropertyObsolescenceSeverityProvider {

	public static final int DISTANCE_LIMIT = 10;

	@Override
	public ObsolescenceSeverity getObsolescenceSeverity(Bookmark bookmark, Map<String, String> obsoleteProperties,
			String propertyName) {
		String oldValue = bookmark.getPropertyValue(propertyName);
		String newValue = obsoleteProperties.get(propertyName);
		if (oldValue == null || newValue == null) {
			// beginning of the file
			return ObsolescenceSeverity.IGNORE;
		}
		if (areTooFar(oldValue, newValue)) {
			return ObsolescenceSeverity.WARNING;
		} else {
			return ObsolescenceSeverity.INFO;
		}
	}

	private boolean areTooFar(String oldValue, String newValue) {
		try {
			int oldLineNumber = Integer.parseInt(oldValue);
			int newLineNumber = Integer.parseInt(newValue);
			return Math.abs(oldLineNumber - newLineNumber) > DISTANCE_LIMIT;
		} catch (NumberFormatException e) {
			return true;
		}
	}

}
