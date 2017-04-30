package mesfavoris.texteditor.internal;

import mesfavoris.bookmarktype.IBookmarkPropertyObsolescenceSeverityProvider;
import mesfavoris.model.Bookmark;

public class LineNumberObsolescenceSeverityProvider implements IBookmarkPropertyObsolescenceSeverityProvider {

	@Override
	public ObsolescenceSeverity getObsolescenceSeverity(Bookmark bookmark, String propertyName, String newValue) {
		String oldValue = bookmark.getPropertyValue(propertyName);
		if (oldValue == null || newValue == null) {
			// beginning of the file
			return ObsolescenceSeverity.IGNORE;
		}
		int oldLineNumber = Integer.parseInt(oldValue);
		int newLineNumber = Integer.parseInt(newValue);
		if (Math.abs(oldLineNumber - newLineNumber) > 10) {
			return ObsolescenceSeverity.WARNING;
		} else {
			return ObsolescenceSeverity.INFO;
		}
	}

}
