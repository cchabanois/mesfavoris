package mesfavoris.git;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;
import static mesfavoris.git.GitBookmarkProperties.PROP_COMMIT_ID;
import static mesfavoris.git.GitBookmarkProperties.PROP_REMOTE_URLS;
import static mesfavoris.git.GitBookmarkProperties.PROP_REPOSITORY_DIR;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.base.Joiner;

import mesfavoris.MesFavoris;
import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.placeholders.IPathPlaceholderResolver;

public class CommitBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {
	private final IPathPlaceholderResolver pathPlaceholderResolver;

	public CommitBookmarkPropertiesProvider() {
		this(MesFavoris.getPathPlaceholderResolver());
	}

	public CommitBookmarkPropertiesProvider(IPathPlaceholderResolver pathPlaceholderResolver) {
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		RevCommit revCommit = Adapters.adapt(selected, RevCommit.class);
		if (revCommit == null) {
			return;
		}
		Repository repository = Adapters.adapt(selected, Repository.class);
		if (repository == null) {
			return;
		}
		String revCommitName = revCommit.name();
		bookmarkProperties.put(PROP_COMMIT_ID, revCommitName);
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, "Commit " + revCommit.getId().abbreviate(7).name());
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_COMMENT, revCommit.getShortMessage());
		IPath localRepositoryDirPath = getLocalRepositoryDirPath(repository);
		if (localRepositoryDirPath != null) {
			putIfAbsent(bookmarkProperties, PROP_REPOSITORY_DIR,
					pathPlaceholderResolver.collapse(localRepositoryDirPath));
		}
		Set<String> remoteUrls = getRemotesUrls(repository);
		if (!remoteUrls.isEmpty()) {
			putIfAbsent(bookmarkProperties, PROP_REMOTE_URLS, getComaSeparatedRemoteUrls(remoteUrls));
		}
	}

	private IPath getLocalRepositoryDirPath(Repository repository) {
		File localRepositoryDir = repository.getDirectory();
		if (localRepositoryDir == null) {
			return null;
		}
		IPath path = Path.fromOSString(localRepositoryDir.toString());
		return path;
	}

	private String getComaSeparatedRemoteUrls(Set<String> remoteUrls) {
		return Joiner.on(",").join(remoteUrls);
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
