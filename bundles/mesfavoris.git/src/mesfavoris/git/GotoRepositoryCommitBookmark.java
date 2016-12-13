package mesfavoris.git;

import org.eclipse.egit.ui.internal.commit.CommitEditor;
import org.eclipse.egit.ui.internal.commit.CommitEditorInput;
import org.eclipse.egit.ui.internal.commit.RepositoryCommit;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class GotoRepositoryCommitBookmark implements IGotoBookmark {

	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark, IBookmarkLocation bookmarkLocation) {
		if (!(bookmarkLocation instanceof RepositoryCommitBookmarkLocation)) {
			return false;
		}
		RepositoryCommitBookmarkLocation repositoryCommitBookmarkLocation = (RepositoryCommitBookmarkLocation) bookmarkLocation;
		RepositoryCommit repositoryCommit = repositoryCommitBookmarkLocation.getRepositoryCommit();
		CommitEditorInput input = new CommitEditorInput(repositoryCommit);

		try {
			IDE.openEditor(window.getActivePage(), input, CommitEditor.ID, true);
		} catch (PartInitException e) {
			return false;
		}
		return true;
	}

}
