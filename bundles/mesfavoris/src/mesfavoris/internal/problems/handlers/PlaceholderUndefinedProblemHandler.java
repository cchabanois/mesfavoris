package mesfavoris.internal.problems.handlers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.internal.placeholders.PathPlaceholdersStore;
import mesfavoris.internal.preferences.placeholders.PathPlaceholderCreationDialog;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemHandler;

public class PlaceholderUndefinedProblemHandler implements IBookmarkProblemHandler {
	private final PathPlaceholdersStore pathPlaceholdersStore;

	public PlaceholderUndefinedProblemHandler() {
		pathPlaceholdersStore = BookmarksPlugin.getDefault().getPathPlaceholdersStore();
	}

	private List<String> getPlaceholderNames(BookmarkProblem bookmarkProblem) {
		List<String> placeholderNames = bookmarkProblem.getProperties().values().stream()
				.map(propValue -> PathPlaceholderResolver.getPlaceholderName(propValue))
				.filter(placeholderName -> placeholderName != null).distinct().collect(Collectors.toList());
		return placeholderNames;
	}

	@Override
	public String getActionMessage(BookmarkProblem bookmarkProblem) {
		return "Create placeholder";
	}

	@Override
	public void handleAction(BookmarkProblem bookmarkProblem) {
		List<String> placeholderNames = getPlaceholderNames(bookmarkProblem);
		if (placeholderNames.size() == 0) {
			return;
		}
		String placeholderName = placeholderNames.get(0);
		Optional<IPath> placeholderPath = getPlaceholderPath(bookmarkProblem, placeholderName);
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		PathPlaceholderCreationDialog dialog = new PathPlaceholderCreationDialog(shell, pathPlaceholdersStore,
				Optional.of(placeholderName), placeholderPath);
		if (dialog.open() != Window.OK) {
			return;
		}
		pathPlaceholdersStore.add(dialog.getPathPlaceholder());
	}

	private Optional<IPath> getPlaceholderPath(BookmarkProblem bookmarkProblem, String placeholderName) {
		String placeholderValue = bookmarkProblem.getProperties().get("${" + placeholderName + "}");
		if (placeholderValue == null) {
			return Optional.empty();
		}
		return Optional.of(Path.fromPortableString(placeholderValue));
	}

}
