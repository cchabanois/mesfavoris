package mesfavoris.url.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.statushandlers.StatusManager;

import mesfavoris.commons.ui.statushandlers.PluginStatusManager;

/**
 * Helper that uses StatusManager to handle statuses.
 * 
 * We will probably have to complete it with methods from org.eclipse.ui.internal.ide.StatusUtil
 * 
 * @author cedric
 * 
 */
public class StatusHelper {
	private static PluginStatusManager pluginStatusManager = new PluginStatusManager(Activator.PLUGIN_ID);

	public static void logError(String msg, Throwable t) {
		pluginStatusManager.logError(msg, t);
	}

	public static void showError(String msg, Throwable t, boolean log) {
		pluginStatusManager.showError(msg, t, log);
	}

	public static void logErrorOnce(String msg, Throwable t, long ttl) {
		pluginStatusManager.logErrorOnce(msg, t, ttl);
	}

	public static void logWarn(String msg, Throwable t) {
		pluginStatusManager.logWarn(msg, t);
	}

	public static void logWarnOnce(String msg, Throwable t, long ttl) {
		pluginStatusManager.logWarnOnce(msg, t, ttl);
	}

	public static void handleStatus(int statusSeverity, String statusMessage, Throwable statusThrowable, int style) {
		pluginStatusManager.handleStatus(statusSeverity, statusMessage, statusThrowable, style);
	}

	public static void handleStatusOnce(int statusSeverity, String statusMessage, Throwable statusThrowable, int style,
			long ttl) {
		pluginStatusManager.handleStatusOnce(statusSeverity, statusMessage, statusThrowable, style, ttl);
	}

	public static void handleStatus(IStatus status, int style) {
		StatusManager.getManager().handle(status, style);
	}

}
