package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.IUIConstants;
import mesfavoris.model.Bookmark;

public class BookmarkLabelProvider implements IBookmarkLabelProvider {
	private final List<IBookmarkLabelProvider> bookmarkLabelProviders;

	public BookmarkLabelProvider() {
		this.bookmarkLabelProviders = new ArrayList<IBookmarkLabelProvider>();
		this.bookmarkLabelProviders.add(new DefaultBookmarkLabelProvider());
	}
	
	public BookmarkLabelProvider(List<IBookmarkLabelProvider> bookmarkLabelProviders) {
		this.bookmarkLabelProviders = new ArrayList<IBookmarkLabelProvider>();
		this.bookmarkLabelProviders.addAll(bookmarkLabelProviders);
		this.bookmarkLabelProviders.add(new DefaultBookmarkLabelProvider());
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		ImageDescriptor imageDescriptor = getBookmarkLabelProvider(context, bookmark).getImageDescriptor(context, bookmark);
		if (imageDescriptor == null) {
			imageDescriptor = BookmarksPlugin.getImageDescriptor(IUIConstants.IMG_BOOKMARK); 
		}
		return imageDescriptor;
	}
	
	@Override
	public StyledString getStyledText(Context context, Bookmark bookmark) {
		return getBookmarkLabelProvider(context, bookmark).getStyledText(context, bookmark);
	}

	private IBookmarkLabelProvider getBookmarkLabelProvider(Context context, Bookmark bookmark) {
		for (IBookmarkLabelProvider bookmarkLabelProvider : bookmarkLabelProviders) {
			if (bookmarkLabelProvider.canHandle(context, bookmark)) {
				return bookmarkLabelProvider;
			}
		}
		// will never happen
		return null;
	}
	
	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return true;
	}

	private static class DefaultBookmarkLabelProvider extends AbstractBookmarkLabelProvider {

		@Override
		public boolean canHandle(Context context, Bookmark bookmark) {
			return true;
		}
		
	}
	

}
