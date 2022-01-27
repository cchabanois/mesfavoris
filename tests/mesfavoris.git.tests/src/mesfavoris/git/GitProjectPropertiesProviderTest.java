package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_BRANCH;
import static mesfavoris.git.GitBookmarkProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_RESOURCE_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_URL;
import static mesfavoris.git.GitTestHelper.importGitProject;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.info.GitItemState;
import org.eclipse.egit.core.internal.info.GitItemStateFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.tests.commons.waits.Waiter;

public class GitProjectPropertiesProviderTest {
	private static final String REPOSITORY_URL = "https://github.com/LorenzoBettini/junit-swtbot-example.git";
	private static final String PROJECT_NAME = "mathutils.core";
	private GitProjectPropertiesProvider gitProjectPropertiesProvider = new GitProjectPropertiesProvider();
	private Map<String, String> bookmarkProperties = new HashMap<>();

	@Before
	public void setUp() throws BookmarksException {
		importGitProject(REPOSITORY_URL, PROJECT_NAME);
	}

	@Test
	public void testAddGitProjectProperties() throws Exception {
		// Given
		IResource resource = getResource("mathutils.core/build.properties");
		ISelection selection = new StructuredSelection(resource);
		Waiter.waitUntil("build.properties not tracked",
				() -> isTracked(resource));

		// When
		gitProjectPropertiesProvider.addBookmarkProperties(bookmarkProperties, null, selection,
				new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties).containsEntry(PROP_URL, REPOSITORY_URL).containsEntry(PROP_BRANCH, "master")
				.containsEntry(PROP_PROJECT_PATH, "mathutils.core")
				.containsEntry(PROP_RESOURCE_PATH, "mathutils.core/build.properties");
	}

	@Test
	public void testDoNotAddGitProjectPropertiesIfNotTracked() throws Exception {
		// Given
		IFile file = createFile("mathutils.core/newFile.txt", "the contents");
		assertThat(file.exists());
		ISelection selection = new StructuredSelection(file);
		Waiter.waitUntil("build.properties not tracked",
				() -> isTracked(getResource("mathutils.core/build.properties")));

		// When
		gitProjectPropertiesProvider.addBookmarkProperties(bookmarkProperties, null, selection,
				new NullProgressMonitor());
		
		// Then
		assertThat(bookmarkProperties).isEmpty();
	}

	private IResource getResource(String resourcePath) {
		Path path = new Path(resourcePath);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = workspaceRoot.findMember(path);
		return resource;
	}

	private IFile createFile(String path, String contents) throws UnsupportedEncodingException, CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(new Path(path));
		InputStream source = new ByteArrayInputStream(contents.getBytes("UTF-8"));
		file.delete(true, null);
		file.create(source, IResource.FORCE, null);
		return file;
	}
	
	private boolean isTracked(IResource resource) {
		GitItemState state = GitItemStateFactory.getInstance()
				.get(resource.getLocation().toFile());
		return state.isTracked();
	}

}
