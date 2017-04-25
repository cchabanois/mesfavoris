package mesfavoris.internal.problems.extension;

import java.util.Optional;

import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblemDescriptor;
import mesfavoris.problems.IBookmarkProblemErrorMessageProvider;
import mesfavoris.problems.IBookmarkProblemHandler;

public class BookmarkProblemDescriptor implements IBookmarkProblemDescriptor {
	private final Severity severity;
	private final String problemType;
	private final IBookmarkProblemErrorMessageProvider bookmarkProblemErrorMessageProvider;
	private final Optional<IBookmarkProblemHandler> bookmarkProblemHandler;
	
	public BookmarkProblemDescriptor(String problemType, Severity severity,
			IBookmarkProblemErrorMessageProvider bookmarkProblemErrorMessageProvider, Optional<IBookmarkProblemHandler> bookmarkProblemHandler) {
		this.problemType = problemType;
		this.severity = severity;
		this.bookmarkProblemErrorMessageProvider = bookmarkProblemErrorMessageProvider;
		this.bookmarkProblemHandler = bookmarkProblemHandler;
	}

	@Override
	public String getProblemType() {
		return problemType;
	}

	@Override
	public Severity getSeverity() {
		return severity;
	}

	@Override
	public String getErrorMessage(BookmarkProblem bookmarkProblem) {
		return bookmarkProblemErrorMessageProvider.getErrorMessage(bookmarkProblem);
	}

	@Override
	public Optional<IBookmarkProblemHandler> getBookmarkProblemHandler() {
		return bookmarkProblemHandler;
	}
	
}
