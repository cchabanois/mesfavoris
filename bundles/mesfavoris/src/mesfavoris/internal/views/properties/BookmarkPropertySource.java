package mesfavoris.internal.views.properties;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import mesfavoris.BookmarksException;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.StatusHelper;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkType;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkTypes;
import mesfavoris.internal.views.properties.PropertyLabelProvider.PropertyIcon;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblems;

public class BookmarkPropertySource implements IPropertySource {
	private static final String CATEGORY_UNKNOWN = "unknown";
	private final BookmarkId bookmarkId;
	private final IBookmarkProblems bookmarkProblems;
	private final PluginBookmarkTypes pluginBookmarkTypes;
	private final BookmarkDatabase bookmarkDatabase;

	public BookmarkPropertySource(BookmarkDatabase bookmarkDatabase, IBookmarkProblems bookmarkProblems,
			BookmarkId bookmarkId) {
		this.bookmarkId = bookmarkId;
		this.bookmarkProblems = bookmarkProblems;
		this.pluginBookmarkTypes = BookmarksPlugin.getDefault().getPluginBookmarkTypes();
		this.bookmarkDatabase = bookmarkDatabase;
	}

	@Override
	public Object getEditableValue() {
		return bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
	}

	private String getCategory(String propertyName) {
		return getPluginBookmarkType(propertyName).map(pluginBookmarkType -> pluginBookmarkType.getName())
				.orElse(CATEGORY_UNKNOWN);
	}

	private Optional<PluginBookmarkType> getPluginBookmarkType(String propertyName) {
		return pluginBookmarkTypes.getBookmarkTypes().stream()
				.filter(pluginBookmarkType -> pluginBookmarkType.getPropertyDescriptor(propertyName) != null)
				.findFirst();
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		List<IPropertyDescriptor> propertyDescriptors = bookmark.getProperties().keySet().stream()
				.map(propertyName -> getPropertyDescriptorFromBookmarkProperty(propertyName))
				.collect(Collectors.toList());
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE);
		if (problem.isPresent()) {
			propertyDescriptors.addAll(problem.get().getProperties().keySet().stream()
					.filter(propertyName -> bookmark.getPropertyValue(propertyName) == null)
					.map(propertyName -> getPropertyDescriptorFromProblemProperty(propertyName, PropertyIcon.INFO))
					.collect(Collectors.toList()));
		}
		problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		if (problem.isPresent()) {
			propertyDescriptors.addAll(problem.get().getProperties().keySet().stream()
					.filter(propertyName -> bookmark.getPropertyValue(propertyName) == null)
					.map(propertyName -> getPropertyDescriptorFromProblemProperty(propertyName, PropertyIcon.WARNING))
					.collect(Collectors.toList()));
		}
		return propertyDescriptors.toArray(new IPropertyDescriptor[0]);
	}

	private IPropertyDescriptor getPropertyDescriptorFromBookmarkProperty(String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, propertyName);
		if (hasPlaceholderUndefinedProblem(propertyName)) {
			propertyDescriptor = new PropertyDescriptor(propertyName, propertyName);
			propertyDescriptor.setLabelProvider(new PropertyLabelProvider(false, PropertyIcon.WARNING));
		} else if (hasPropertyMayUpdateProblem(propertyName)) {
			propertyDescriptor = new ObsoletePropertyPropertyDescriptor(
					getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE).get(), propertyName);
		} else if (hasPropertyNeedUpdateProblem(propertyName)) {
			propertyDescriptor = new ObsoletePropertyPropertyDescriptor(
					getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE).get(), propertyName);
		} else {
			propertyDescriptor = new PropertyDescriptor(propertyName, propertyName);
		}
		propertyDescriptor.setCategory(getCategory(propertyName));
		return propertyDescriptor;
	}

	private boolean hasPlaceholderUndefinedProblem(String propertyName) {
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED);
		Optional<String> value = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName));
		return value.isPresent();
	}

	private boolean hasPropertyMayUpdateProblem(String propertyName) {
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE);
		Optional<String> value = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName));
		return value.isPresent();
	}

	private boolean hasPropertyNeedUpdateProblem(String propertyName) {
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		Optional<String> value = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName));
		return value.isPresent();
	}

	private IPropertyDescriptor getPropertyDescriptorFromProblemProperty(String propertyName,
			PropertyIcon propertyIcon) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, propertyName + " (New value)");
		propertyDescriptor.setLabelProvider(new PropertyLabelProvider(true, propertyIcon));
		propertyDescriptor.setCategory(getCategory(propertyName));
		return propertyDescriptor;
	}

	@Override
	public Object getPropertyValue(Object id) {
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		String propertyName = (String) id;
		String propertyValue = bookmark.getPropertyValue(propertyName);
		if (propertyValue != null) {
			return getPropertyValueFromBookmark(propertyName);
		} else {
			return getPropertyValueFromProblem(propertyName);
		}

	}

	private Object getPropertyValueFromProblem(String propertyName) {
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		return problem.get().getProperties().get(propertyName);
	}

	private Object getPropertyValueFromBookmark(String propertyName) {
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		String propertyValue = bookmark.getPropertyValue(propertyName);
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE);
		if (!problem.isPresent()) {
			problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		}
		String updatedValue = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName))
				.orElse(null);
		if (updatedValue != null) {
			return new ObsoletePropertyPropertySource(propertyName, propertyValue, updatedValue);
		} else {
			return propertyValue;
		}
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		try {
			bookmarkDatabase.modify(bookmarksTreeModifier -> {
				String propertyName = (String) id;
				String propertyValue = (String) value;
				bookmarksTreeModifier.setPropertyValue(bookmarkId, propertyName, propertyValue);
			});
		} catch (BookmarksException e) {
			StatusHelper.logWarn("Could not set property value", e);
		}
	}

	public Optional<BookmarkProblem> getBookmarkProblem(String problemType) {
		return bookmarkProblems.getBookmarkProblem(bookmarkId, problemType);
	}

}
