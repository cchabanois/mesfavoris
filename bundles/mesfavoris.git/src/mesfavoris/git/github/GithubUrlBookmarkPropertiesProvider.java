package mesfavoris.git.github;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;
import static mesfavoris.git.GitBookmarkProperties.PROP_BRANCH;
import static mesfavoris.git.GitBookmarkProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_URL;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;

public class GithubUrlBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		if (!(selected instanceof URL)) {
			return;
		}
		URL url = (URL) selected;
		if (!url.getHost().equalsIgnoreCase("github.com")) {
			return;
		}
		Optional<Repository> repository = getRepositoryIdentifierCandidate(url).flatMap(repositoryIdentifier->getRepository(repositoryIdentifier));
		if (!repository.isPresent()) {
			return;
		}
		putIfAbsent(bookmarkProperties, PROP_URL, repository.get().getCloneUrl());
		putIfAbsent(bookmarkProperties, PROP_BRANCH, repository.get().getDefaultBranch());
		putIfAbsent(bookmarkProperties, PROP_PROJECT_PATH, "");
	}

	private Optional<Repository> getRepository(RepositoryIdentifier repositoryIdentifier) {
		RepositoryService repositoryService = new RepositoryService();
		try {
			Repository repository = repositoryService.getRepository(repositoryIdentifier.owner,
					repositoryIdentifier.name);
			if (repository == null) {
				return Optional.empty();
			}
			return Optional.of(repository);
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	private Optional<RepositoryIdentifier> getRepositoryIdentifierCandidate(URL url) {
		String[] pathSegments = url.getPath().split("/");
		if (pathSegments.length < 3) {
			return Optional.empty();
		}
		return Optional.of(new RepositoryIdentifier(pathSegments[1], pathSegments[2]));
	}

	private static class RepositoryIdentifier {
		private final String name;
		private final String owner;

		public RepositoryIdentifier(String owner, String name) {
			this.owner = owner;
			this.name = name;
		}

	}

}
