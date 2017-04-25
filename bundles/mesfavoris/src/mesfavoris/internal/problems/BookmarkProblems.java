package mesfavoris.internal.problems;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.JImmutableSet;
import org.javimmutable.collections.util.JImmutables;

import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;

public class BookmarkProblems implements Iterable<BookmarkProblem> {
	private final static BookmarkProblemComparator BOOKMARK_PROBLEM_COMPARATOR = new BookmarkProblemComparator();
	private final JImmutableMap<BookmarkId, JImmutableSet<BookmarkProblem>> map;
	private final int size;

	public BookmarkProblems() {
		this.map = JImmutables.map();
		this.size = 0;
	}

	private BookmarkProblems(JImmutableMap<BookmarkId, JImmutableSet<BookmarkProblem>> map, int size) {
		this.map = map;
		this.size = size;
	}

	public Set<BookmarkId> getBookmarksWithProblems() {
		return map.getMap().keySet();
	}

	public Set<BookmarkProblem> getBookmarkProblems(BookmarkId bookmarkId) {
		JImmutableSet<BookmarkProblem> problems = map.get(bookmarkId);
		if (problems == null) {
			return Collections.emptySet();
		} else {
			return problems.getSet();
		}
	}

	public Optional<BookmarkProblem> getBookmarkProblem(BookmarkId bookmarkId, String problemType) {
		return getBookmarkProblems(bookmarkId).stream().filter(problem -> problemType.equals(problem.getProblemType()))
				.findAny();
	}

	public int size() {
		return size;
	}

	public BookmarkProblems add(BookmarkProblem problem) {
		JImmutableSet<BookmarkProblem> problems = map.get(problem.getBookmarkId());
		if (problems == null) {
			problems = JImmutables.sortedSet(BOOKMARK_PROBLEM_COMPARATOR);
		}
		JImmutableSet<BookmarkProblem> newProblems = problems.delete(problem).insert(problem);
		int newSize = size + newProblems.size() - problems.size();
		return newBookmarkProblems(map.assign(problem.getBookmarkId(), newProblems), newSize);
	}

	public BookmarkProblems delete(BookmarkProblem problem) {
		JImmutableSet<BookmarkProblem> problems = map.get(problem.getBookmarkId());
		if (problems == null) {
			return this;
		}
		int newSize = problems.contains(problem) ? size - 1 : size;
		problems = problems.delete(problem);
		if (problems.isEmpty()) {
			return newBookmarkProblems(map.delete(problem.getBookmarkId()), newSize);
		} else {
			return newBookmarkProblems(map.assign(problem.getBookmarkId(), problems), newSize);
		}
	}

	public BookmarkProblems delete(BookmarkId bookmarkId) {
		JImmutableSet<BookmarkProblem> problems = map.get(bookmarkId);
		int newSize = size;
		if (problems != null) {
			newSize -= problems.size();
		}
		return newBookmarkProblems(map.delete(bookmarkId), newSize);
	}

	private BookmarkProblems newBookmarkProblems(JImmutableMap<BookmarkId, JImmutableSet<BookmarkProblem>> map,
			int size) {
		if (map == this.map) {
			return this;
		}
		return new BookmarkProblems(map, size);
	}

	@Override
	public Iterator<BookmarkProblem> iterator() {
		return new BookmarkProblemIterator();
	}

	private static class BookmarkProblemComparator implements Comparator<BookmarkProblem> {

		@Override
		public int compare(BookmarkProblem problem1, BookmarkProblem problem2) {
//			int result = problem1.getSeverity().compareTo(problem2.getSeverity());
//			if (result != 0) {
//				return result;
//			}
			return problem1.getProblemType().compareTo(problem2.getProblemType());
		}

	}

	private class BookmarkProblemIterator implements Iterator<BookmarkProblem> {
		private final Iterator<JImmutableSet<BookmarkProblem>> setIterator;
		private Iterator<BookmarkProblem> currentIterator;

		private BookmarkProblemIterator() {
			setIterator = map.valuesCursor().iterator();
		}

		@Override
		public boolean hasNext() {
			if (currentIterator == null) {
				return setIterator.hasNext();
			}
			if (currentIterator.hasNext()) {
				return true;
			} else {
				return setIterator.hasNext();
			}
		}

		@Override
		public BookmarkProblem next() {
			if (currentIterator == null) {
				currentIterator = setIterator.next().iterator();
			}
			while (true) {
				try {
					return currentIterator.next();
				} catch (NoSuchElementException e) {
					currentIterator = setIterator.next().iterator();
				}
			}
		}

	}

}
