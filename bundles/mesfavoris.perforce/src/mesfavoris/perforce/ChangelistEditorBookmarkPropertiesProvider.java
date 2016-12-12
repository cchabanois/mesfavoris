package mesfavoris.perforce;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.ui.changelists.IChangelistEditorInput;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;

import static mesfavoris.perforce.PerforceBookmarkProperties.*;

public class ChangelistEditorBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		if (!(part instanceof IEditorPart)) {
			return;
		}
		IEditorPart editorPart = (IEditorPart)part;
		if (!(editorPart.getEditorInput() instanceof IChangelistEditorInput)) {
			return;
		}
		IChangelistEditorInput changelistEditorInput = (IChangelistEditorInput) editorPart.getEditorInput();
		IP4Changelist changelist = changelistEditorInput.getChangelist();
		if (changelist.getStatus() != ChangelistStatus.SUBMITTED) {
			// for now, only consider submitted CLs, not pending or shelved ones
			return;
		}
		bookmarkProperties.put(PROP_PORT, changelist.getConnection().getParameters().getPort());
		bookmarkProperties.put(PROP_CHANGELIST, Integer.toString(changelist.getId()));
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, "Changelist "+ changelist.getId());
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_COMMENT, changelist.getShortDescription());
	}

}
