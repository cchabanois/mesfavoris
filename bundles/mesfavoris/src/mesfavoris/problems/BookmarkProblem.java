package mesfavoris.problems;

import java.util.Map;

import mesfavoris.model.BookmarkId;

public class BookmarkProblem {
	public static String TYPE_CANNOT_GOTOBOOKMARK = "cannotGotoBookmark";
	public static String TYPE_PLACEHOLDER_UNDEFINED = "placeholderUndefined";
	public static String TYPE_PROPERTIES_NEED_UPDATE = "propertiesNeedUpdate";
	public static String TYPE_LOCAL_PATH_SHARED = "localPathShared";

	private final BookmarkId bookmarkId;
	private final Severity severity;
	private final String problemType;
	private final Map<String, String> properties;

	public static enum Severity {
		// order is important
		ERROR, WARNING
	}

	public BookmarkProblem(BookmarkId bookmarkId, String problemType, Severity severity,
			Map<String, String> properties) {
		this.bookmarkId = bookmarkId;
		this.severity = severity;
		this.problemType = problemType;
		this.properties = properties;
	}

	public BookmarkId getBookmarkId() {
		return bookmarkId;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getProblemType() {
		return problemType;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarkId == null) ? 0 : bookmarkId.hashCode());
		result = prime * result + ((problemType == null) ? 0 : problemType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookmarkProblem other = (BookmarkProblem) obj;
		if (bookmarkId == null) {
			if (other.bookmarkId != null)
				return false;
		} else if (!bookmarkId.equals(other.bookmarkId))
			return false;
		if (problemType == null) {
			if (other.problemType != null)
				return false;
		} else if (!problemType.equals(other.problemType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BookmarkProblem [bookmarkId=" + bookmarkId + ", severity=" + severity + ", problemType=" + problemType
				+ "]";
	}

}
