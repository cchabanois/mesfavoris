package mesfavoris.internal.views.properties;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkType;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkTypes;
import mesfavoris.internal.views.properties.PropertyLabelProvider.PropertyIcon;
import mesfavoris.model.Bookmark;
import mesfavoris.problems.BookmarkProblem;

public class BookmarkPropertySource implements IPropertySource {
	private static final String CATEGORY_UNKNOWN = "unknown";
	private final Bookmark bookmark;
	private final Set<BookmarkProblem> bookmarkProblems;
	private final PluginBookmarkTypes pluginBookmarkTypes;

	public BookmarkPropertySource(Bookmark bookmark, Set<BookmarkProblem> bookmarkProblems) {
		this.bookmark = bookmark;
		this.bookmarkProblems = bookmarkProblems;
		this.pluginBookmarkTypes = BookmarksPlugin.getDefault().getPluginBookmarkTypes();
	}

	@Override
	public Object getEditableValue() {
		return bookmark;
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
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_MAY_UPDATE);
		String updatedValue = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName))
				.orElse(null);
		if (updatedValue != null) {
			propertyDescriptor.setLabelProvider(new PropertyLabelProvider(false, PropertyIcon.INFO));
		}
		problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		updatedValue = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName)).orElse(null);
		if (updatedValue != null) {
			propertyDescriptor.setLabelProvider(new PropertyLabelProvider(false, PropertyIcon.WARNING));
		}
		problem = getBookmarkProblem(BookmarkProblem.TYPE_PLACEHOLDER_UNDEFINED);
		String value = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName)).orElse(null);
		if (value != null) {
			propertyDescriptor.setLabelProvider(new PropertyLabelProvider(false, PropertyIcon.WARNING));
		}
		propertyDescriptor.setCategory(getCategory(propertyName));
		return propertyDescriptor;
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

	}

	public Optional<BookmarkProblem> getBookmarkProblem(String problemType) {
		return bookmarkProblems.stream().filter(problem -> problemType.equals(problem.getProblemType())).findAny();
	}

}
