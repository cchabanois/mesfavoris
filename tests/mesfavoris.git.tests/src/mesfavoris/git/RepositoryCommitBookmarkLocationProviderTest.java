package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_COMMIT_ID;
import static mesfavoris.git.GitBookmarkProperties.PROP_REMOTE_URLS;
import static mesfavoris.git.GitTestHelper.importGitProject;
import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Test;

import mesfavoris.BookmarksException;
import mesfavoris.model.Bookmark;

public class RepositoryCommitBookmarkLocationProviderTest {
	private static final String REPOSITORY_URL = "https://github.com/LorenzoBettini/junit-swtbot-example.git";
	private static final String PROJECT_NAME = "mathutils.core";
	private RepositoryCommitBookmarkLocationProvider bookmarkLocationProvider;

	@Before
	public void setUp() throws BookmarksException {
		importGitProject(REPOSITORY_URL, PROJECT_NAME);
		bookmarkLocationProvider = new RepositoryCommitBookmarkLocationProvider();
	}

	@Test
	public void testLocation() {
		// Given
		String commitId = "e62f9c6177b8e13fafa4a9416511aa4ddb4ace31";
		Bookmark bookmark = bookmark("Commit e62f9c6").withProperty(PROP_COMMIT_ID, commitId)
				.withProperty(PROP_REMOTE_URLS, REPOSITORY_URL).withProperty(PROP_REMOTE_URLS, REPOSITORY_URL)
				.withProperty(Bookmark.PROPERTY_NAME, "Commit e62f9c6")
				.withProperty(Bookmark.PROPERTY_COMMENT, "added pom files for tycho build").build();

		// When
		RepositoryCommitBookmarkLocation bookmarkLocation = (RepositoryCommitBookmarkLocation) bookmarkLocationProvider
				.getBookmarkLocation(bookmark, new NullProgressMonitor());

		// Then
		assertThat(bookmarkLocation.getRepositoryCommit().getRepositoryName()).isEqualTo("junit-swtbot-example");
		assertThat(bookmarkLocation.getRepositoryCommit().getRevCommit().getId().name()).isEqualTo(commitId);
	}

}
