package mesfavoris.internal.views;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class ProxySelectionProvider implements ISelectionProvider {
	private ISelectionProvider currentSelectionProvider;
	private final ListenerList<ISelectionChangedListener> listeners = new ListenerList<>(ListenerList.IDENTITY);
	private final ISelectionChangedListener proxySelectionChangedListener = event -> fireSelectionChanged();

	
	public void setCurrentSelectionProvider(ISelectionProvider selectionProvider) {
		if (currentSelectionProvider == selectionProvider) {
			return;
		}
		if (currentSelectionProvider != null) {
			currentSelectionProvider.removeSelectionChangedListener(proxySelectionChangedListener);
		}
		this.currentSelectionProvider = selectionProvider;
		if (currentSelectionProvider != null) {
			currentSelectionProvider.addSelectionChangedListener(proxySelectionChangedListener);
		}
		fireSelectionChanged();
	}

	private void fireSelectionChanged() {
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		for (ISelectionChangedListener listener : listeners) {
			listener.selectionChanged(event);
		}
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		if (currentSelectionProvider != null) {
			return currentSelectionProvider.getSelection();
		}
		return StructuredSelection.EMPTY;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		if (currentSelectionProvider == null) {
			return;
		}
		currentSelectionProvider.setSelection(selection);
	}

}