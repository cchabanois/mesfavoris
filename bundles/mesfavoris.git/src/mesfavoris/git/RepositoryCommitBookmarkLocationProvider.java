package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_COMMIT_ID;
import static mesfavoris.git.GitBookmarkProperties.PROP_REMOTE_URLS;
import static mesfavoris.git.GitBookmarkProperties.PROP_REPOSITORY_DIR;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.egit.ui.internal.commit.RepositoryCommit;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import mesfavoris.MesFavoris;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.placeholders.IPathPlaceholderResolver;

public class RepositoryCommitBookmarkLocationProvider implements IBookmarkLocationProvider {
	private final IPathPlaceholderResolver pathPlaceholderResolver;
	private final RepositoryCache repositoryCache;

	public RepositoryCommitBookmarkLocationProvider() {
		this(Activator.getDefault().getRepositoryCache(), MesFavoris.getPathPlaceholderResolver());
	}

	public RepositoryCommitBookmarkLocationProvider(RepositoryCache repositoryCache,
			IPathPlaceholderResolver pathPlaceholderResolver) {
		this.repositoryCache = repositoryCache;
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		String commitId = bookmark.getPropertyValue(PROP_COMMIT_ID);
		if (commitId == null) {
			return null;
		}
		Repository repository = null;
		String repositoryDir = bookmark.getPropertyValue(PROP_REPOSITORY_DIR);
		if (repositoryDir != null) {
			IPath path = pathPlaceholderResolver.expand(repositoryDir);
			repository = repositoryCache.getRepository(path);
		}
		if (repository == null) {
			String remoteUrlsAsString = bookmark.getPropertyValue(PROP_REMOTE_URLS);
			if (remoteUrlsAsString != null) {
				Set<String> remoteUrls = Sets.newHashSet(Splitter.on(",").split(remoteUrlsAsString));
				repository = getRepository(remoteUrls);
			}
		}
		if (repository == null) {
			return null;
		}
		RepositoryCommit repositoryCommit = getRepositoryCommit(repository, commitId);
		if (repositoryCommit == null) {
			return null;
		}
		return new RepositoryCommitBookmarkLocation(repositoryCommit);
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

	private Repository getRepository(Set<String> remoteUrls) {
		for (Repository repository : repositoryCache.getAllRepositories()) {
			if (!Collections.disjoint(remoteUrls, getRemotesUrls(repository))) {
				return repository;
			}
		}
		return null;
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
