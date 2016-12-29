package mesfavoris.internal.bookmarktypes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.internal.StatusHelper;

public class PluginBookmarkTypes {
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

	public List<IGotoBookmark> getGotoBookmarks() {
		return getBookmarkTypes().stream()
				.flatMap(bookmarkType -> bookmarkType.getGotoBookmarks().stream())
				.sorted((obj1, obj2) -> getPriority(obj1) - getPriority(obj2))
				.collect(Collectors.toList());
	}
	
	public List<IBookmarkLabelProvider> getLabelProviders() {
		return getBookmarkTypes().stream()
				.flatMap(bookmarkType -> bookmarkType.getLabelProviders().stream())
				.sorted((obj1, obj2) -> getPriority(obj1) - getPriority(obj2))
				.collect(Collectors.toList());
	}

	public List<IBookmarkPropertiesProvider> getPropertiesProviders() {
		return getBookmarkTypes().stream()
				.flatMap(bookmarkType -> bookmarkType.getPropertiesProviders().stream())
				.sorted((obj1, obj2) -> getPriority(obj1) - getPriority(obj2))
				.collect(Collectors.toList());
	}	
	
	public List<IBookmarkLocationProvider> getLocationsProviders() {
		return getBookmarkTypes().stream()
				.flatMap(bookmarkType -> bookmarkType.getLocationProviders().stream())
				.sorted((obj1, obj2) -> getPriority(obj1) - getPriority(obj2))
				.collect(Collectors.toList());
	}	
	
	public List<IBookmarkMarkerAttributesProvider> getMarkerAttributesProviders() {
		return getBookmarkTypes().stream()
				.flatMap(bookmarkType -> bookmarkType.getMarkerAttributesProviders().stream())
				.sorted((obj1, obj2) -> getPriority(obj1) - getPriority(obj2))
				.collect(Collectors.toList());
	}	
	
	public List<IImportTeamProject> getImportTeamProjects() {
		return getBookmarkTypes().stream()
				.flatMap(bookmarkType -> bookmarkType.getImportTeamProjects().stream())
				.sorted((obj1, obj2) -> getPriority(obj1) - getPriority(obj2))
				.collect(Collectors.toList());
	}		
	
	private int getPriority(Object object) {
		int priority = 100;
		for (PluginBookmarkType bookmarkType : getBookmarkTypes()) {
			int newPriority = bookmarkType.getPriority(object);
			if (newPriority < priority) {
				priority = newPriority;
			}
		}
		return priority;
	}

}
