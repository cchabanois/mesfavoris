package mesfavoris.gdrive.dialogs;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.api.services.drive.model.File;

import mesfavoris.BookmarksPlugin;
import mesfavoris.viewers.StylerProvider;

/**
 * Viewer for a set of {@link com.google.api.services.drive.model.File}s
 * 
 * @author cchabanois
 *
 */
public class FileTableViewer extends TableViewer {

	public FileTableViewer(Composite parent, int style) {
		super(parent);

		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new DelegatingStyledCellLabelProvider(new FileLabelProvider()));
	}

	public void setFiles(List<File> files) {
		setInput(files);
	}

	private class FileLabelProvider extends LabelProvider implements IStyledLabelProvider {
		private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
		private final ImageDescriptor imageDescriptor = BookmarksPlugin.getImageDescriptor("icons/bookmarks-16.png");
		private final StylerProvider stylerProvider = new StylerProvider();

		@Override
		public void dispose() {
			super.dispose();
			resourceManager.dispose();
		}

		@Override
		public String getText(Object element) {
			return getStyledText(element).toString();
		}

		@Override
		public Image getImage(Object element) {
			return (Image) resourceManager.get(imageDescriptor);
		}

		@Override
		public StyledString getStyledText(Object element) {
			File file = (File) element;
			StyledString styledString = new StyledString(file.getTitle());
			if (Boolean.FALSE.equals(file.getEditable())) {
				styledString.append(" [readonly]", stylerProvider.getStyler(null,
						Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW), null));
			}
			if (file.getSharingUser() != null) {
				styledString.append(String.format(" [Shared by %s]", file.getSharingUser().getDisplayName()), stylerProvider
						.getStyler(null, Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW), null));
			}
			return styledString;
		}

	}

}
