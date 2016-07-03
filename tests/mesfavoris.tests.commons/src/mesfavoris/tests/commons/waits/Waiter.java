package mesfavoris.tests.commons.waits;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class Waiter {

	public static void waitUntil(String failureMessage, ICondition condition) throws TimeoutException {
		waitUntil(failureMessage, condition, Duration.ofSeconds(5), Duration.ofMillis(200));
	}

	public static void waitUntil(String failureMessage, ICondition condition, Duration timeout)
			throws TimeoutException {
		waitUntil(failureMessage, condition, timeout, Duration.ofMillis(200));
	}

	/**
	 * Waits until a condition becomes true.
	 * 
	 * @param failureMessage
	 * @param condition
	 * @param timeout
	 * @param interval
	 * @throws TimeoutException
	 */
	public static void waitUntil(String failureMessage, ICondition condition, Duration timeout, Duration interval)
			throws TimeoutException {
		long timeStart = System.nanoTime();
		long elapsedTime = 0;
		Throwable cause = null;
		while (true) {
			try {
				if (condition.test()) {
					return;
				} else {
					cause = null;
				}
			} catch (Throwable e) {
				cause = e;
			}
			sleep(interval);
			elapsedTime = System.nanoTime() - timeStart;
			if (elapsedTime >= timeout.toNanos()) {
				if (cause == null) {
					throw new ConditionTimeoutException(failureMessage);
				} else {
					throw new ConditionTimeoutException(failureMessage, cause);
				}
			}
		}
	}

	public static void sleep(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		} catch (InterruptedException e) {
			throw new RuntimeException("Could not sleep", e);
		}

	}

	@FunctionalInterface
	public static interface ICondition {

		public boolean test() throws Exception;;

	}

	public static class ConditionTimeoutException extends TimeoutException {

		private static final long serialVersionUID = -8937117315692165698L;

		public ConditionTimeoutException(String message) {
			super(message);
		}

		public ConditionTimeoutException(String message, Throwable cause) {
			super(message);
			initCause(cause);
		}
	}

}
