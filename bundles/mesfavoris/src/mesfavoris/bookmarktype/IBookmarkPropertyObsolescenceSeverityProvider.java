package mesfavoris.bookmarktype;

import java.util.Map;

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

	/**
	 * Get the obsolescence severity for a given property
	 * @param bookmark
	 * @param obsoleteProperties
	 *            key is the name of the obsolete property, value is the new
	 *            value
	 * @param propertyName
	 *            the property name
	 * @return the severity
	 */
	public ObsolescenceSeverity getObsolescenceSeverity(Bookmark bookmark, Map<String, String> obsoleteProperties,
			String propertyName);

}
