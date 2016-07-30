package mesfavoris.texteditor.internal.preferences;

import static mesfavoris.texteditor.internal.Constants.PLACEHOLDER_HOME_NAME;

import org.eclipse.jface.viewers.ViewerSorter;

import mesfavoris.texteditor.placeholders.PathPlaceholder;

public class PathPlaceholderViewerSorter extends ViewerSorter {

	@Override
	public int category(Object element) {
		PathPlaceholder pathPlaceholder = (PathPlaceholder) element;
		return isUnmodifiable(pathPlaceholder) ? 0 : 1;
	}
	
	private boolean isUnmodifiable(PathPlaceholder pathPlaceholder) {
		return PLACEHOLDER_HOME_NAME.equals(pathPlaceholder.getName());
	}	
	
}
