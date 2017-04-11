package mesfavoris.gdrive.decoration;

import java.util.Optional;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import mesfavoris.commons.ui.viewers.StylerProvider;
import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.mappings.BookmarkMapping;
import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.viewers.BookmarkFolderLabelProvider;

public class GDriveBookmarkFolderLabelProvider extends BookmarkFolderLabelProvider {
	private final IBookmarkMappings bookmarkMappings;
	private final StylerProvider stylerProvider = new StylerProvider();

	public GDriveBookmarkFolderLabelProvider() {
		bookmarkMappings = Activator.getDefault().getBookmarkMappingsStore();
	}

	@Override
	public StyledString getStyledText(Object element) {
		StyledString styledString = super.getStyledText(element);
		BookmarkFolder bookmarkFolder = (BookmarkFolder) element;
		BookmarkId bookmarkId = bookmarkFolder.getId();
		Optional<BookmarkMapping> bookmarkMapping = bookmarkMappings.getMapping(bookmarkId);
		if (!bookmarkMapping.isPresent()) {
			return styledString;
		}
		String sharingUser = bookmarkMapping.get().getProperties().get(BookmarkMapping.PROP_SHARING_USER);
		if (sharingUser == null) {
			return styledString;
		}
		Styler styler = stylerProvider.getStyler(null,
				Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW), null);
		styledString.append(String.format(" [Shared by %s]", sharingUser), styler);
		return styledString;
	}

	@Override
	public boolean handlesBookmark(Bookmark bookmark) {
		return super.handlesBookmark(bookmark) && bookmarkMappings.getMapping(bookmark.getId()).isPresent();
	}

}
