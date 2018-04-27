package mesfavoris.path.internal;

import static mesfavoris.path.PathBookmarkProperties.PROPERTY_NAME;
import static mesfavoris.path.PathBookmarkProperties.PROP_FOLDER_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mesfavoris.placeholders.IPathPlaceholderResolver;

public class ExternalFolderBookmarkPropertiesProviderTest {
	private ExternalFolderBookmarkPropertiesProvider propertiesProvider;
	private IPathPlaceholderResolver pathPlaceholderResolver = mock(IPathPlaceholderResolver.class);
	private Map<String, String> bookmarkProperties = new HashMap<>();
	private File folder;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws IOException {
		propertiesProvider = new ExternalFolderBookmarkPropertiesProvider(pathPlaceholderResolver);
		folder = temporaryFolder.newFolder("folder1");
	}

	@Test
	public void testAddBookmarkProperties() {
		// Given
		IPath path = new Path(folder.getAbsolutePath());
		ISelection selection = new StructuredSelection(path);
		when(pathPlaceholderResolver.collapse(path)).thenReturn(path.toString());
		
		// When
		propertiesProvider.addBookmarkProperties(bookmarkProperties, null, selection, new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties).containsEntry(PROPERTY_NAME, "folder1");
		assertThat(bookmarkProperties).containsEntry(PROP_FOLDER_PATH, new Path(folder.getAbsolutePath()).toString());
	}

}
