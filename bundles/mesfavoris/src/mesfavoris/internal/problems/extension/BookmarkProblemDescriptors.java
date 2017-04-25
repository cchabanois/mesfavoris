package mesfavoris.internal.problems.extension;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.problems.messages.StaticBookmarkProblemErrorMessageProvider;
import mesfavoris.problems.IBookmarkProblemDescriptor;
import mesfavoris.problems.IBookmarkProblemDescriptor.Severity;
import mesfavoris.problems.IBookmarkProblemDescriptorProvider;
import mesfavoris.problems.IBookmarkProblemErrorMessageProvider;
import mesfavoris.problems.IBookmarkProblemHandler;

public class BookmarkProblemDescriptors implements IBookmarkProblemDescriptorProvider {
	private final static String EXTENSION_POINT = "mesfavoris.bookmarkProblems";
	private Map<String, IBookmarkProblemDescriptor> bookmarkProblemDescriptors;

	private synchronized Map<String, IBookmarkProblemDescriptor> getBookmarkProblemDescriptors() {
		if (bookmarkProblemDescriptors != null) {
			return bookmarkProblemDescriptors;
		}
		bookmarkProblemDescriptors = new HashMap<>();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT);
		if (extensionPoint == null) {
			StatusHelper.logError(MessageFormat.format("no {0} extension point", EXTENSION_POINT), null);
			return bookmarkProblemDescriptors;
		}
		IExtension[] extensions = extensionPoint.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				IBookmarkProblemDescriptor bookmarkProblemDescriptor = getBookmarkProblemDescriptor(element);
				if (bookmarkProblemDescriptor != null) {
					bookmarkProblemDescriptors.put(bookmarkProblemDescriptor.getProblemType(),
							bookmarkProblemDescriptor);
				}
			}
		}
		return bookmarkProblemDescriptors;
	}

	private Severity getSeverity(IConfigurationElement element) {
		String severity = element.getAttribute("severity");
		return Severity.valueOf(severity);
	}

	private IBookmarkProblemErrorMessageProvider getBookmarkProblemErrorMessageProvider(IConfigurationElement element) {
		String className = element.getAttribute("errorMessageProvider");
		if (className == null) {
			String errorMessage = element.getAttribute("errorMessage");
			if (errorMessage == null) {
				errorMessage = "Issue with this bookmark";
			}
			return new StaticBookmarkProblemErrorMessageProvider(errorMessage);
		}
		try {
			IBookmarkProblemErrorMessageProvider bookmarkProblemErrorMessageProvider = (IBookmarkProblemErrorMessageProvider) element
					.createExecutableExtension("errorMessageProvider");
			return bookmarkProblemErrorMessageProvider;
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not create errorMessageProvider " + className, e);
			return new StaticBookmarkProblemErrorMessageProvider("Issue with this bookmark");
		}
	}

	private IBookmarkProblemDescriptor getBookmarkProblemDescriptor(IConfigurationElement element) {
		if (!"bookmarkProblem".equals(element.getName())) {
			return null;
		}
		String type = element.getAttribute("type");
		Severity severity = getSeverity(element);
		IBookmarkProblemErrorMessageProvider bookmarkProblemErrorMessageProvider = getBookmarkProblemErrorMessageProvider(
				element);
		Optional<IBookmarkProblemHandler> bookmarkProblemHandler = getBookmarkProblemHandler(element);
		BookmarkProblemDescriptor bookmarkProblemDescriptor = new BookmarkProblemDescriptor(type, severity,
				bookmarkProblemErrorMessageProvider, bookmarkProblemHandler);
		return bookmarkProblemDescriptor;
	}

	private Optional<IBookmarkProblemHandler> getBookmarkProblemHandler(IConfigurationElement element) {
		String className = element.getAttribute("handler");
		if (className == null) {
			return Optional.empty();
		}
		try {
			IBookmarkProblemHandler problemHandler = (IBookmarkProblemHandler) element
					.createExecutableExtension("handler");
			return Optional.of(problemHandler);
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not create bookmark type element " + className, e);
			return Optional.empty();
		}
	}

	@Override
	public IBookmarkProblemDescriptor getBookmarkProblemDescriptor(String type) {
		IBookmarkProblemDescriptor bookmarkProblemDescriptor = getBookmarkProblemDescriptors().get(type);
		if (bookmarkProblemDescriptor == null) {
			return new BookmarkProblemDescriptor(type, Severity.ERROR,
					new StaticBookmarkProblemErrorMessageProvider("Bookmark problem of type : " + type),
					Optional.empty());
		} else {
			return bookmarkProblemDescriptor;
		}
	}
}
