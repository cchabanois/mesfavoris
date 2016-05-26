package mesfavoris.model;

import java.util.UUID;

import com.google.common.base.Preconditions;

public class BookmarkId {
	private final String id;
	
	public BookmarkId() {
		this.id = UUID.randomUUID().toString();
	}
	
	public BookmarkId(String id) {
		Preconditions.checkNotNull(id);
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		BookmarkId other = (BookmarkId) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	
}
