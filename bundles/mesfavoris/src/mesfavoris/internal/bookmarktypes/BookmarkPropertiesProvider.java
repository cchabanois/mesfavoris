package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.StatusHelper;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;

public class BookmarkPropertiesProvider implements IBookmarkPropertiesProvider {
	private final List<IBookmarkPropertiesProvider> providers;

	public BookmarkPropertiesProvider(List<IBookmarkPropertiesProvider> providers) {
		this.providers = new ArrayList<IBookmarkPropertiesProvider>(providers);
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Getting bookmark properties", providers.size());
		providers.forEach(p -> addBookmarkProperties(p, bookmarkProperties, part, selection, subMonitor.newChild(1)));
	}

	private void addBookmarkProperties(IBookmarkPropertiesProvider provider, Map<String, String> bookmarkProperties,
			IWorkbenchPart part, ISelection selection, IProgressMonitor monitor) {
		SafeRunner.run(new ISafeRunnable() {

			public void run() throws Exception {
				provider.addBookmarkProperties(bookmarkProperties, part, selection, monitor);
			}

			public void handleException(Throwable exception) {
				StatusHelper.logError("Error adding bookmark properties", exception);
			}
		});
	}

}
