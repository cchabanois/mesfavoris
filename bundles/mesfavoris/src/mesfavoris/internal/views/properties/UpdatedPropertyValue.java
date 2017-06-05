package mesfavoris.internal.views.properties;

public class UpdatedPropertyValue {
	private final String newValue;

	public UpdatedPropertyValue(String newValue) {
		this.newValue = newValue;
	}
	
	@Override
	public String toString() {
		return newValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
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
		UpdatedPropertyValue other = (UpdatedPropertyValue) obj;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		return true;
	}

}
