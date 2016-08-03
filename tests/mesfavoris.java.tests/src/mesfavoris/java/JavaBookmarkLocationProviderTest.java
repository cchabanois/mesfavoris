package mesfavoris.java;

import static mesfavoris.java.JavaBookmarkProperties.KIND_METHOD;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_DECLARING_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_KIND;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_NAME;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Platform;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.java.JavaBookmarkLocationProvider.JavaEditorBookmarkLocation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class JavaBookmarkLocationProviderTest {
	private final JavaBookmarkLocationProvider javaBookmarkLocationProvider = new JavaBookmarkLocationProvider();

	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate("javaBookmarkLocationProviderTest", "commons-cli");
	}

	@Test
	public void testJavaTypeFindLocation() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROP_JAVA_TYPE, "org.apache.commons.cli.DefaultParser"));

		// When
		JavaEditorBookmarkLocation location = javaBookmarkLocationProvider.findLocation(bookmark);

		// Then
		assertEquals("DefaultParser", location.getMember().getElementName());
		assertEquals(30, location.getLineNumber().intValue());
	}

	@Test
	public void testJavaMethodFindLocation() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROP_JAVA_DECLARING_TYPE, "org.apache.commons.cli.DefaultParser",
						PROP_JAVA_ELEMENT_KIND, KIND_METHOD, PROP_JAVA_ELEMENT_NAME, "parse"));

		// When
		JavaEditorBookmarkLocation location = javaBookmarkLocationProvider.findLocation(bookmark);

		// Then
		// currently, we only condider the first "parse" method
		assertEquals("parse", location.getMember().getElementName());
		assertEquals(57, location.getLineNumber().intValue());
	}

	@Test
	public void testInsideJavaElementFindLocation() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROP_JAVA_DECLARING_TYPE, "org.apache.commons.cli.DefaultParser",
						PROP_JAVA_ELEMENT_KIND, KIND_METHOD, PROP_JAVA_ELEMENT_NAME, "handleProperties",
						PROP_LINE_NUMBER_INSIDE_ELEMENT, "7"));

		// When
		JavaEditorBookmarkLocation location = javaBookmarkLocationProvider.findLocation(bookmark);

		// Then
		assertEquals("handleProperties", location.getMember().getElementName());
		assertEquals(146, location.getLineNumber().intValue());
	}

	@Test
	public void testInsideJavaElementFuzzyFindLocation() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROP_JAVA_DECLARING_TYPE, "org.apache.commons.cli.DefaultParser",
						PROP_JAVA_ELEMENT_KIND, KIND_METHOD, PROP_JAVA_ELEMENT_NAME, "handleProperties",
						PROP_LINE_CONTENT,
						"for (Enumeration<?> enumeration = properties.propertyNames(); enumeration.hasMoreElements();)"));

		// When
		JavaEditorBookmarkLocation location = javaBookmarkLocationProvider.findLocation(bookmark);

		// Then
		assertEquals("handleProperties", location.getMember().getElementName());
		assertEquals(146, location.getLineNumber().intValue());
	}

	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.java.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

}
