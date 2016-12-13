package mesfavoris.git;

import org.eclipse.egit.ui.internal.commit.RepositoryCommit;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class RepositoryCommitBookmarkLocation implements IBookmarkLocation {
	private final RepositoryCommit repositoryCommit;

	public RepositoryCommitBookmarkLocation(RepositoryCommit repositoryCommit) {
		this.repositoryCommit = repositoryCommit;
	}

	public RepositoryCommit getRepositoryCommit() {
		return repositoryCommit;
	}
}
