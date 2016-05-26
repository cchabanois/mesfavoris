package mesfavoris.internal.remote;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.spi.RegistryContributor;

import mesfavoris.StatusHelper;
import mesfavoris.remote.AbstractRemoteBookmarksStore;
import mesfavoris.remote.IRemoteBookmarksStore;

public class RemoteBookmarksStoreLoader implements Provider<List<IRemoteBookmarksStore>> {
	private static final String EXTENSION_POINT = "mesfavoris.remoteStorage";
	private List<IRemoteBookmarksStore> remoteBookmarksStores = null;

	public synchronized List<IRemoteBookmarksStore> get() {
		if (remoteBookmarksStores == null) {
			loadRemoteBookmarksStore();
		}

		return remoteBookmarksStores;

	}

	private void loadRemoteBookmarksStore() {
		remoteBookmarksStores = new ArrayList<>();
		IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT);

		if (extPoint == null) {
			StatusHelper.logError(MessageFormat.format("no {0} extension point", EXTENSION_POINT), null);
			return;
		}

		IExtension[] extensions = extPoint.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IContributor contributor = extension.getContributor();
			String contributorName;
			if (contributor instanceof RegistryContributor) {
				// contributor is a fragment
				contributorName = ((RegistryContributor) contributor).getActualName();
			} else {
				contributorName = contributor.getName();
			}

			IConfigurationElement[] elements = extension.getConfigurationElements();

			for (IConfigurationElement configurationElement : elements) {
				try {
					String id = configurationElement.getAttribute("id");
					String shortId = id.substring(id.lastIndexOf('.') + 1);
					String label = configurationElement.getAttribute("label");
					String iconResource = configurationElement.getAttribute("icon");
					String iconOverlayResource = configurationElement.getAttribute("overlayIcon");
					AbstractRemoteBookmarksStore store = (AbstractRemoteBookmarksStore) configurationElement
							.createExecutableExtension("class");
					RemoteBookmarkStoreDescriptor remoteStoreDescriptor = new RemoteBookmarkStoreDescriptor(shortId,
							label, iconResource, iconOverlayResource, contributorName);
					store.init(remoteStoreDescriptor);
					remoteBookmarksStores.add(store);
				} catch (CoreException e) {
					StatusHelper.logWarn(
							"Could not create remote bookmarks store " + configurationElement.getAttribute("class"), e);
				}
			}
		}
	}

}
