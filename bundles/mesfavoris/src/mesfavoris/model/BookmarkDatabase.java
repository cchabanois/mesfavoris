package mesfavoris.model;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;

import mesfavoris.BookmarksException;
import mesfavoris.StatusHelper;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksTreeModifier;

public class BookmarkDatabase {
	private final String id;
	private final ListenerList listenerList = new ListenerList();
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock writeLock = rwLock.writeLock();
	private final Lock readLock = rwLock.readLock();
	private BookmarksTree bookmarksTree;

	public BookmarkDatabase(String id, BookmarksTree bookmarksTree) {
		this.id = id;
		this.bookmarksTree = bookmarksTree;
	}

	public String getId() {
		return id;
	}

	public void modify(IBookmarksOperation operation) throws BookmarksException {
		modify(operation, (bookmarksTree) -> {
		});
	}

	public void modify(IBookmarksOperation operation, Consumer<BookmarksTree> afterCommit) throws BookmarksException {
		modify(LockMode.PESSIMISTIC, operation, afterCommit);
	}

	public void modify(LockMode lockMode, IBookmarksOperation operation) throws BookmarksException {
		modify(lockMode, operation, (bookmarksTree) -> {
		});
	}

	public void modify(LockMode lockMode, IBookmarksOperation operation, Consumer<BookmarksTree> afterCommit)
			throws BookmarksException {
		switch (lockMode) {
		case OPTIMISTIC:
			modifyWithOptimisticLocking(operation, afterCommit);
			break;
		case PESSIMISTIC:
			modifyWithPessimisticLocking(operation, afterCommit);
			break;
		}
	}

	private void modifyWithOptimisticLocking(IBookmarksOperation operation, Consumer<BookmarksTree> afterCommit)
			throws BookmarksException {
		List<BookmarksModification> modifications = Collections.emptyList();
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(bookmarksTree);
		operation.exec(bookmarksTreeModifier);
		try {
			writeLock.lock();
			if (bookmarksTree != bookmarksTreeModifier.getOriginalTree()) {
				// tree has been modified ...
				throw new OptimisticLockException();
			}
			this.bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			modifications = bookmarksTreeModifier.getModifications();
			afterCommit.accept(this.bookmarksTree);
		} finally {
			try {
				// downgrade lock to read lock
				readLock.lock();
				writeLock.unlock();
				// notify
				if (!modifications.isEmpty()) {
					fireBookmarksModified(modifications);
				}
			} finally {
				readLock.unlock();
			}
		}
	}

	private void modifyWithPessimisticLocking(IBookmarksOperation operation, Consumer<BookmarksTree> afterCommit)
			throws BookmarksException {
		List<BookmarksModification> modifications = Collections.emptyList();
		try {
			writeLock.lock();
			BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(bookmarksTree);
			operation.exec(bookmarksTreeModifier);
			if (bookmarksTree != bookmarksTreeModifier.getOriginalTree()) {
				throw new BookmarksException("BookmarksDatabase.modify is not reentrant");
			}
			this.bookmarksTree = bookmarksTreeModifier.getCurrentTree();
			modifications = bookmarksTreeModifier.getModifications();
			afterCommit.accept(this.bookmarksTree);
		} finally {
			try {
				// downgrade lock to read lock
				readLock.lock();
				writeLock.unlock();
				// notify
				if (!modifications.isEmpty()) {
					fireBookmarksModified(modifications);
				}
			} finally {
				readLock.unlock();
			}
		}
	}

	public BookmarksTree getBookmarksTree() {
		try {
			readLock.lock();
			return bookmarksTree;
		} finally {
			readLock.unlock();
		}
	}

	public void addListener(IBookmarksListener listener) {
		listenerList.add(listener);
	}

	public void removeListener(IBookmarksListener listener) {
		listenerList.remove(listener);
	}

	private void fireBookmarksModified(final List<BookmarksModification> events) {
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IBookmarksListener listener = (IBookmarksListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.bookmarksModified(events);
				}

				public void handleException(Throwable exception) {
					StatusHelper.logError("Error while firing BookmarkModified event", exception);
				}
			});
		}
	}

}
