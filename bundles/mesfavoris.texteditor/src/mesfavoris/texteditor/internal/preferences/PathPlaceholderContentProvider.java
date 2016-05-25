package mesfavoris.texteditor.internal.preferences;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.collect.Lists;

import mesfavoris.texteditor.placeholders.IPathPlaceholders;

public class PathPlaceholderContentProvider implements IStructuredContentProvider {
	@Override
	public Object[] getElements(Object inputElement) {
		IPathPlaceholders pathPlaceholders = (IPathPlaceholders) inputElement;
		return Lists.newArrayList(pathPlaceholders).toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}