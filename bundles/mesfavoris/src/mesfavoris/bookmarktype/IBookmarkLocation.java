package mesfavoris.bookmarktype;

import mesfavoris.model.Bookmark;

/**
 * Location corresponding to a {@link Bookmark}
 * 
 * @author cchabanois
 *
 */
public interface IBookmarkLocation {

	public static float MAX_SCORE = 1.0f;

	/**
	 * The match score.
	 * 
	 * @return the score between 0 and 1.0. It returns 1.0 if the location
	 *         corresponds exactly to the bookmark.
	 */
	default float getScore() {
		return MAX_SCORE;
	}

}
