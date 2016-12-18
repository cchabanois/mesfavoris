package mesfavoris.java;

import static mesfavoris.java.JavaBookmarkProperties.KIND_METHOD;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_DECLARING_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_KIND;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_NAME;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_METHOD_SIGNATURE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class JavaTypeBookmarkLocationProviderTest {
	private final JavaTypeMemberBookmarkLocationProvider javaBookmarkLocationProvider = new JavaTypeMemberBookmarkLocationProvider();

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
		JavaTypeMemberBookmarkLocation location = javaBookmarkLocationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

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
		JavaTypeMemberBookmarkLocation location = javaBookmarkLocationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

		// Then
		// there are several parse methods but we don't have enough information
		// to choose : we get the first one
		assertEquals("parse", location.getMember().getElementName());
		assertEquals(57, location.getLineNumber().intValue());
	}

	@Test
	public void testJavaMethodFindLocationUsingSignature() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROP_JAVA_DECLARING_TYPE, "org.apache.commons.cli.DefaultParser",
						PROP_JAVA_ELEMENT_KIND, KIND_METHOD, PROP_JAVA_ELEMENT_NAME, "parse",
						PROP_JAVA_METHOD_SIGNATURE, "CommandLine parse(Options,String[],Properties,boolean)"));

		// When
		JavaTypeMemberBookmarkLocation location = javaBookmarkLocationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

		// Then
		assertEquals("parse", location.getMember().getElementName());
		assertEquals(98, location.getLineNumber().intValue());
	}

	@Test
	public void testInsideJavaElementFindLocation() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROP_JAVA_DECLARING_TYPE, "org.apache.commons.cli.DefaultParser",
						PROP_JAVA_ELEMENT_KIND, KIND_METHOD, PROP_JAVA_ELEMENT_NAME, "handleProperties",
						PROP_LINE_NUMBER_INSIDE_ELEMENT, "7"));

		// When
		JavaTypeMemberBookmarkLocation location = javaBookmarkLocationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

		// Then
		assertEquals("handleProperties", location.getMember().getElementName());
		assertEquals(146, location.getLineNumber().intValue());
	}

	@Test
	public void testInsideJavaElementFuzzyFindLocation() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_JAVA_DECLARING_TYPE,
				"org.apache.commons.cli.DefaultParser", PROP_JAVA_ELEMENT_KIND, KIND_METHOD, PROP_JAVA_ELEMENT_NAME,
				"handleProperties", PROP_LINE_CONTENT,
				"for (Enumeration<?> enumeration = properties.propertyNames(); enumeration.hasMoreElements();)"));

		// When
		JavaTypeMemberBookmarkLocation location = javaBookmarkLocationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

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
