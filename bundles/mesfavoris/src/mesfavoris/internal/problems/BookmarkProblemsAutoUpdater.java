package mesfavoris.internal.problems;

import static mesfavoris.problems.BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Provider;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.EventHandler;

import com.google.common.collect.Iterables;

import mesfavoris.internal.placeholders.PathPlaceholderResolver;
import mesfavoris.internal.service.operations.CheckBookmarkPropertiesOperation;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.topics.BookmarksEvents;

public class BookmarkProblemsAutoUpdater {
	private final IEventBroker eventBroker;
	private final EventHandler pathPlaceholderChangedEventHandler = event -> pathPlaceholderChanged(
			(String) event.getProperty("name"));
	private final BookmarkDatabase bookmarkDatabase;
	private final BookmarkProblemsDatabase bookmarkProblemsDatabase;
	private final Provider<Set<String>> pathPropertiesProvider;
	private final CheckBookmarkPropertiesOperation checkBookmarkPropertiesOperation;
	private final IBookmarksListener bookmarksListener = modifications -> {
		bookmarksAdded(modifications.stream().filter(modification -> modification instanceof BookmarksAddedModification)
				.map(modification -> (BookmarksAddedModification) modification).collect(Collectors.toList()));
		bookmarksPropertiesModified(modifications.stream()
				.filter(modification -> modification instanceof BookmarkPropertiesModification)
				.map(modification -> (BookmarkPropertiesModification) modification).collect(Collectors.toList()));
	};

	public BookmarkProblemsAutoUpdater(IEventBroker eventBroker, BookmarkDatabase bookmarkDatabase,
			BookmarkProblemsDatabase bookmarkProblemsDatabase, Provider<Set<String>> pathPropertiesProvider,
			CheckBookmarkPropertiesOperation checkBookmarkPropertiesOperation) {
		this.eventBroker = eventBroker;
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkProblemsDatabase = bookmarkProblemsDatabase;
		this.pathPropertiesProvider = pathPropertiesProvider;
		this.checkBookmarkPropertiesOperation = checkBookmarkPropertiesOperation;
	}

	public void init() {
		bookmarkDatabase.addListener(bookmarksListener);
		eventBroker.subscribe(BookmarksEvents.TOPIC_PATH_PLACEHOLDERS_CHANGED, pathPlaceholderChangedEventHandler);
	}

	public void close() {
		eventBroker.unsubscribe(pathPlaceholderChangedEventHandler);
		bookmarkDatabase.removeListener(bookmarksListener);
	}

	private void pathPlaceholderChanged(String pathPlaceholderName) {
		StreamSupport.stream(bookmarkProblemsDatabase.spliterator(), false)
				.filter(bookmarkProblem -> bookmarkProblem.getProblemType() == TYPE_PLACEHOLDER_UNDEFINED)
				.filter(bookmarkProblem -> hasPropertyUsingPathPlaceholder(bookmarkProblem, pathPlaceholderName))
				.forEach(bookmarkProblem -> {
					Optional<BookmarkProblem> newProblem = getPathPlaceholderProblem(bookmarkProblem.getBookmarkId());
					if (!newProblem.isPresent()) {
						bookmarkProblemsDatabase.delete(bookmarkProblem);
					}
				});
	}

	private boolean hasPropertyUsingPathPlaceholder(BookmarkProblem bookmarkProblem, String pathPlaceholderName) {
		Map<String, String> properties = bookmarkProblem.getProperties();
		for (String pathProperty : pathPropertiesProvider.get()) {
			String propValue = properties.get(pathProperty);
			if (propValue != null
					&& pathPlaceholderName.equals(PathPlaceholderResolver.getPlaceholderName(propValue))) {
				return true;
			}
		}
		return false;
	}

	private Optional<BookmarkProblem> getPathPlaceholderProblem(BookmarkId bookmarkId) {
		return checkBookmarkPropertiesOperation.getBookmarkPropertiesProblems(bookmarkId).stream()
				.filter(bookmarkProblem -> bookmarkProblem.getProblemType() == TYPE_PLACEHOLDER_UNDEFINED).findFirst();
	}

	private void bookmarksAdded(List<BookmarksAddedModification> modifications) {
		modifications.stream().flatMap(modification -> modification.getBookmarks().stream())
				.map(bookmark -> bookmark.getId())
				.flatMap(bookmarkId -> checkBookmarkPropertiesOperation.getBookmarkPropertiesProblems(bookmarkId)
						.stream())
				.filter(bookmarkProblem -> !bookmarkProblemsDatabase
						.getBookmarkProblem(bookmarkProblem.getBookmarkId(), bookmarkProblem.getProblemType())
						.isPresent())
				.forEach(bookmarkProblem -> bookmarkProblemsDatabase.add(bookmarkProblem));
	}

	private void bookmarksPropertiesModified(List<BookmarkPropertiesModification> modifications) {
		for (BookmarkPropertiesModification modification : modifications) {
			Optional<BookmarkProblem> bookmarkProblem = bookmarkProblemsDatabase
					.getBookmarkProblem(modification.getBookmarkId(), BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
			if (bookmarkProblem.isPresent()) {
				updateObsoletePropertiesBookmarkProblem(modification, bookmarkProblem.get());
			}
			bookmarkProblem = bookmarkProblemsDatabase.getBookmarkProblem(modification.getBookmarkId(),
					BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE);
			if (bookmarkProblem.isPresent()) {
				updateObsoletePropertiesBookmarkProblem(modification, bookmarkProblem.get());
			}
		}
	}

	private void updateObsoletePropertiesBookmarkProblem(BookmarkPropertiesModification modification,
			BookmarkProblem bookmarkProblem) {
		Map<String, String> newProperties = null;
		for (String propertyName : Iterables.concat(modification.getModifiedProperties(), modification.getAddedProperties())) {
			if (Objects.equals(bookmarkProblem.getProperties().get(propertyName), modification.getTargetTree()
					.getBookmark(modification.getBookmarkId()).getPropertyValue(propertyName))) {
				if (newProperties == null) {
					newProperties = new HashMap<>(bookmarkProblem.getProperties());
				}
				newProperties.remove(propertyName);
			}
		}
		if (newProperties == null) {
			return;
		}
		if (newProperties.isEmpty()) {
			bookmarkProblemsDatabase.delete(bookmarkProblem);
		} else {
			BookmarkProblem newBookmarkProblem = new BookmarkProblem(bookmarkProblem.getBookmarkId(),
					bookmarkProblem.getProblemType(), newProperties);
			bookmarkProblemsDatabase.add(newBookmarkProblem);
		}
	}

}
