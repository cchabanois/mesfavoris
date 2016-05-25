package org.chabanois.mesfavoris;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class BookmarksException extends CoreException {
	private static final long serialVersionUID = 7786165398262067509L;

	public BookmarksException(IStatus status) {
		super(status);
	}

	/**
	 * Create a <code>BookmarksException</code> with an
	 * error status that contains the given message and 
	 * throwable.
	 * @param message the message for the exception
	 * @param e an associated exception
	 */
	public BookmarksException(String message, Throwable e) {
		super(new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, Status.OK, message, e));
	}	
	
	/**
	 * Create a <code>BookmarksException</code> with an
	 * error status that contains the given message.
	 * @param message the message for the exception
	 */
	public BookmarksException(String message) {
		this(message, null);
	}	
	
}
