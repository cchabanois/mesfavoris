package mesfavoris.gdrive.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import mesfavoris.gdrive.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	private static final int DEFAULT_POLL_CHANGES_INTERVAL = 30;

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(IPreferenceConstants.POLL_CHANGES_INTERVAL_PREF, DEFAULT_POLL_CHANGES_INTERVAL);
	}

}
