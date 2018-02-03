package mesfavoris.internal.snippets;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class Snippet implements IBookmarkLocation {
	private final String content;
	
	public Snippet(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	
}
