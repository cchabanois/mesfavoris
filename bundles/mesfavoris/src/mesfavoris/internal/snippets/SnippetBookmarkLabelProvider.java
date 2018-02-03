package mesfavoris.internal.snippets;

import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.bookmarktype.AbstractBookmarkLabelProvider;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.IUIConstants;
import mesfavoris.model.Bookmark;

public class SnippetBookmarkLabelProvider extends AbstractBookmarkLabelProvider {

	@Override
	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark) {
		return BookmarksPlugin.getImageDescriptor(IUIConstants.IMG_SNIPPET);
	}
	
	@Override
	public boolean canHandle(Context context, Bookmark bookmark) {
		return bookmark.getPropertyValue(SnippetBookmarkProperties.PROP_SNIPPET_CONTENT) != null;
	}

}
