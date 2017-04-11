package mesfavoris.internal.views;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ProxySelectionProviderTest {
	private ProxySelectionProvider proxySelectionProvider = new ProxySelectionProvider();

	@Test
	public void testSetCurrentSelectionProvider() {
		// Given
		ISelectionProvider selectionProvider = new TestSelectionProvider();
		ISelectionChangedListener listener1 = mock(ISelectionChangedListener.class);
		ISelectionChangedListener listener2 = mock(ISelectionChangedListener.class);
		proxySelectionProvider.addSelectionChangedListener(listener1);

		// When
		proxySelectionProvider.setCurrentSelectionProvider(selectionProvider);
		proxySelectionProvider.addSelectionChangedListener(listener2);
		selectionProvider.setSelection(new StructuredSelection("my selection"));

		// Then
		ArgumentCaptor<SelectionChangedEvent> captor = ArgumentCaptor.forClass(SelectionChangedEvent.class);
		verify(listener1).selectionChanged(captor.capture());
		assertThat(captor.getValue().getSelection()).isEqualTo(new StructuredSelection("my selection"));
		verify(listener2).selectionChanged(captor.capture());
		assertThat(captor.getValue().getSelection()).isEqualTo(new StructuredSelection("my selection"));

	}

	private static class TestSelectionProvider implements ISelectionProvider {

		private final List<ISelectionChangedListener> listeners = new ArrayList<>();
		private ISelection theSelection = StructuredSelection.EMPTY;

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.add(listener);
		}

		@Override
		public ISelection getSelection() {
			return theSelection;
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			listeners.remove(listener);
		}

		@Override
		public void setSelection(ISelection selection) {
			theSelection = selection;
			final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
			Object[] listenersArray = listeners.toArray();

			for (int i = 0; i < listenersArray.length; i++) {
				final ISelectionChangedListener l = (ISelectionChangedListener) listenersArray[i];
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() {
						l.selectionChanged(e);
					}
				});
			}
		}

	}

}
