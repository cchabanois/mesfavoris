package mesfavoris.remote;

import org.eclipse.jface.resource.ImageDescriptor;

public interface IRemoteBookmarksStoreDescriptor {

	public abstract String getId();
	
	public abstract ImageDescriptor getImageDescriptor();

	public abstract ImageDescriptor getImageOverlayDescriptor();

	public abstract String getLabel();

}