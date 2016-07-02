package mesfavoris.commons.ui.wizards.datatransfer;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Bundle;

public class BundleProjectImportOperation implements IRunnableWithProgress {
	private final Bundle bundle;
	private final String projectName;
	private final String bundleProjectEntryPath;

	public BundleProjectImportOperation(Bundle bundle, String projectName, String bundleProjectEntryPath) {
		this.bundle = bundle;
		this.projectName = projectName;
		if (!bundleProjectEntryPath.endsWith("/")) {
			bundleProjectEntryPath += '/';
		}
		this.bundleProjectEntryPath = bundleProjectEntryPath;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		BundleImportStructureProvider bundleImportStructureProvider = new BundleImportStructureProvider(bundle);
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
			public String queryOverwrite(String file) {
				return ALL;
			}
		};
		ImportOperation importOperation = new ImportOperation(project.getFullPath(), bundleProjectEntryPath,
				bundleImportStructureProvider, overwriteQuery);
		importOperation.setCreateContainerStructure(false);
		importOperation.run(monitor);
	}

}
