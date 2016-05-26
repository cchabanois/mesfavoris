package mesfavoris.internal.remote;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import mesfavoris.remote.IRemoteBookmarksStoreDescriptor;

public class RemoteBookmarkStoreDescriptor implements IRemoteBookmarksStoreDescriptor {
	private final String id;
	private final String label;
	private final ImageDescriptor imageDescriptor;
	private final ImageDescriptor imageOverlayDescriptor;

	public RemoteBookmarkStoreDescriptor(String id,
			String label, String iconResource, String iconOverlayResource, String bundleSymbolicName) {
		this.id = id;
		this.label = label;
		this.imageDescriptor = iconResource == null ? null : AbstractUIPlugin
				.imageDescriptorFromPlugin(bundleSymbolicName, iconResource);
		this.imageOverlayDescriptor = iconOverlayResource == null ? null : AbstractUIPlugin
				.imageDescriptorFromPlugin(bundleSymbolicName, iconOverlayResource);
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}
	
	@Override
	public ImageDescriptor getImageOverlayDescriptor() {
		return imageOverlayDescriptor;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

}
