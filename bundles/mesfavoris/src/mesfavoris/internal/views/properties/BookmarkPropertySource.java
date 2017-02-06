package mesfavoris.internal.views.properties;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import mesfavoris.model.Bookmark;
import mesfavoris.problems.BookmarkProblem;

public class BookmarkPropertySource implements IPropertySource {
	private final Bookmark bookmark;
	private final Set<BookmarkProblem> bookmarkProblems;

	public BookmarkPropertySource(Bookmark bookmark, Set<BookmarkProblem> bookmarkProblems) {
		this.bookmark = bookmark;
		this.bookmarkProblems = bookmarkProblems;
	}

	@Override
	public Object getEditableValue() {
		return bookmark;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		List<IPropertyDescriptor> propertyDescriptors = bookmark.getProperties().keySet().stream()
				.map(propertyName -> getPropertyDescriptorFromBookmarkProperty(propertyName, problem))
				.collect(Collectors.toList());
		if (problem.isPresent()) {
			propertyDescriptors.addAll(problem.get().getProperties().keySet().stream()
					.filter(propertyName -> bookmark.getPropertyValue(propertyName) == null)
					.map(propertyName -> getPropertyDescriptorFromProblemProperty(propertyName))
					.collect(Collectors.toList()));
		}
		return propertyDescriptors.toArray(new IPropertyDescriptor[0]);
	}

	private IPropertyDescriptor getPropertyDescriptorFromBookmarkProperty(String propertyName,
			Optional<BookmarkProblem> problem) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, propertyName);
		String updatedValue = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName))
				.orElse(null);
		if (updatedValue != null) {
			propertyDescriptor.setLabelProvider(new PropertyLabelProvider(false, true));
		}
		return propertyDescriptor;
	}

	private IPropertyDescriptor getPropertyDescriptorFromProblemProperty(String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, propertyName + " (New value)");
		propertyDescriptor.setLabelProvider(new PropertyLabelProvider(true, true));
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
		Optional<BookmarkProblem> problem = getBookmarkProblem(BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		String updatedValue = problem.map(bookmarkProblem -> bookmarkProblem.getProperties().get(propertyName))
				.orElse(null);
		if (updatedValue != null) {
			return new PropertyNeedsUpdatePropertySource(propertyName, propertyValue, updatedValue);
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

	private static class PropertyLabelProvider extends LabelProvider implements IColorProvider {
		private final boolean isGrayed;
		private final boolean useWarningIcon;

		public PropertyLabelProvider(boolean isGrayed, boolean useWarningIcon) {
			this.isGrayed = isGrayed;
			this.useWarningIcon = useWarningIcon;
		}

		@Override
		public Image getImage(Object element) {
			if (useWarningIcon) {
				ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
				return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
			}
			return null;
		}

		@Override
		public Color getForeground(Object element) {
			if (isGrayed) {
				return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);
			}
			return null;
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}

	}

	private static class PropertyNeedsUpdatePropertySource implements IPropertySource {

		private final String propertyName;
		private final String updatedValue;
		private final String propertyValue;

		public PropertyNeedsUpdatePropertySource(String propertyName, String propertyValue, String updatedValue) {
			this.propertyName = propertyName;
			this.updatedValue = updatedValue;
			this.propertyValue = propertyValue;
		}

		@Override
		public Object getEditableValue() {
			return propertyValue;
		}

		@Override
		public IPropertyDescriptor[] getPropertyDescriptors() {
			PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, "Updated value");
			propertyDescriptor.setLabelProvider(new PropertyLabelProvider(true, false));
			return new IPropertyDescriptor[] { propertyDescriptor };
		}

		@Override
		public Object getPropertyValue(Object id) {
			return updatedValue;
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

	}
}
