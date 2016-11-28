package mesfavoris.commons.ui.ide;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

public class ShowInSystemExplorer {
	private static final String VARIABLE_RESOURCE = "${selected_resource_loc}";
	private static final String VARIABLE_RESOURCE_URI = "${selected_resource_uri}";
	private static final String VARIABLE_FOLDER = "${selected_resource_parent_loc}";

	public ShowInSystemExplorer() {

	}

	public void showInSystemExplorer(File canonicalPath, IProgressMonitor monitor) throws IOException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		String launchCmd = formShowInSystemExplorerCommand(canonicalPath);

		if ("".equals(launchCmd)) {
			throw new IOException(
					"System Explorer command unavailable.  Please set the System Explorer command in the workspace preferences.");
		}

		File dir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		Process p;
		if (Util.isLinux() || Util.isMac()) {
			p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", launchCmd }, null, dir);
		} else {
			p = Runtime.getRuntime().exec(launchCmd, null, dir);
		}
		int retCode;
		try {
			retCode = p.waitFor();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		subMonitor.worked(100);
		if (retCode != 0 && !Util.isWindows()) {
			throw new IOException("Execution of '" + launchCmd + "' failed with return code: " + retCode, null);
		}
	}

	/**
	 * Prepare command for launching system explorer to show a path
	 *
	 * @param path
	 *            the path to show
	 * @return the command that shows the path
	 */
	private String formShowInSystemExplorerCommand(File path) throws IOException {
		String command = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getString(IDEInternalPreferences.WORKBENCH_SYSTEM_EXPLORER);

		command = Util.replaceAll(command, VARIABLE_RESOURCE, quotePath(path.getCanonicalPath()));
		command = Util.replaceAll(command, VARIABLE_RESOURCE_URI, path.getCanonicalFile().toURI().toString());
		File parent = path.getParentFile();
		if (parent != null) {
			command = Util.replaceAll(command, VARIABLE_FOLDER, quotePath(parent.getCanonicalPath()));
		}
		return command;
	}

	private String quotePath(String path) {
		if (Util.isLinux() || Util.isMac()) {
			// Quote for usage inside "", man sh, topic QUOTING:
			path = path.replaceAll("[\"$`]", "\\\\$0"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Windows: Can't quote, since explorer.exe has a very special command
		// line parsing strategy.
		return path;
	}

}
