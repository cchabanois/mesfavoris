package mesfavoris.internal.views;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class PreviousActivePartListener implements IPartListener {
	private IWorkbenchPart previousActivePart;

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (!(part instanceof BookmarksView)) {
			this.previousActivePart = part;
		}
	}

	public IWorkbenchPart getPreviousActivePart() {
		return previousActivePart;
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (previousActivePart == part) {
			previousActivePart = null;
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

}