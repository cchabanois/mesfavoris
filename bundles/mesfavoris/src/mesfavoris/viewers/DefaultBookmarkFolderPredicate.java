package mesfavoris.viewers;

import java.util.function.Predicate;

import mesfavoris.model.Bookmark;
import mesfavoris.workspace.DefaultBookmarkFolderManager;

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
