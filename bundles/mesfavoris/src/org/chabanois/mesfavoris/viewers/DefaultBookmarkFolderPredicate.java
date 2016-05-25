package org.chabanois.mesfavoris.viewers;

import java.util.function.Predicate;

import org.chabanois.mesfavoris.model.Bookmark;
import org.chabanois.mesfavoris.workspace.DefaultBookmarkFolderManager;

public class DefaultBookmarkFolderPredicate implements Predicate<Bookmark> {
	private final DefaultBookmarkFolderManager defaultBookmarkFolderManager;
	
	public DefaultBookmarkFolderPredicate(DefaultBookmarkFolderManager defaultBookmarkFolderManager) {
		this.defaultBookmarkFolderManager = defaultBookmarkFolderManager;
	}
	
	@Override
	public boolean test(Bookmark bookmark) {
		return defaultBookmarkFolderManager.getDefaultFolder() == bookmark;
	}

}
