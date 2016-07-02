package mesfavoris.commons.ui.wizards.datatransfer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.osgi.framework.Bundle;

public class BundleImportStructureProvider implements IImportStructureProvider {
	private final Bundle bundle;

	public BundleImportStructureProvider(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public List<String> getChildren(Object element) {
		String entryPath = (String) element;
		Enumeration<String> enumeration = (Enumeration<String>) bundle.getEntryPaths(entryPath);
		return Collections.list(enumeration);
	}

	@Override
	public InputStream getContents(Object element) {
		try {
			String entryPath = (String) element;
			URL url = bundle.getEntry(entryPath);
			return url.openStream();
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public String getFullPath(Object element) {
		String entryPath = (String) element;
		return entryPath;
	}

	@Override
	public String getLabel(Object element) {
		Path entryPath = new Path((String) element);
		return entryPath.lastSegment();
	}

	@Override
	public boolean isFolder(Object element) {
		String entryPath = (String) element;
		return entryPath.endsWith("/");
	}

}