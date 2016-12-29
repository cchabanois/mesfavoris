package mesfavoris.internal.bookmarktypes;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;

public class PluginBookmarkType {
	private final String name;
	private final ImageDescriptor imageDescriptor;
	private final IdentityHashMap<Object, Integer> priorities = new IdentityHashMap<>();
	private final Set<IGotoBookmark> gotoBookmarks = new HashSet<>();
	private final Set<IBookmarkPropertiesProvider> propertiesProviders = new HashSet<>();;
	private final Set<IBookmarkLabelProvider> labelProviders = new HashSet<>();;
	private final Set<IBookmarkLocationProvider> locationProviders = new HashSet<>();;
	private final Set<IBookmarkMarkerAttributesProvider> markerAttributesProviders = new HashSet<>();
	private final Set<IImportTeamProject> importTeamProjects = new HashSet<>();

	public PluginBookmarkType(IConfigurationElement bookmarkTypeConfigurationElement) {
		this.name = bookmarkTypeConfigurationElement.getAttribute("name");
		String iconPath = bookmarkTypeConfigurationElement.getAttribute("icon");
		if (iconPath != null) {
			String pluginId = bookmarkTypeConfigurationElement.getContributor().getName();
			this.imageDescriptor = BookmarksPlugin.imageDescriptorFromPlugin(pluginId, iconPath);
		} else {
			this.imageDescriptor = null;
		}
		load(bookmarkTypeConfigurationElement, "propertiesProvider", propertiesProviders);
		load(bookmarkTypeConfigurationElement, "gotoBookmark", gotoBookmarks);
		load(bookmarkTypeConfigurationElement, "labelProvider", labelProviders);
		load(bookmarkTypeConfigurationElement, "locationProvider", locationProviders);
		load(bookmarkTypeConfigurationElement, "markerAttributesProvider", markerAttributesProviders);
		load(bookmarkTypeConfigurationElement, "importTeamProject", importTeamProjects);
	}

	private <T> void load(IConfigurationElement bookmarkTypeConfigurationElement, String elementName, Set<T> set) {
		IConfigurationElement[] elements = bookmarkTypeConfigurationElement.getChildren(elementName);
		for (IConfigurationElement configurationElement : elements) {
			String className = configurationElement.getAttribute("class");
			try {
				T instance = (T) configurationElement.createExecutableExtension("class");
				set.add(instance);
				int priority = getPriority(configurationElement);
				priorities.put(instance, priority);
			} catch (CoreException e) {
				StatusHelper.logWarn("Could not create bookmark type element " + className, e);
			}
		}
	}

	private int getPriority(IConfigurationElement configurationElement) {
		try {
			return Integer.parseInt(configurationElement.getAttribute("priority"));
		} catch (NumberFormatException e) {
			return 100;
		}
	}

	public Set<IGotoBookmark> getGotoBookmarks() {
		return gotoBookmarks;
	}

	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	public String getName() {
		return name;
	}

	public Set<IImportTeamProject> getImportTeamProjects() {
		return importTeamProjects;
	}

	public Set<IBookmarkLabelProvider> getLabelProviders() {
		return labelProviders;
	}

	public Set<IBookmarkLocationProvider> getLocationProviders() {
		return locationProviders;
	}

	public Set<IBookmarkMarkerAttributesProvider> getMarkerAttributesProviders() {
		return markerAttributesProviders;
	}

	public Set<IBookmarkPropertiesProvider> getPropertiesProviders() {
		return propertiesProviders;
	}

	public int getPriority(Object object) {
		Integer priority = priorities.get(object);
		if (priority == null) {
			return 100;
		} else {
			return priority;
		}
	}

}
