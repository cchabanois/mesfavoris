package mesfavoris.internal.views;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

public class ProxySelectionProvider implements ISelectionProvider {
	private ISelectionProvider currentSelectionProvider;
	private final ListenerList<ISelectionChangedListener> listeners = new ListenerList<>(ListenerList.IDENTITY);

	public void setCurrentSelectionProvider(ISelectionProvider selectionProvider) {
		if (currentSelectionProvider == selectionProvider) {
			return;
		}
		if (currentSelectionProvider != null) {
			for (ISelectionChangedListener listener : listeners) {
				currentSelectionProvider.removeSelectionChangedListener(listener);
			}
		}
		this.currentSelectionProvider = selectionProvider;
		if (currentSelectionProvider != null) {
			for (ISelectionChangedListener listener : listeners) {
				currentSelectionProvider.addSelectionChangedListener(listener);
			}
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
		if (currentSelectionProvider != null) {
			currentSelectionProvider.addSelectionChangedListener(listener);
		}
	}

	@Override
	public ISelection getSelection() {
		if (currentSelectionProvider != null) {
			return currentSelectionProvider.getSelection();
		}
		return new StructuredSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
		if (currentSelectionProvider != null) {
			currentSelectionProvider.removeSelectionChangedListener(listener);
		}
	}

	@Override
	public void setSelection(ISelection selection) {
		if (currentSelectionProvider == null) {
			return;
		}
		currentSelectionProvider.setSelection(selection);
	}

}