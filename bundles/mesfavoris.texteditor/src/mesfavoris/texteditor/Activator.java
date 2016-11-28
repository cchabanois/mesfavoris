package mesfavoris.texteditor;

import static mesfavoris.texteditor.internal.Constants.PLACEHOLDER_HOME_NAME;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import mesfavoris.texteditor.placeholders.PathPlaceholder;
import mesfavoris.texteditor.placeholders.PathPlaceholdersStore;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "mesfavoris.texteditor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static PathPlaceholdersStore pathPlaceholdersStore;

	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		File storeFile = new File(getStateLocation().toFile(), "placeholders.json");
		pathPlaceholdersStore = new PathPlaceholdersStore(storeFile);
		pathPlaceholdersStore.init();
		if (pathPlaceholdersStore.get(PLACEHOLDER_HOME_NAME) == null) {
			IPath userHome = getUserHome();
			if (userHome != null) {
				pathPlaceholdersStore.add(new PathPlaceholder(PLACEHOLDER_HOME_NAME, userHome));
			}
		}
	}

	private IPath getUserHome() {
		String userHome = System.getProperty("user.home");
		if (userHome == null) {
			return null;
		}
		return new Path(userHome);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			pathPlaceholdersStore.close();
		} finally {
			plugin = null;
			super.stop(context);
		}
	}

	public static PathPlaceholdersStore getPathPlaceholdersStore() {
		return pathPlaceholdersStore;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}	
	
}
