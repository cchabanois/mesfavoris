package mesfavoris.bookmarktype;

import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

public interface IBookmarkPropertiesProvider {

	/**
	 * 
	 * @param bookmarkProperties
	 * @param part
	 *            the part the selection is coming from or null if not available
	 * @param selection
	 *            the selection to create the bookmark from
	 */
	public abstract void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection);

}