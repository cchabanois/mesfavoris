package mesfavoris.gdrive.filebookmark;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.google.api.services.drive.model.File;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.gdrive.Activator;
import mesfavoris.gdrive.connection.GDriveConnectionManager;
import mesfavoris.gdrive.operations.GetFileIdFromUrlOperation;
import mesfavoris.gdrive.operations.GetFileMetadataOperation;
import mesfavoris.model.Bookmark;
import mesfavoris.remote.IRemoteBookmarksStore.State;
import mesfavoris.url.UrlBookmarkProperties;

public class GDriveFileBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {
	private final GetFileIdFromUrlOperation getFileIdFromUrlOperation;
	private final GDriveConnectionManager gDriveConnectionManager;

	public GDriveFileBookmarkPropertiesProvider() {
		this.getFileIdFromUrlOperation = new GetFileIdFromUrlOperation();
		this.gDriveConnectionManager = Activator.getDefault().getGDriveConnectionManager();
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		if (!(selected instanceof URL)) {
			return;
		}
		URL url = (URL) selected;

		Optional<String> fileId = getFileIdFromUrlOperation.getFileId(url.toString());
		if (!fileId.isPresent()) {
			return;
		}
		putIfAbsent(bookmarkProperties, UrlBookmarkProperties.PROP_URL, url.toString());
		putIfAbsent(bookmarkProperties, GDriveBookmarkProperties.PROP_FILE_ID, fileId.get());
		
		if (gDriveConnectionManager.getState() != State.connected) {
			return;
		}
		GetFileMetadataOperation getFileMetadataOperation = new GetFileMetadataOperation(
				gDriveConnectionManager.getDrive());
		File file;
		try {
			file = getFileMetadataOperation.getFileMetadata(fileId.get());
		} catch (IOException e) {
			return;
		}
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, file.getTitle());
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_COMMENT, file.getDescription());
		
		if (file.getIconLink() != null) {
			getIconAsBase64(file.getIconLink())
					.ifPresent(icon -> putIfAbsent(bookmarkProperties, UrlBookmarkProperties.PROP_ICON, icon));
		}
	}

	private Optional<String> getIconAsBase64(String iconUrl) {
		Response resultImageResponse;
		try {
			resultImageResponse = Jsoup.connect(iconUrl).ignoreContentType(true).execute();
		} catch (IOException e) {
			return Optional.empty();
		}
		byte[] bytes = resultImageResponse.bodyAsBytes();
		Image image = null;
		try {
			image = new Image(Display.getCurrent(), new ByteArrayInputStream(bytes));
			return Optional.of(Base64.getEncoder().encodeToString(bytes));
		} catch (SWTException e) {
			return Optional.empty();
		} finally {
			if (image != null) {
				image.dispose();
			}
		}
	}

}
