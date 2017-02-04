package mesfavoris.internal.views.properties;

import java.util.Optional;
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

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.IBookmarkProblems;

public class BookmarkPropertySource implements IPropertySource {
	private final Bookmark bookmark;
	private final IBookmarkProblems bookmarkProblems;
	private final BookmarkPropertyWithProblemLabelProvider bookmarkPropertyWithProblemLabelProvider = new BookmarkPropertyWithProblemLabelProvider();

	public BookmarkPropertySource(Bookmark bookmark) {
		this.bookmark = bookmark;
		this.bookmarkProblems = BookmarksPlugin.getDefault().getBookmarkProblems();
	}

	@Override
	public Object getEditableValue() {
		return bookmark;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return bookmark.getProperties().keySet().stream().map(propertyName -> getPropertyDescriptor(propertyName))
				.collect(Collectors.toList()).toArray(new IPropertyDescriptor[0]);
	}

	private IPropertyDescriptor getPropertyDescriptor(String propertyName) {
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, propertyName);
		Optional<BookmarkProblem> problem = bookmarkProblems.getBookmarkProblem(bookmark.getId(),
				BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		String updatedValue = problem.map(bookmarkProblem->bookmarkProblem.getProperties().get(propertyName)).orElse(null);
		if (updatedValue != null) {
			propertyDescriptor.setLabelProvider(bookmarkPropertyWithProblemLabelProvider);
		}
		return propertyDescriptor;
	}

	@Override
	public Object getPropertyValue(Object id) {
		String propertyName = (String)id;
		String propertyValue = bookmark.getPropertyValue(propertyName);
		Optional<BookmarkProblem> problem = bookmarkProblems.getBookmarkProblem(bookmark.getId(),
				BookmarkProblem.TYPE_PROPERTIES_NEED_UPDATE);
		String updatedValue = problem.map(bookmarkProblem->bookmarkProblem.getProperties().get(propertyName)).orElse(null);
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

	private static class BookmarkPropertyWithProblemLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
			return sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
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
			propertyDescriptor.setLabelProvider(new GrayedLabelProvided());
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

	private static class GrayedLabelProvided extends LabelProvider implements IColorProvider {

		@Override
		public Color getForeground(Object element) {
			return PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}
		
	}
	
}
