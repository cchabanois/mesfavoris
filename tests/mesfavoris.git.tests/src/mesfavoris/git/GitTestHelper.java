package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_BRANCH;
import static mesfavoris.git.GitBookmarkProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_URL;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.ui.internal.commit.RepositoryCommit;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.FileUtils;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;

public class GitTestHelper {

	public static void importGitProject(String repositoryUrl, String projectPath) throws BookmarksException {
		Bookmark bookmark = bookmark("myBookmark").withProperty(PROP_BRANCH, "master")
				.withProperty(PROP_PROJECT_PATH, projectPath).withProperty(PROP_URL, repositoryUrl).build();
		ImportGitProject importGitProject = new ImportGitProject();
		importGitProject.importProject(bookmark, new NullProgressMonitor());
	}

	public static RepositoryCommit getRepositoryCommit(Repository repository, String commitId) {
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
	
	public static void tryDeleteRepository(String remoteUrl) {
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
	
	private static Set<String> getRemotesUrls(Repository repository) {
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
