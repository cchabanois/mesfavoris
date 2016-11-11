package mesfavoris.model;

import mesfavoris.BookmarksException;

public class OptimisticLockException extends BookmarksException {

	private static final long serialVersionUID = 6506162570729610798L;

	public OptimisticLockException() {
		super("Data has changed");
	}
	
}
