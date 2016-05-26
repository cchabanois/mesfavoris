package mesfavoris.internal.bookmarktypes;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.google.common.collect.Lists;

import mesfavoris.StatusHelper;

public class BookmarkTypeConfigElementLoader {
	private final static String EXTENSION_POINT = "mesfavoris.bookmarkType";

	@SuppressWarnings("unchecked")
	public <T> List<T> load(String elementName) {
		IExtensionPoint extPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_POINT);
		List<T> results = Lists.newArrayList();
		if (extPoint == null) {
			StatusHelper.logError(MessageFormat.format(
					"no {0} extension point", EXTENSION_POINT), null);
			return results;
		}

		List<IConfigurationElement> elements = getOrderedConfigurationElements(
				extPoint, elementName);

		for (IConfigurationElement configurationElement : elements) {
			String className = configurationElement.getAttribute("class");
			try {
				results.add((T) configurationElement
						.createExecutableExtension("class"));
			} catch (CoreException e) {
				StatusHelper.logWarn("Could not create bookmark type element "
						+ className, e);
			}

		}
		return results;
	}

	private List<IConfigurationElement> getOrderedConfigurationElements(
			IExtensionPoint extensionPoint, String elementName) {
		List<IConfigurationElement> result = new ArrayList<IConfigurationElement>();
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (elementName.equals(element.getName())) {
					result.add(element);
				}
			}
		}
		Collections.sort(result, new Comparator<IConfigurationElement>() {

			@Override
			public int compare(IConfigurationElement o1,
					IConfigurationElement o2) {
				String p1AsString = o1.getAttribute("priority");
				String p2AsString = o2.getAttribute("priority");
				if (p1AsString == null && p2AsString == null) {
					return 0;
				}				
				int p1 = Integer.parseInt(p1AsString);
				int p2 = Integer.parseInt(p2AsString);
				return p1 - p2;
			}
		});
		return result;
	}

}
