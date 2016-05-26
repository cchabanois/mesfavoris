package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.model.Bookmark;

public class BookmarkLabelProvider extends LabelProvider implements IBookmarkLabelProvider {
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
	public Image getImage(Object element) {
		return getBookmarkLabelProvider((Bookmark)element).getImage(element);
	}
	
	@Override
	public void dispose() {
		bookmarkLabelProviders.forEach(p -> p.dispose());
		super.dispose();
	}
	
	@Override
	public StyledString getStyledText(Object element) {
		return getBookmarkLabelProvider((Bookmark)element).getStyledText(element);
	}

	private IBookmarkLabelProvider getBookmarkLabelProvider(Bookmark bookmark) {
		for (IBookmarkLabelProvider bookmarkLabelProvider : bookmarkLabelProviders) {
			if (bookmarkLabelProvider.handlesBookmark(bookmark)) {
				return bookmarkLabelProvider;
			}
		}
		// will never happen
		return null;
	}
	
	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return true;
	}

	private static class DefaultBookmarkLabelProvider extends AbstractBookmarkLabelProvider {

		@Override
		public boolean handlesBookmark(Bookmark bookmark) {
			return true;
		}
		
	}
	

}
