package mesfavoris.commons.core.jobs;

import java.time.Duration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import mesfavoris.commons.Activator;

/**
 * Job that executes until a condition becomes true or timeout expires
 * 
 * @author cchabanois
 * 
 */
public abstract class ConditionJob extends Job {
	private final Duration timeout;
	private final Duration interval;
	private final String failureMessage;

	public ConditionJob(String name, Duration timeout) {
		this(name, timeout, Duration.ofMillis(250), null);
	}

	public ConditionJob(String name, Duration timeout, Duration interval, String failureMessage) {
		super(name);
		this.timeout = timeout;
		this.interval = interval;
		this.failureMessage = failureMessage;
	}

	/**
	 * This method will be called until condition is met or timeout expires.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract boolean condition() throws Exception;

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		Exception latestException = null;
		try {
			int totalWorked = 0;
			// if we change timeout while running, it will be taken into account
			// but the visual representation of the progress will not be correct
			monitor.beginTask(getName(), (int) timeout.toMillis());
			long waitStart = System.nanoTime();

			while (System.nanoTime() - waitStart < timeout.toNanos()) {

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				try {
					if (condition()) {
						onSuccess();
						return Status.OK_STATUS;
					}
				} catch (Exception e1) {
					if (e1 instanceof OperationCanceledException) {
						throw (OperationCanceledException) e1;
					} else {
						latestException = e1;
					}
				}

				Duration elapsedTime = Duration.ofNanos(System.nanoTime() - waitStart);
				monitor.worked((int) elapsedTime.toMillis() - totalWorked);
				totalWorked = (int) elapsedTime.toMillis();
				try {
					Thread.sleep(interval.toMillis());
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
			}
		} finally {
			monitor.done();
		}
		onTimeout();
		if (failureMessage == null) {
			return Status.OK_STATUS;
		} else {
			return new Status(Status.ERROR, Activator.PLUGIN_ID, failureMessage, latestException);
		}
	}

	/**
	 * Do nothing by default. Override to do something when condition is met
	 */
	protected void onSuccess() {

	}

	/**
	 * Do nothing by default. Override to do something when job timeouts
	 */
	protected void onTimeout() {

	}
}
