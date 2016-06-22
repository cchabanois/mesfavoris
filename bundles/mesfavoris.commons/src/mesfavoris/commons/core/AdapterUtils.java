package mesfavoris.commons.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

/**
 * Some utility static methods concerning adapters
 * 
 * @author cedric
 * 
 */
public class AdapterUtils {

	public static <T> T getAdapter(Object element, Class<T> adapterType,
			boolean load) {
		if (adapterType.isInstance(element))
			return adapterType.cast(element);
		if (element instanceof IAdaptable) {
			Object adapted = ((IAdaptable) element).getAdapter(adapterType);
			if (adapterType.isInstance(adapted))
				return adapterType.cast(adapted);
		}
		if (load) {
			Object adapted = Platform.getAdapterManager().loadAdapter(element,
					adapterType.getName());
			if (adapterType.isInstance(adapted))
				return adapterType.cast(adapted);
		} else {
			Object adapted = Platform.getAdapterManager().getAdapter(element,
					adapterType);
			if (adapterType.isInstance(adapted))
				return adapterType.cast(adapted);
		}
		return null;
	}

	public static <T> T getAdapter(Object element, Class<T> adapterType) {
		return getAdapter(element, adapterType, false);
	}

}
