package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_BRANCH;
import static mesfavoris.git.GitBookmarkProperties.PROP_COMMIT_ID;
import static mesfavoris.git.GitBookmarkProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_REMOTE_URLS;
import static mesfavoris.git.GitBookmarkProperties.PROP_URL;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.ui.SWTBotViewHelper.closeWelcomeView;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.ui.internal.commit.CommitEditor;
import org.eclipse.egit.ui.internal.commit.RepositoryCommit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;

public class CommitEditorBookmarkPropertiesProviderTest {
	private static final String REPOSITORY_URL = "https://github.com/LorenzoBettini/junit-swtbot-example.git";
	private static final String PROJECT_NAME = "mathutils.core";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private SWTWorkbenchBot bot;
	private Map<String, String> bookmarkProperties = new HashMap<>();

	@Before
	public void setUp() throws BookmarksException {
		bot = new SWTWorkbenchBot();
		closeWelcomeView();
		importGitProject();
	}

	@Test
	public void testGetBookmarkProperties() throws Exception {
		// Given
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).getFile(new Path("pom.xml"));
		String commitId = "e62f9c6177b8e13fafa4a9416511aa4ddb4ace31";
		IEditorPart editorPart = openCommitEditor(getRepositoryCommit(file, commitId));

		// When
		CommitEditorBookmarkPropertiesProvider provider = new CommitEditorBookmarkPropertiesProvider();
		provider.addBookmarkProperties(bookmarkProperties, editorPart, null, new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties).containsEntry(PROP_COMMIT_ID, commitId)
				.containsEntry(PROP_REMOTE_URLS, REPOSITORY_URL).containsEntry(PROP_REMOTE_URLS, REPOSITORY_URL)
				.containsEntry(Bookmark.PROPERTY_NAME, "Commit e62f9c6")
				.containsEntry(Bookmark.PROPERTY_COMMENT, "added pom files for tycho build");
	}

	private IEditorPart openCommitEditor(RepositoryCommit repositoryCommit) {
		return UIThreadRunnable.syncExec(() -> {
			try {
				return CommitEditor.open(repositoryCommit);
			} catch (PartInitException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private RepositoryCommit getRepositoryCommit(IFile file, String commitId) {
		RepositoryCache repositoryCache = Activator.getDefault().getRepositoryCache();
		Repository repository = repositoryCache.getRepository(file);
		RepositoryCommit repositoryCommit = getRepositoryCommit(repository, commitId);
		return repositoryCommit;
	}

	private void importGitProject() throws BookmarksException {
		Bookmark bookmark = bookmark("myBookmark").withProperty(PROP_BRANCH, "master")
				.withProperty(PROP_PROJECT_PATH, PROJECT_NAME).withProperty(PROP_URL, REPOSITORY_URL).build();
		ImportGitProject importGitProject = new ImportGitProject();
		importGitProject.importProject(bookmark, new NullProgressMonitor());
	}

	private RepositoryCommit getRepositoryCommit(Repository repository, String commitId) {
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(ObjectId.fromString(commitId));
			for (RevCommit parent : commit.getParents())
				walk.parseBody(parent);
			RepositoryCommit repositoryCommit = new RepositoryCommit(repository, commit);
			return repositoryCommit;
		} catch (IOException e) {
			return null;
		}
	}
}
