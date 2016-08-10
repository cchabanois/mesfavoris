package mesfavoris.persistence;

public interface IBookmarksDatabaseDirtyStateTracker {

	/**
	 * Check if the bookmarks database is dirty (with some changes not yet saved
	 * to local file and remote stores)
	 * 
	 * @return true if dirty
	 */
	boolean isDirty();

}