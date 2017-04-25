package mesfavoris.problems;

import java.util.Optional;
import java.util.Set;

import mesfavoris.model.BookmarkId;

public interface IBookmarkProblems extends Iterable<BookmarkProblem>, IBookmarkProblemDescriptorProvider {

	Optional<BookmarkProblem> getBookmarkProblem(BookmarkId bookmarkId, String problemType);

	Set<BookmarkProblem> getBookmarkProblems(BookmarkId bookmarkId);

	void delete(BookmarkProblem problem);

	void delete(BookmarkId bookmarkId);
	
	void add(BookmarkProblem problem);

	int size();

}