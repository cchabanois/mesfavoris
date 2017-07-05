package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.resource.ImageDescriptor;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class TextEditorBookmarkLabelProviderTest {
	private TextEditorBookmarkLabelProvider labelProvider;

	@Before
	public void setUp() {
		labelProvider = new TextEditorBookmarkLabelProvider();
	}
	
	@Test
	public void testGetImageFromBookmarkWithUndefinedPlaceholder() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_FILE_PATH, "${PLACEHOLDER}/myFile.txt"));
		
		// When
		ImageDescriptor image = labelProvider.getImageDescriptor(null, bookmark);
		
		// Then
		assertNotNull(image);
	}

}
