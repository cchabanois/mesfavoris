package org.chabanois.mesfavoris.model;

import java.util.List;

import org.chabanois.mesfavoris.model.modification.BookmarksModification;

public interface IBookmarksListener {

	public void bookmarksModified(List<BookmarksModification> modifications);
	
}
