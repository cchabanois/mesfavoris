package mesfavoris.bookmarktype;

import mesfavoris.model.Bookmark;

/**
 * Provides the severity in case a property changed
 * 
 * @author cchabanois
 *
 */
public interface IBookmarkPropertyObsolescenceSeverityProvider {

	public enum ObsolescenceSeverity {
		// ignore the new value
		IGNORE,
		// only info, property does not really need to be updated
		INFO,
		// warning, property should be updated to the new value
		WARNING
	}

	public ObsolescenceSeverity getObsolescenceSeverity(Bookmark bookmark, String propertyName, String newValue);

}
