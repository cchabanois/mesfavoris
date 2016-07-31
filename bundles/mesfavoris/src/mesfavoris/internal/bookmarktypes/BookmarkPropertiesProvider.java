package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import mesfavoris.StatusHelper;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class BookmarkPropertiesProvider implements IBookmarkPropertiesProvider {
	private final List<IBookmarkPropertiesProvider> providers;

	public BookmarkPropertiesProvider(List<IBookmarkPropertiesProvider> providers) {
		this.providers = new ArrayList<IBookmarkPropertiesProvider>(providers);
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection) {
		providers.forEach(p -> addBookmarkProperties(p, bookmarkProperties, part, selection));
	}

	private void addBookmarkProperties(IBookmarkPropertiesProvider provider, Map<String, String> bookmarkProperties,
			IWorkbenchPart part, ISelection selection) {
		SafeRunner.run(new ISafeRunnable() {

			public void run() throws Exception {
				provider.addBookmarkProperties(bookmarkProperties, part, selection);
			}

			public void handleException(Throwable exception) {
				StatusHelper.logError("Error adding bookmark properties", exception);
			}
		});
	}

}
