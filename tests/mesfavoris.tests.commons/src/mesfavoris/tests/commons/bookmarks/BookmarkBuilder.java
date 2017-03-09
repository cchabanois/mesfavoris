package mesfavoris.tests.commons.bookmarks;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;

public abstract class BookmarkBuilder<T extends Bookmark>  {
	protected final BookmarkId id;
	protected final Map<String, String> properties = new HashMap<>();
	
	public BookmarkBuilder(BookmarkId bookmarkId, String name) {
		this.id = bookmarkId;
		properties.put(Bookmark.PROPERTY_NAME, name);		
	}
	
	public BookmarkBuilder(String name) {
		this(new BookmarkId(name), name);
	}
	
	public BookmarkBuilder<T> withProperty(String name, String value) {
		properties.put(name, value);
		return this;
	}
 	
	public BookmarkBuilder<T> created(Instant created) {
		properties.put(Bookmark.PROPERTY_CREATED, created.toString());
		return this;
	}
	
	public abstract T build();
	
	public static BookmarkBuilder<Bookmark> bookmark(String name) {
		return bookmark(new BookmarkId(name), name);
	}
	
	public static BookmarkBuilder<Bookmark> bookmark(BookmarkId bookmarkId, String name) {
		 return new BookmarkBuilder<Bookmark>(bookmarkId, name) {

			@Override
			public Bookmark build() {
				return new Bookmark(id, properties);
			}
			 
		 };
	}

	public static BookmarkBuilder<BookmarkFolder> bookmarkFolder(String name) {
		return bookmarkFolder(new BookmarkId(name), name);
	}	
	
	public static BookmarkBuilder<BookmarkFolder> bookmarkFolder(BookmarkId bookmarkId, String name) {
		 return new BookmarkBuilder<BookmarkFolder>(bookmarkId, name) {

			@Override
			public BookmarkFolder build() {
				return new BookmarkFolder(id, properties);
			}
			 
		 };
	}
	
}
