package mesfavoris.bookmarktype;

import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;


public abstract class AbstractBookmarkPropertiesProvider implements IBookmarkPropertiesProvider {

	protected void putIfAbsent(Map<String, String> bookmarkProperties, String name, String value) {
		putIfAbsent(bookmarkProperties, name, ()->value);
	}
	
	protected void putIfAbsent(Map<String, String> bookmarkProperties, String name, Supplier<String> valueProvider) {
		if (bookmarkProperties.containsKey(name)) {
			return;
		}
		bookmarkProperties.put(name, valueProvider.get());
	}
	
	protected Object getFirstElement(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection)selection;
		return structuredSelection.getFirstElement();
	}
	
}
