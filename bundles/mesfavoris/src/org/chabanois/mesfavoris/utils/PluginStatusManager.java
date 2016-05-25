package org.chabanois.mesfavoris.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * StatusManager for a given plugin. It uses {@link StatusManager} but add
 * methods to handle statuses only once
 * 
 * @author cedric
 * 
 */
public class PluginStatusManager {
	private static int DEFAULT_TTL = 500;
	private Set<StatusAtTime> statuses = new HashSet<StatusAtTime>();
	private String pluginId;

	public PluginStatusManager(String pluginId) {
		this.pluginId = pluginId;
	}

	/**
	 * log an error
	 * 
	 * @param msg
	 * @param t
	 */
	public void logError(String msg, Throwable t) {
		handleStatus(IStatus.ERROR, msg, t, StatusManager.LOG);
	}

	/**
	 * show an error and optionally log it
	 * 
	 * @param msg
	 * @param t
	 * @param log
	 */
	public void showError(String msg, Throwable t, boolean log) {
		int style = StatusManager.SHOW;
		if (log) {
			style = style | StatusManager.LOG;
		}
		handleStatus(IStatus.ERROR, msg, t, style);
	}

	/**
	 * log a warning
	 * 
	 * @param msg
	 * @param t
	 */
	public void logWarn(String msg, Throwable t) {
		handleStatus(IStatus.WARNING, msg, t, StatusManager.LOG);
	}

	/**
	 * log an error but only once. If logErrorOnce is called again with the same
	 * exception (same message and same stacktrace), it will not be logged again
	 * 
	 * @param msg
	 * @param t
	 * @param ttl
	 */
	public void logErrorOnce(String msg, Throwable t, long ttl) {
		handleStatusOnce(IStatus.ERROR, msg, t, StatusManager.LOG, ttl);
	}

	/**
	 * log a warning but only once. If logWarnOnce is called again with the same
	 * exception (same message and same stacktrace), it will not be logged again
	 * 
	 * @param msg
	 * @param t
	 * @param ttl
	 */
	public void logWarnOnce(String msg, Throwable t, long ttl) {
		handleStatusOnce(IStatus.WARNING, msg, t, StatusManager.LOG, ttl);
	}

	/**
	 * handle given status
	 * 
	 * @param statusSeverity
	 * @param statusMessage
	 * @param statusThrowable
	 * @param style
	 * @param onlyOnce
	 */
	public void handleStatus(int statusSeverity, String statusMessage,
			Throwable statusThrowable, int style) {
		if (statusMessage == null) {
			if (statusThrowable != null)
				statusMessage = statusThrowable.toString();
			else
				statusMessage = "";
		}
		IStatus status = createStatus(statusSeverity, statusMessage, statusThrowable);
		handleStatus(status, style);
	}

	/**
	 * handle given status but only once. If handleStatusOnce is called again
	 * with the same status severity, the same status message, throwable and
	 * style, the status will not be handled again
	 * 
	 * @param statusSeverity
	 * @param statusMessage
	 * @param statusThrowable
	 * @param style
	 * @param ttl
	 */
	public void handleStatusOnce(int statusSeverity, String statusMessage,
			Throwable statusThrowable, int style, long ttl) {
		if (statusMessage == null) {
			if (statusThrowable != null)
				statusMessage = statusThrowable.toString();
			else
				statusMessage = "";
		}
		IStatus status = createStatus(statusSeverity, statusMessage, statusThrowable);
		handleStatusOnce(status, style, ttl);
	}

	/**
	 * Create an error status
	 * 
	 * @param message
	 * @param e
	 * @return
	 */
	public IStatus createErrorStatus(String message, Throwable e) {
		return createStatus(IStatus.ERROR, message, e);
	}

	/**
	 * Create a status
	 * 
	 * @param severity
	 * @param message
	 * @param e
	 * @return
	 */
	public IStatus createStatus(int severity, String message, Throwable e) {
		return new Status(severity, pluginId, Status.OK, message, e);
	}

	public static void handleStatus(IStatus status, int style) {
		StatusManager.getManager().handle(status, style);
	}

	private synchronized void handleStatusOnce(IStatus status, int style,
			long ttl) {
		removeOldStatuses();
		StatusAtTime statusAtTime = new StatusAtTime(status, style, ttl);
		if (statuses.contains(statusAtTime)) {
			statusAtTime.setLastEmitted();
			return;
		} else {
			statuses.add(statusAtTime);
			handleStatus(status, style);
		}
	}

	private synchronized void removeOldStatuses() {
		long currentTime = System.currentTimeMillis();
		for (Iterator<StatusAtTime> it = statuses.iterator(); it.hasNext();) {
			StatusAtTime statusAtTime = it.next();
			if (statusAtTime.getLastEmitted() - currentTime > statusAtTime
					.getTtl()) {
				it.remove();
			}
		}
	}

	/**
	 * keep the status and style handled by the status manager at a given time
	 * 
	 * @author cedric
	 * 
	 */
	private class StatusAtTime {
		private IStatus status;
		private long lastEmitted;
		private int style;
		private long ttl;

		public StatusAtTime(IStatus status, int style, long ttl) {
			this.status = status;
			this.style = style;
			this.lastEmitted = System.currentTimeMillis();
			this.ttl = ttl;
		}

		public long getLastEmitted() {
			return lastEmitted;
		}

		public void setLastEmitted() {
			this.lastEmitted = System.currentTimeMillis();
		}

		public long getTtl() {
			return ttl;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((status == null) ? 0 : hashCode(status));
			result = prime * result + style;
			return result;
		}

		public int hashCode(IStatus status) {
			final int prime = 31;
			int result = 1;
			result = prime * result + status.getCode();
			result = prime * result + status.getMessage().hashCode();
			result = prime * result + status.getPlugin().hashCode();
			result = prime * result + status.getSeverity();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final StatusAtTime other = (StatusAtTime) obj;
			if (style != other.style)
				return false;
			if (status == null) {
				if (other.status != null)
					return false;
			} else if (!areSimilar(status, other.status))
				return false;
			return true;
		}

		private boolean areSimilar(IStatus status1, IStatus status2) {
			// plugin is required
			if (!status1.getPlugin().equals(status2.getPlugin())) {
				return false;
			}

			// message is required
			if (!status1.getMessage().equals(status2.getMessage())) {
				return false;
			}

			// serverity is required
			if (status1.getSeverity() != status2.getSeverity()) {
				return false;
			}

			// code is required
			if (status1.getCode() != status2.getCode()) {
				return false;
			}

			// exception is optional
			if (!areSimilar(status1.getException(), status2.getException())) {
				return false;
			}

			if (!areSimilar(status1.getChildren(), status2.getChildren())) {
				return false;
			}

			return true;
		}

		private boolean areSimilar(IStatus[] statuses1, IStatus[] statuses2) {
			if ((statuses1 == null && statuses2 != null)
					|| (statuses1 != null && statuses2 == null)) {
				return false;
			}
			if (statuses1 == null && statuses2 == null) {
				return true;
			}
			if (statuses1.length != statuses2.length) {
				return false;
			}
			for (int i = 0; i < statuses1.length; i++) {
				if (!areSimilar(statuses1[i], statuses2[i])) {
					return false;
				}
			}
			return true;
		}

		private boolean areSimilar(Throwable throwable1, Throwable throwable2) {
			if ((throwable1 == null && throwable2 != null)
					|| (throwable1 != null && throwable2 == null)) {
				return false;
			}
			if (throwable1 == null && throwable2 == null) {
				return true;
			}
			if (!equals(throwable1.getMessage(), throwable2.getMessage())) {
				return false;
			}
			if (!Arrays.equals(throwable1.getStackTrace(),
					throwable2.getStackTrace())) {
				return false;
			}
			return true;
		}

		private boolean equals(String str1, String str2) {
			if ((str1 == null && str2 != null)
					|| (str1 != null && str2 == null)) {
				return false;
			}
			if (str1 == null && str2 == null) {
				return true;
			}
			return str1.equals(str2);
		}
	}

}
