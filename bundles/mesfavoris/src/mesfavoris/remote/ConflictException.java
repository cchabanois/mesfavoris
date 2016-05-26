package mesfavoris.remote;

public class ConflictException extends Exception {

	private static final long serialVersionUID = 8579685645471132350L;

	public ConflictException() {
		super();
	}
	
    public ConflictException(String message) {
        super(message);
    }
	
}
