package mesfavoris.internal.bookmarktypes.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor;
import mesfavoris.bookmarktype.BookmarkPropertyDescriptor.BookmarkPropertyType;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertyObsolescenceSeverityProvider;
import mesfavoris.bookmarktype.IBookmarkPropertyObsolescenceSeverityProvider.ObsolescenceSeverity;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.ui.details.IBookmarkDetailPart;

public class PluginBookmarkType {
	private final String name;
	private final ImageDescriptor imageDescriptor;
	private final IConfigurationElement bookmarkTypeConfigurationElement;
	private Collection<PrioritizedElement<IGotoBookmark>> gotoBookmarks;
	private Collection<PrioritizedElement<IBookmarkPropertiesProvider>> propertiesProviders;
	private Collection<PrioritizedElement<IBookmarkLabelProvider>> labelProviders;
	private Collection<PrioritizedElement<IBookmarkLocationProvider>> locationProviders;
	private Collection<PrioritizedElement<IBookmarkMarkerAttributesProvider>> markerAttributesProviders;
	private Collection<PrioritizedElement<IImportTeamProject>> importTeamProjects;
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

	private <T> Collection<PrioritizedElement<T>> load(IConfigurationElement bookmarkTypeConfigurationElement, String elementName) {
		Set<PrioritizedElement<T>> set = new HashSet<>();
		IConfigurationElement[] elements = bookmarkTypeConfigurationElement.getChildren(elementName);
		for (IConfigurationElement configurationElement : elements) {
			String className = configurationElement.getAttribute("class");
			try {
				T instance = (T) configurationElement.createExecutableExtension("class");
				int priority = getPriority(configurationElement);
				set.add(new PrioritizedElement<T>(instance, priority));
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
				BookmarkPropertyDescriptor bookmarkPropertyDescriptor = getBookmarkPropertyDescriptor(propertyElement);
				properties.put(bookmarkPropertyDescriptor.getName(), bookmarkPropertyDescriptor);
			}
		}
	}

	private BookmarkPropertyDescriptor getBookmarkPropertyDescriptor(IConfigurationElement propertyElement) {
		String name = propertyElement.getAttribute("name");
		BookmarkPropertyType type = BookmarkPropertyType.valueOf(propertyElement.getAttribute("type").toUpperCase());
		Boolean updatable = Boolean.parseBoolean(propertyElement.getAttribute("updatable"));
		String description = propertyElement.getAttribute("description");
		IBookmarkPropertyObsolescenceSeverityProvider obsolescenceSeverityProvider = getBookmarkPropertyObsolescenceSeverityProvider(
				propertyElement);
		return new BookmarkPropertyDescriptor(name, type, updatable, description, obsolescenceSeverityProvider);
	}

	private IBookmarkPropertyObsolescenceSeverityProvider getBookmarkPropertyObsolescenceSeverityProvider(
			IConfigurationElement element) {
		String className = element.getAttribute("obsolescenceSeverityProvider");
		if (className == null) {
			String severity = element.getAttribute("obsolescenceSeverity");
			ObsolescenceSeverity obsolescenceSeverity;
			if (severity == null) {
				obsolescenceSeverity = ObsolescenceSeverity.WARNING;
			} else {
				obsolescenceSeverity = ObsolescenceSeverity.valueOf(severity);
			}
			return (bookmark, propertyName, newValue) -> obsolescenceSeverity;
		}
		try {
			return (IBookmarkPropertyObsolescenceSeverityProvider) element
					.createExecutableExtension("obsolescenceSeverityProvider");
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not create obsolescenceSeverityProvider " + className, e);
			return (bookmark, propertyName, newValue) -> ObsolescenceSeverity.WARNING;
		}
	}

	public synchronized Collection<PrioritizedElement<IGotoBookmark>> getGotoBookmarks() {
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

	public synchronized Collection<PrioritizedElement<IImportTeamProject>> getImportTeamProjects() {
		if (importTeamProjects == null) {
			importTeamProjects = load(bookmarkTypeConfigurationElement, "importTeamProject");
		}
		return importTeamProjects;
	}

	/**
	 * Must be called from the UI thread
	 * 
	 * @return
	 */
	public synchronized Collection<PrioritizedElement<IBookmarkLabelProvider>> getLabelProviders() {
		if (labelProviders == null) {
			labelProviders = load(bookmarkTypeConfigurationElement, "labelProvider");
		}
		return labelProviders;
	}

	public synchronized Collection<PrioritizedElement<IBookmarkLocationProvider>> getLocationProviders() {
		if (locationProviders == null) {
			locationProviders = load(bookmarkTypeConfigurationElement, "locationProvider");
		}
		return locationProviders;
	}

	public Collection<PrioritizedElement<IBookmarkDetailPart>> getBookmarkDetailParts() {
		// we want a new instance each time
		return load(bookmarkTypeConfigurationElement, "detailPart");
	}
	
	public synchronized Collection<PrioritizedElement<IBookmarkMarkerAttributesProvider>> getMarkerAttributesProviders() {
		if (markerAttributesProviders == null) {
			markerAttributesProviders = load(bookmarkTypeConfigurationElement, "markerAttributesProvider");
		}
		return markerAttributesProviders;
	}

	public synchronized Collection<PrioritizedElement<IBookmarkPropertiesProvider>> getPropertiesProviders() {
		if (propertiesProviders == null) {
			propertiesProviders = load(bookmarkTypeConfigurationElement, "propertiesProvider");
		}
		return propertiesProviders;
	}

	public List<BookmarkPropertyDescriptor> getPropertyDescriptors() {
		return new ArrayList<>(properties.values());
	}

	public BookmarkPropertyDescriptor getPropertyDescriptor(String propertyName) {
		return properties.get(propertyName);
	}
	                    
	public static class PrioritizedElement<T> {
		private final T element;
		private final int priority;
		
		public PrioritizedElement(T element, int priority) {
			this.element = element;
			this.priority = priority;
		}
		
		public T getElement() {
			return element;
		}
		
		public int getPriority() {
			return priority;
		}
		
	}
	
}
