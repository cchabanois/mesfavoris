package mesfavoris.gdrive.decoration;

import java.util.Optional;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.mappings.BookmarkMapping;
import mesfavoris.gdrive.mappings.IBookmarkMappings;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class GDriveBookmarkFolderLightweightDecorator extends LabelProvider implements ILightweightLabelDecorator {
	private IBookmarkMappings bookmarkMappings;

	public GDriveBookmarkFolderLightweightDecorator() {
		bookmarkMappings = Activator.getBookmarkMappingsStore();
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (!(element instanceof Bookmark)) {
			return;
		}
		Bookmark bookmark = (Bookmark) element;
		BookmarkId bookmarkId = bookmark.getId();
		Optional<BookmarkMapping> bookmarkMapping = bookmarkMappings.getMapping(bookmarkId);
		if (!bookmarkMapping.isPresent()) {
			return;
		}
		String sharingUser = bookmarkMapping.get().getProperties().get("sharingUser");
		if (sharingUser == null) {
			return;
		}
		decoration.addSuffix(String.format(" [Shared by %s]", sharingUser));
	}

}
