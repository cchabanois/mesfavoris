package mesfavoris.testutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarksModification;

public class BookmarksListener implements IBookmarksListener {
	private final List<BookmarksModification> modifications = Collections.synchronizedList(new ArrayList<>());
	
	
	@Override
	public void bookmarksModified(List<BookmarksModification> modifications) {
		this.modifications.addAll(modifications);
	}

	public List<BookmarksModification> getModifications() {
		return modifications;
	}
	
	public void clear() {
		this.modifications.clear();
	}
	
}
