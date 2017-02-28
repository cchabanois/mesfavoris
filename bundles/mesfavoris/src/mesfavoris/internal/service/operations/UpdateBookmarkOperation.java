package mesfavoris.internal.service.operations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.BookmarksException;
import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;

public class UpdateBookmarkOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkPropertiesProvider bookmarkPropertiesProvider;
	private final Provider<Set<String>> nonUpdatablePropertiesProvider;

	public UpdateBookmarkOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkPropertiesProvider bookmarkPropertiesProvider, Provider<Set<String>> nonUpdatablePropertiesProvider) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkPropertiesProvider = bookmarkPropertiesProvider;
		this.nonUpdatablePropertiesProvider = nonUpdatablePropertiesProvider;
	}

	public void updateBookmark(BookmarkId bookmarkId, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) throws BookmarksException {
		Map<String, String> bookmarkProperties = new HashMap<String, String>();
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, part, selection, monitor);
		if (bookmarkProperties.isEmpty()) {
			throw new BookmarksException("Could not update bookmark from current selection");
		}
		updateBookmark(bookmarkId, bookmarkProperties);
	}

	private void updateBookmark(final BookmarkId bookmarkId, Map<String, String> properties) throws BookmarksException {
		Set<String> nonUpdatableProperties = nonUpdatablePropertiesProvider.get();
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			properties.forEach((propertyName, propertyValue) -> {
				if (!nonUpdatableProperties.contains(propertyName)) {
					bookmarksTreeModifier.setPropertyValue(bookmarkId, propertyName, propertyValue);
				}
			});
		});
	}

}
