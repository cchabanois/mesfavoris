package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_BRANCH;
import static mesfavoris.git.GitBookmarkProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_URL;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.git.rules.TemporaryDefaultRepositoryDirRule;
import mesfavoris.model.Bookmark;

public class ImportGitProjectTest {
	private static final String REPOSITORY_URL = "https://github.com/LorenzoBettini/junit-swtbot-example.git";
	private static final String PROJECT_NAME = "mathutils.core";
	private ImportGitProject importGitProject;

	@Rule
	public TemporaryDefaultRepositoryDirRule tempRepositoryDir = new TemporaryDefaultRepositoryDirRule();	
	
	@Before
	public void setUp() {
		importGitProject = new ImportGitProject();
	}

	@Test
	public void testImportGitProject() throws BookmarksException {
		// Given
		Bookmark bookmark = bookmark("myBookmark").withProperty(PROP_BRANCH, "master")
				.withProperty(PROP_PROJECT_PATH, PROJECT_NAME).withProperty(PROP_URL, REPOSITORY_URL).build();

		// When
		assertTrue(importGitProject.canHandle(bookmark));
		importGitProject.importProject(bookmark, new NullProgressMonitor());

		// Then
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(PROJECT_NAME);
		assertTrue(project.exists());
	}

	@Test
	public void testImportAlreadyImportedGitProjectDoesNothing() throws BookmarksException {
		// Given
		Bookmark bookmark = bookmark("myBookmark").withProperty(PROP_BRANCH, "master")
				.withProperty(PROP_PROJECT_PATH, PROJECT_NAME).withProperty(PROP_URL, REPOSITORY_URL).build();
		importGitProject.importProject(bookmark, new NullProgressMonitor());

		// When
		importGitProject.importProject(bookmark, new NullProgressMonitor());

		// Then
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(PROJECT_NAME);
		assertTrue(project.exists());
	}

}
