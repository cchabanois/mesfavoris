package mesfavoris.viewers;

import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.model.Bookmark;

public class BookmarkDecorationProvider implements IBookmarkDecorationProvider {
	private final IBookmarkDecorationProvider[] providers;
	
	
	public BookmarkDecorationProvider(IBookmarkDecorationProvider... providers) {
		this.providers = providers;
	}
	
	
	@Override
	public ImageDescriptor[] apply(Bookmark bookmark) {
		ImageDescriptor[] allOverlayImages = new ImageDescriptor[5];
		for (IBookmarkDecorationProvider provider : providers) {
			ImageDescriptor[] overlayImages = provider.apply(bookmark);
			for (int i = 0; i < overlayImages.length; i++) {
				if (overlayImages[i] != null) {
					allOverlayImages[i] = overlayImages[i];
				}
			}
		}
		return allOverlayImages;
	}

}
