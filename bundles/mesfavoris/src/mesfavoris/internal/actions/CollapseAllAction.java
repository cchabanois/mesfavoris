package mesfavoris.internal.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.IUIConstants;

/**
 * An {@link Action} that will collapse all nodes in a given {@link TreeViewer}.
 *
 */
public class CollapseAllAction extends Action {

	private final TreeViewer viewer;

	public CollapseAllAction(TreeViewer viewer) {
		super("Collapse All", BookmarksPlugin.getImageDescriptor(IUIConstants.IMG_COLLAPSEALL));
		setToolTipText("Collapse All");
		setDescription("Collapse All");
		Assert.isNotNull(viewer);
		this.viewer = viewer;
	}

	@Override
	public void run() {
		try {
			viewer.getControl().setRedraw(false);
			viewer.collapseAll();
		} finally {
			viewer.getControl().setRedraw(true);
		}
	}

}
