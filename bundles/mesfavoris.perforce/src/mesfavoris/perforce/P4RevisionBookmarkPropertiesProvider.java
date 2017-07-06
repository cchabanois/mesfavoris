package mesfavoris.perforce;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;
import static mesfavoris.perforce.PerforceBookmarkProperties.PROP_CHANGELIST;
import static mesfavoris.perforce.PerforceBookmarkProperties.PROP_PORT;

import java.util.Map;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBookView;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.history.P4HistoryPage;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;

public class P4RevisionBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		selection = getSelection(part, selection);
		Object selected = getFirstElement(selection);
		IP4Revision p4Revision = Adapters.adapt(selected, IP4Revision.class);
		if (p4Revision == null) {
			return;
		}
		bookmarkProperties.put(PROP_PORT, p4Revision.getConnection().getParameters().getPort());
		bookmarkProperties.put(PROP_CHANGELIST, Integer.toString(p4Revision.getChangelist()));
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, "Changelist "+ p4Revision.getChangelist());
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_COMMENT, p4Revision.getComment());	
	}

	private ISelection getSelection(IWorkbenchPart part, ISelection selection) {
		// for some reason, selection is empty when we select a revision from the P4 history view
		if (!(part instanceof PageBookView)) {
			return selection;
		}
		IPage page = ((PageBookView)part).getCurrentPage();
		if (!(page instanceof P4HistoryPage)) {
			return selection;
		}
		return getSelection((P4HistoryPage)page);
	}
	
	private ISelection getSelection(P4HistoryPage page) {
		ISelection[] selection = new ISelection[1];
		page.getSite().getWorkbenchWindow().getShell().getDisplay().syncExec(() -> selection[0] = page.getViewer().getSelection());
		return selection[0];
	}
	
}
