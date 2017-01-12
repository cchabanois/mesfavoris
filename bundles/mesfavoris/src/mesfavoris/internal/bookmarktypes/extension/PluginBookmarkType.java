package mesfavoris.internal.bookmarktypes.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.bookmarktype.BookmarkPropertyDescriptor.BookmarkPropertyType;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;

public class PluginBookmarkType {
	private final String name;
	private final ImageDescriptor imageDescriptor;
	private final IConfigurationElement bookmarkTypeConfigurationElement;
	private final IdentityHashMap<Object, Integer> priorities = new IdentityHashMap<>();
	private Set<IGotoBookmark> gotoBookmarks;
	private Set<IBookmarkPropertiesProvider> propertiesProviders;
	private Set<IBookmarkLabelProvider> labelProviders;
	private Set<IBookmarkLocationProvider> locationProviders;
	private Set<IBookmarkMarkerAttributesProvider> markerAttributesProviders;
	private Set<IImportTeamProject> importTeamProjects;
	private final Map<String, BookmarkPropertyDescriptor> properties = new HashMap<>();

	public PluginBookmarkType(IConfigurationElement bookmarkTypeConfigurationElement) {
		this.bookmarkTypeConfigurationElement = bookmarkTypeConfigurationElement;
		this.name = bookmarkTypeConfigurationElement.getAttribute("name");
		String iconPath = bookmarkTypeConfigurationElement.getAttribute("icon");
		if (iconPath != null) {
			String pluginId = bookmarkTypeConfigurationElement.getContributor().getName();
			this.imageDescriptor = BookmarksPlugin.imageDescriptorFromPlugin(pluginId, iconPath);
		} else {
			this.imageDescriptor = null;
		}
		loadAttributes(bookmarkTypeConfigurationElement);
	}

	private <T> Set<T> load(IConfigurationElement bookmarkTypeConfigurationElement, String elementName) {
		Set<T> set = new HashSet<>();
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
		return Collections.unmodifiableSet(set);
	}

	private int getPriority(IConfigurationElement configurationElement) {
		try {
			return Integer.parseInt(configurationElement.getAttribute("priority"));
		} catch (NumberFormatException e) {
			return 100;
		}
	}

	private void loadAttributes(IConfigurationElement bookmarkTypeConfigurationElement) {
		IConfigurationElement[] propertiesElements = bookmarkTypeConfigurationElement.getChildren("properties");
		for (IConfigurationElement propertiesElement : propertiesElements) {
			IConfigurationElement[] propertyElements = propertiesElement.getChildren("property");
			for (IConfigurationElement propertyElement : propertyElements) {
				String name = propertyElement.getAttribute("name");
				BookmarkPropertyType type = BookmarkPropertyType
						.valueOf(propertyElement.getAttribute("type").toUpperCase());
				Boolean updatable = Boolean.parseBoolean(propertyElement.getAttribute("updatable"));
				String description = propertyElement.getAttribute("description");
				properties.put(name, new BookmarkPropertyDescriptor(name, type, updatable, description));
			}
		}
	}

	public synchronized Set<IGotoBookmark> getGotoBookmarks() {
		if (gotoBookmarks == null) {
			gotoBookmarks = load(bookmarkTypeConfigurationElement, "gotoBookmark");
		}
		return gotoBookmarks;
	}

	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	public String getName() {
		return name;
	}

	public synchronized Set<IImportTeamProject> getImportTeamProjects() {
		if (importTeamProjects == null) {
			importTeamProjects = load(bookmarkTypeConfigurationElement, "importTeamProject");
		}
		return importTeamProjects;
	}

	/**
	 * Must be called from the UI thread
	 * @return
	 */
	public synchronized Set<IBookmarkLabelProvider> getLabelProviders() {
		if (labelProviders == null) {
			labelProviders = load(bookmarkTypeConfigurationElement, "labelProvider");
		}
		return labelProviders;
	}

	public synchronized Set<IBookmarkLocationProvider> getLocationProviders() {
		if (locationProviders == null) {
			locationProviders = load(bookmarkTypeConfigurationElement, "locationProvider");
		}
		return locationProviders;
	}

	public synchronized Set<IBookmarkMarkerAttributesProvider> getMarkerAttributesProviders() {
		if (markerAttributesProviders == null) {
			markerAttributesProviders = load(bookmarkTypeConfigurationElement, "markerAttributesProvider");
		}
		return markerAttributesProviders;
	}

	public synchronized Set<IBookmarkPropertiesProvider> getPropertiesProviders() {
		if (propertiesProviders == null) {
			propertiesProviders = load(bookmarkTypeConfigurationElement, "propertiesProvider");
		}
		return propertiesProviders;
	}

	public int getPriority(Object object) {
		Integer priority = priorities.get(object);
		if (priority == null) {
			return Integer.MAX_VALUE;
		} else {
			return priority;
		}
	}

	public List<BookmarkPropertyDescriptor> getPropertyDescriptors() {
		return new ArrayList<>(properties.values());
	}
	
	public BookmarkPropertyDescriptor getPropertyDescriptor(String propertyName) {
		return properties.get(propertyName);
	}
	
}
