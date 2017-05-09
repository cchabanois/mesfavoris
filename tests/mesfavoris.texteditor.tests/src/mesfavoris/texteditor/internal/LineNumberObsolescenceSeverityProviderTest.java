package mesfavoris.texteditor.internal;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static mesfavoris.texteditor.internal.LineNumberObsolescenceSeverityProvider.DISTANCE_LIMIT;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import mesfavoris.bookmarktype.IBookmarkPropertyObsolescenceSeverityProvider.ObsolescenceSeverity;
import mesfavoris.model.Bookmark;

public class LineNumberObsolescenceSeverityProviderTest {
	private LineNumberObsolescenceSeverityProvider severityProvider = new LineNumberObsolescenceSeverityProvider();
	
	@Test
	public void testInfoSeverityWhenLineNumberDidNotChangeTooMuch() {
		// Given
		Bookmark bookmark = bookmark("myBookmark").withProperty(PROP_LINE_NUMBER, ""+100).build();
		Map<String,String> obsoleteProperties = ImmutableMap.of(PROP_LINE_NUMBER, ""+(100+DISTANCE_LIMIT-1));
		
		// When
		ObsolescenceSeverity severity = severityProvider.getObsolescenceSeverity(bookmark, obsoleteProperties, PROP_LINE_NUMBER);
		
		// Then
		assertEquals(ObsolescenceSeverity.INFO, severity);
	}
	
	@Test
	public void testWarningSeverityWhenLineNumberChangesTooMuch() {
		// Given
		Bookmark bookmark = bookmark("myBookmark").withProperty(PROP_LINE_NUMBER, ""+100).build();
		Map<String,String> obsoleteProperties = ImmutableMap.of(PROP_LINE_NUMBER, ""+(100+DISTANCE_LIMIT+1));
		
		// When
		ObsolescenceSeverity severity = severityProvider.getObsolescenceSeverity(bookmark, obsoleteProperties, PROP_LINE_NUMBER);
		
		// Then
		assertEquals(ObsolescenceSeverity.WARNING, severity);
	}
	
}
