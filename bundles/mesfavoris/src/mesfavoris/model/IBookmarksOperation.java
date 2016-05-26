package mesfavoris.model;

import mesfavoris.BookmarksException;
import mesfavoris.model.modification.IBookmarksTreeModifier;


/**
 * A unit of work which can be passed to {@link BookmarkDatabase} in order to
 * get write access to the model.
 */
public interface IBookmarksOperation {

	void exec(IBookmarksTreeModifier bookmarksTreeModifier) throws BookmarksException;

}