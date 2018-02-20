package mesfavoris.internal.bookmarktypes.extension;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor;
import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertyDescriptors;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkType.PrioritizedElement;
import mesfavoris.ui.details.IBookmarkDetailPart;

public class PluginBookmarkTypes implements IBookmarkPropertyDescriptors {
	private final static String EXTENSION_POINT = "mesfavoris.bookmarkTypes";
	private List<PluginBookmarkType> bookmarkTypes;

	public synchronized List<PluginBookmarkType> getBookmarkTypes() {
		if (bookmarkTypes != null) {
			return bookmarkTypes;
		}
		bookmarkTypes = new ArrayList<>();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT);
		if (extensionPoint == null) {
			StatusHelper.logError(MessageFormat.format("no {0} extension point", EXTENSION_POINT), null);
			return bookmarkTypes;
		}
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if ("bookmarkType".equals(element.getName())) {
					PluginBookmarkType bookmarkType = new PluginBookmarkType(element);
					bookmarkTypes.add(bookmarkType);
				}
			}
		}
		return bookmarkTypes;
	}

	public PluginBookmarkType getBookmarkType(String name) {
		for (PluginBookmarkType bookmarkType : getBookmarkTypes()) {
			if (name.equals(bookmarkType.getName())) {
				return bookmarkType;
			}
		}
		return null;
	}

	@Override
	public BookmarkPropertyDescriptor getPropertyDescriptor(String propertyName) {
		Set<BookmarkPropertyDescriptor> propertyDescriptors = new HashSet<>();
		for (PluginBookmarkType bookmarkType : getBookmarkTypes()) {
			BookmarkPropertyDescriptor propertyDescriptor = bookmarkType.getPropertyDescriptor(propertyName);
			if (propertyDescriptor != null) {
				propertyDescriptors.add(propertyDescriptor);
			}
		}
		if (propertyDescriptors.isEmpty()) {
			return null;
		}
		if (propertyDescriptors.size() > 1) {
			StatusHelper.logWarn(String.format(
					"Bookmark property '%s' registered several times with different definitions", propertyName), null);
		}
		return propertyDescriptors.iterator().next();
	}

	@Override
	public List<BookmarkPropertyDescriptor> getPropertyDescriptors() {
		Map<String, BookmarkPropertyDescriptor> propertyDescriptors = new LinkedHashMap<>();
		for (PluginBookmarkType bookmarkType : getBookmarkTypes()) {
			for (BookmarkPropertyDescriptor propertyDescriptor : bookmarkType.getPropertyDescriptors()) {
				if (propertyDescriptors.containsKey(propertyDescriptor.getName())) {
					if (!propertyDescriptors.get(propertyDescriptor.getName()).equals(propertyDescriptor)) {
						StatusHelper.logWarn(String.format(
								"Bookmark property '%s' registered several times with different definitions",
								propertyDescriptor.getName()), null);
					}
				} else {
					propertyDescriptors.put(propertyDescriptor.getName(), propertyDescriptor);
				}
			}
		}
		return new ArrayList<>(propertyDescriptors.values());
	}

	public List<IGotoBookmark> getGotoBookmarks() {
		return getBookmarkTypes().stream().flatMap(bookmarkType -> bookmarkType.getGotoBookmarks().stream())
				.sorted((obj1, obj2) -> obj1.getPriority() - obj2.getPriority()).map(PrioritizedElement::getElement).collect(Collectors.toList());
	}

	/**
	 * This method must be called from the UI thread
	 * @return
	 */
	public List<IBookmarkLabelProvider> getLabelProviders() {
		return getBookmarkTypes().stream().flatMap(bookmarkType -> bookmarkType.getLabelProviders().stream())
				.sorted((obj1, obj2) -> obj1.getPriority() - obj2.getPriority()).map(PrioritizedElement::getElement).collect(Collectors.toList());
	}

	public List<IBookmarkPropertiesProvider> getPropertiesProviders() {
		return getBookmarkTypes().stream().flatMap(bookmarkType -> bookmarkType.getPropertiesProviders().stream())
				.sorted((obj1, obj2) -> obj1.getPriority() - obj2.getPriority()).map(PrioritizedElement::getElement).collect(Collectors.toList());
	}

	public List<IBookmarkLocationProvider> getLocationsProviders() {
		return getBookmarkTypes().stream().flatMap(bookmarkType -> bookmarkType.getLocationProviders().stream())
				.sorted((obj1, obj2) -> obj1.getPriority() - obj2.getPriority()).map(PrioritizedElement::getElement).collect(Collectors.toList());
	}

	public List<IBookmarkMarkerAttributesProvider> getMarkerAttributesProviders() {
		return getBookmarkTypes().stream().flatMap(bookmarkType -> bookmarkType.getMarkerAttributesProviders().stream())
				.sorted((obj1, obj2) -> obj1.getPriority() - obj2.getPriority()).map(PrioritizedElement::getElement).collect(Collectors.toList());
	}

	public List<IImportTeamProject> getImportTeamProjects() {
		return getBookmarkTypes().stream().flatMap(bookmarkType -> bookmarkType.getImportTeamProjects().stream())
				.sorted((obj1, obj2) -> obj1.getPriority() - obj2.getPriority()).map(PrioritizedElement::getElement).collect(Collectors.toList());
	}

	public List<IBookmarkDetailPart> getBookmarkDetailParts() {
		return getBookmarkTypes().stream().flatMap(bookmarkType -> bookmarkType.getBookmarkDetailParts().stream())
				.sorted((obj1, obj2) -> obj1.getPriority() - obj2.getPriority()).map(PrioritizedElement::getElement).collect(Collectors.toList());
	}

}
