package org.chabanois.mesfavoris.internal.bookmarktypes;

import java.util.List;

import org.chabanois.mesfavoris.bookmarktype.IBookmarkLabelProvider;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class PluginBookmarkLabelProvider extends LabelProvider implements
		IBookmarkLabelProvider {
	private final BookmarkTypeConfigElementLoader loader = new BookmarkTypeConfigElementLoader();
	private BookmarkLabelProvider bookmarkLabelProvider;

	private synchronized BookmarkLabelProvider getBookmarkLabelProvider() {
		if (bookmarkLabelProvider != null) {
			return bookmarkLabelProvider;
		}
		List<IBookmarkLabelProvider> labelProviders = loader
				.load("labelProvider");
		this.bookmarkLabelProvider = new BookmarkLabelProvider(labelProviders);
		return bookmarkLabelProvider;
	}

	@Override
	public Image getImage(Object element) {
		return getBookmarkLabelProvider().getImage(element);
	}

	@Override
	public String getText(Object element) {
		return getBookmarkLabelProvider().getText(element);
	}

	@Override
	public void dispose() {
		try {
			getBookmarkLabelProvider().dispose();
		} finally {
			super.dispose();
		}
	}

	@Override
	public StyledString getStyledText(Object element) {
		return getBookmarkLabelProvider().getStyledText(element);
	}

	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return getBookmarkLabelProvider().handlesBookmark(bookmark);
	}

}
