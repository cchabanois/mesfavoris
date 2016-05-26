package mesfavoris.git;

import static mesfavoris.git.GitProjectProperties.PROP_BRANCH;
import static mesfavoris.git.GitProjectProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitProjectProperties.PROP_URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.GitProjectSetCapability;
import org.eclipse.team.core.TeamException;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.AbstractImportTeamProject;
import mesfavoris.model.Bookmark;

public class ImportGitProject extends AbstractImportTeamProject {

	private String[] getReferenceStrings(Bookmark bookmark) {
		// [1.0,https://git.eclipse.org/r/egit/egit.git,master,org.eclipse.egit]
		return new String[] { "1.0," + bookmark.getPropertyValue(PROP_URL)
				+ "," + bookmark.getPropertyValue(PROP_BRANCH) + ","
				+ bookmark.getPropertyValue(PROP_PROJECT_PATH) };
	}

	@Override
	public void importProject(Bookmark bookmark, IProgressMonitor monitor)
			throws BookmarksException {
		GitProjectSetCapability gitProjectSetCapability = new GitProjectSetCapability();
		try {
			gitProjectSetCapability.addToWorkspace(
					getReferenceStrings(bookmark), null, monitor);
		} catch (TeamException e) {
			throw new BookmarksException("Could not import git project", e);
		}
	}

	@Override
	public boolean canHandle(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_URL) != null
				&& bookmark.getPropertyValue(PROP_BRANCH) != null
				&& bookmark.getPropertyValue(PROP_PROJECT_PATH) != null;
	}

}
