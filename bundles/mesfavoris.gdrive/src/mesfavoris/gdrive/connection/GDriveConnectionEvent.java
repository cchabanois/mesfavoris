package mesfavoris.gdrive.connection;

public class GDriveConnectionEvent {
	private final Type type;
	
	public enum Type {
		CONNECTED, DISCONNECTED
	}
	
	public GDriveConnectionEvent(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
}
