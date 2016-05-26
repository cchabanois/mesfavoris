package mesfavoris.model;

import java.util.List;

import mesfavoris.model.modification.BookmarksModification;

public interface IBookmarksListener {

	public void bookmarksModified(List<BookmarksModification> modifications);
	
}
