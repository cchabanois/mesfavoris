package mesfavoris.tests.commons.bookmarks;

import java.util.concurrent.atomic.AtomicInteger;

import mesfavoris.model.BookmarkId;

public class IncrementalIDGenerator implements IDGenerator {
	private final AtomicInteger nextId = new AtomicInteger(0);
	
	public BookmarkId newId() {
		return new BookmarkId(Integer.toString(nextId.incrementAndGet()));
	}
	
}
