package mesfavoris.internal.problems.extension;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import mesfavoris.internal.StatusHelper;
import mesfavoris.problems.IBookmarkProblemHandler;

public class BookmarkProblemHandlers {
	private final static String EXTENSION_POINT = "mesfavoris.bookmarkProblems";
	private Map<String, IBookmarkProblemHandler> bookmarkProblemHandlers;

	private synchronized Map<String, IBookmarkProblemHandler> getBookmarkProblemHandlers() {
		if (bookmarkProblemHandlers != null) {
			return bookmarkProblemHandlers;
		}
		bookmarkProblemHandlers = new HashMap<>();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT);
		if (extensionPoint == null) {
			StatusHelper.logError(MessageFormat.format("no {0} extension point", EXTENSION_POINT), null);
			return bookmarkProblemHandlers;
		}
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if ("bookmarkProblem".equals(element.getName())) {
					String type = element.getAttribute("type");
					String className = element.getAttribute("class");
					try {
						IBookmarkProblemHandler problemHandler = (IBookmarkProblemHandler) element
								.createExecutableExtension("class");
						bookmarkProblemHandlers.put(type, problemHandler);
					} catch (CoreException e) {
						StatusHelper.logWarn("Could not create bookmark type element " + className, e);
					}
				}
			}
		}
		return bookmarkProblemHandlers;
	}

	public IBookmarkProblemHandler getBookmarkProblemHandler(String type) {
		return getBookmarkProblemHandlers().get(type);
	}
}
