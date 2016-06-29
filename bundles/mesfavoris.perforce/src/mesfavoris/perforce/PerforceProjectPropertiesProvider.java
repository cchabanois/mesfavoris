package mesfavoris.perforce;

import static mesfavoris.perforce.PerforceProjectProperties.PROP_PATH;
import static mesfavoris.perforce.PerforceProjectProperties.PROP_PORT;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4ConnectionManager;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;

public class PerforceProjectPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection) {
		IProject project = getProject(bookmarkProperties);
		if (project == null) {
			return;
		}
		IP4Connection connection = P4ConnectionManager.getManager().getConnection(project);
		if (connection == null) {
			return;
		}
		IP4Resource resource = P4ConnectionManager.getManager().getResource(project);
		if (!(resource instanceof IP4Folder)) {
			return;
		}
		IP4Folder projectFolder = (IP4Folder) resource;
		String depotPath = projectFolder.getFirstWhereRemotePath();
		if (depotPath == null) {
			return;
		}
		bookmarkProperties.put(PROP_PORT, connection.getParameters().getPort());
		bookmarkProperties.put(PROP_PATH, depotPath);

	}

	private IProject getProject(Map<String, String> bookmarkProperties) {
		String projectName = bookmarkProperties.get("projectName");
		if (projectName == null) {
			return null;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists()) {
			return null;
		}
		return project;
	}

}
