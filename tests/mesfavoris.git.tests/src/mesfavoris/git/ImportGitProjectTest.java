package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_BRANCH;
import static mesfavoris.git.GitBookmarkProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_URL;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;

public class ImportGitProjectTest {
	private static final String REPOSITORY_URL = "https://github.com/LorenzoBettini/junit-swtbot-example.git";
	private static final String PROJECT_NAME = "mathutils.core";
	private ImportGitProject importGitProject;

	@Before
	public void setUp() {
		importGitProject = new ImportGitProject();
		tryDeleteProject(PROJECT_NAME);
		tryDeleteRepository(REPOSITORY_URL);
	}

	@After
	public void tearDown() {
		tryDeleteProject(PROJECT_NAME);
		tryDeleteRepository(REPOSITORY_URL);
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

	private void tryDeleteProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);
		try {
			project.delete(false, true, new NullProgressMonitor());
		} catch (CoreException e) {

		}
	}

	private void tryDeleteRepository(String remoteUrl) {
		RepositoryCache repositoryCache = Activator.getDefault().getRepositoryCache();
		Repository[] repositories = repositoryCache.getAllRepositories();
		for (Repository repository : repositories) {
			if (getRemotesUrls(repository).contains(remoteUrl)) {
				try {
					FileUtils.delete(repository.getDirectory(),
							FileUtils.RECURSIVE | FileUtils.RETRY | FileUtils.SKIP_MISSING);
					FileUtils.delete(repository.getWorkTree(),
							FileUtils.RECURSIVE | FileUtils.RETRY | FileUtils.SKIP_MISSING);
				} catch (IOException e) {
				}
			}
		}
	}

	private Set<String> getRemotesUrls(Repository repository) {
		Set<String> remoteUrls = new HashSet<String>();
		Config storedConfig = repository.getConfig();
		Set<String> remotes = storedConfig.getSubsections("remote");
		for (String remoteName : remotes) {
			String url = storedConfig.getString("remote", remoteName, "url");
			remoteUrls.add(url);
		}
		return remoteUrls;
	}

}
