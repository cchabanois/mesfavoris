package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;

public class BookmarkLocationProvider implements IBookmarkLocationProvider {
	private final List<IBookmarkLocationProvider> bookmarkLocationProviders;

	public BookmarkLocationProvider(List<IBookmarkLocationProvider> bookmarkLocationProviders) {
		this.bookmarkLocationProviders = new ArrayList<IBookmarkLocationProvider>(bookmarkLocationProviders);
	}

	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, bookmarkLocationProviders.size());
		float bestScore = 0.0f;
		IBookmarkLocation bestBookmarkLocation = null;
		for (IBookmarkLocationProvider provider : bookmarkLocationProviders) {
			IBookmarkLocation bookmarkLocation = provider.getBookmarkLocation(bookmark, subMonitor.newChild(1));
			if (bookmarkLocation != null && bookmarkLocation.getScore() > bestScore) {
				bestBookmarkLocation = bookmarkLocation;
				bestScore = bookmarkLocation.getScore();
				if (bestScore >= IBookmarkLocation.MAX_SCORE) {
					break;
				}
			}
		}
		return bestBookmarkLocation;
	}

}
