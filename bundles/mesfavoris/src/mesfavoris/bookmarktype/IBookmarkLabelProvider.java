package mesfavoris.bookmarktype;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

import mesfavoris.model.Bookmark;

public interface IBookmarkLabelProvider {

	public StyledString getStyledText(Context context, Bookmark bookmark);

	public ImageDescriptor getImageDescriptor(Context context, Bookmark bookmark);

	public boolean canHandle(Context context, Bookmark bookmark);

	public static interface Context {
		public static final String BOOKMARK_DATABASE_ID = "id";
		public static final String BOOKMARKS_TREE = "bookmarksTree";
				
		public <T> T get(String name);
	}

}
