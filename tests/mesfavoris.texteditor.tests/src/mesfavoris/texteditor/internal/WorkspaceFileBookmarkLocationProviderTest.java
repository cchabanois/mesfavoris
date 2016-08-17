package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.*;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableMap;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class WorkspaceFileBookmarkLocationProviderTest {

	private WorkspaceFileBookmarkLocationProvider locationProvider;

	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate("textEditorBookmarkLocationProviderTest", "commons-cli");
	}

	@Before
	public void setUp() {
		locationProvider = new WorkspaceFileBookmarkLocationProvider();
	}

	@Test
	public void testInsideWorkspaceFileFuzzyFindLocation() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(), ImmutableMap.of(PROP_WORKSPACE_PATH,
				"/textEditorBookmarkLocationProviderTest/src/main/java/org/apache/commons/cli/DefaultParser.java",
				PROP_LINE_CONTENT,
				"for (Enumeration<?> enumeration = properties.propertyNames(); enumeration.hasMoreElements();)"));

		// When
		WorkspaceFileBookmarkLocation location = locationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

		// Then
		assertEquals("/textEditorBookmarkLocationProviderTest/src/main/java/org/apache/commons/cli/DefaultParser.java",
				location.getWorkspaceFile().getFullPath().toString());
		assertEquals(146, location.getLineNumber().intValue());
	}

	@Test
	public void testNonExistingWorkspaceFile() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId(),
				ImmutableMap.of(PROP_WORKSPACE_PATH,
						"/textEditorBookmarkLocationProviderTest/src/main/java/org/apache/commons/cli/NonExistingFile.java",
						PROP_LINE_NUMBER, "15"));

		// When
		WorkspaceFileBookmarkLocation location = locationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

		// Then
		assertNull(location);
	}

	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.texteditor.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

}
