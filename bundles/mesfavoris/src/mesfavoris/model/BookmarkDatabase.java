package mesfavoris.model;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;

import mesfavoris.BookmarksException;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.validation.AcceptAllBookmarksModificationValidator;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.model.modification.BookmarksTreeModifier;
import mesfavoris.model.modification.IBookmarksModificationValidator;

public class BookmarkDatabase {
	private final String id;
	private final ListenerList<IBookmarksListener> listenerList = new ListenerList<>();
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock writeLock = rwLock.writeLock();
	private final Lock readLock = rwLock.readLock();
	private final IBookmarksModificationValidator bookmarksModificationValidator;
	private BookmarksTree bookmarksTree;

	public BookmarkDatabase(String id, BookmarksTree bookmarksTree) {
		this(id, bookmarksTree, new AcceptAllBookmarksModificationValidator());
	}
	
	public BookmarkDatabase(String id, BookmarksTree bookmarksTree,
			IBookmarksModificationValidator bookmarksModificationValidator) {
		this.id = id;
		this.bookmarksTree = bookmarksTree;
		this.bookmarksModificationValidator = bookmarksModificationValidator;
	}

	public IBookmarksModificationValidator getBookmarksModificationValidator() {
		return bookmarksModificationValidator;
	}
	
	public String getId() {
		return id;
	}

	public void modify(IBookmarksOperation operation) throws BookmarksException {
		modify(operation, (bookmarksTree) -> {
		});
	}

	public void modify(IBookmarksOperation operation, Consumer<BookmarksTree> afterCommit) throws BookmarksException {
		modify(LockMode.PESSIMISTIC, operation, true, afterCommit);
	}

	public void modify(LockMode lockMode, IBookmarksOperation operation) throws BookmarksException {
		modify(lockMode, operation, true, (bookmarksTree) -> {
		});
	}

	public void modify(LockMode lockMode, IBookmarksOperation operation, boolean validateModifications) throws BookmarksException {
		modify(lockMode, operation, validateModifications, (bookmarksTree) -> {
		});
	}
	
	public void modify(LockMode lockMode, IBookmarksOperation operation, boolean validateModifications, Consumer<BookmarksTree> afterCommit)
			throws BookmarksException {
		switch (lockMode) {
		case OPTIMISTIC:
			modifyWithOptimisticLocking(operation, validateModifications, afterCommit);
			break;
		case PESSIMISTIC:
			modifyWithPessimisticLocking(operation, validateModifications, afterCommit);
			break;
		}
	}

	private void modifyWithOptimisticLocking(IBookmarksOperation operation, boolean validateModifications, Consumer<BookmarksTree> afterCommit)
			throws BookmarksException {
		List<BookmarksModification> modifications = Collections.emptyList();
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(bookmarksTree);
		operation.exec(bookmarksTreeModifier);
		if (validateModifications) {
			validateModifications(bookmarksTreeModifier.getModifications());
		}
		try {
			writeLock.lock();
			if (bookmarksTree != bookmarksTreeModifier.getOriginalTree()) {
				// tree has been modified ...
				throw new OptimisticLockException();
			}
			bookmarksTreeModifier.optimize();
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

	private void modifyWithPessimisticLocking(IBookmarksOperation operation, boolean validateModifications, Consumer<BookmarksTree> afterCommit)
			throws BookmarksException {
		List<BookmarksModification> modifications = Collections.emptyList();
		try {
			writeLock.lock();
			BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(bookmarksTree);
			operation.exec(bookmarksTreeModifier);
			if (bookmarksTree != bookmarksTreeModifier.getOriginalTree()) {
				throw new BookmarksException("BookmarksDatabase.modify is not reentrant");
			}
			if (validateModifications) {
				validateModifications(bookmarksTreeModifier.getModifications());
			}
			modifications = bookmarksTreeModifier.getModifications();
			this.bookmarksTree = bookmarksTreeModifier.getCurrentTree();
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

	private void validateModifications(List<BookmarksModification> modifications) throws BookmarksException {
		for (BookmarksModification modification : modifications) {
			IStatus status = bookmarksModificationValidator.validateModification(modification);
			if (!status.isOK()) {
				throw new BookmarksException(status);
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
		for (IBookmarksListener listener : listenerList) {
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
