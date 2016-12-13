package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_BRANCH;
import static mesfavoris.git.GitBookmarkProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_URL;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;

public class GitProjectPropertiesProvider extends AbstractBookmarkPropertiesProvider {

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		IProject project = getProject(bookmarkProperties);
		if (project == null) {
			return;
		}
		RepositoryMapping mapping = RepositoryMapping.getMapping((IResource) project);
		if (mapping == null) {
			return;
		}
		String branch = getBranch(mapping);
		if (branch == null) {
			return;
		}
		String url = getUrl(mapping, branch);
		String projectPath = getProjectPath(mapping, project);
		putIfAbsent(bookmarkProperties, PROP_BRANCH, branch);
		putIfAbsent(bookmarkProperties, PROP_URL, url);
		putIfAbsent(bookmarkProperties, PROP_PROJECT_PATH, projectPath);
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

	private String getBranch(RepositoryMapping mapping) {
		try {
			return mapping.getRepository().getBranch();
		} catch (IOException e) {
			return null;
		}
	}

	private String getUrl(RepositoryMapping mapping, String branch) {
		StoredConfig config = mapping.getRepository().getConfig();
		String remote = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, branch,
				ConfigConstants.CONFIG_KEY_REMOTE);
		String url = config.getString(ConfigConstants.CONFIG_REMOTE_SECTION, remote, ConfigConstants.CONFIG_KEY_URL);
		return url;
	}

	private String getProjectPath(RepositoryMapping mapping, IProject project) {
		String projectPath = mapping.getRepoRelativePath(project);
		if (projectPath.equals(""))
			projectPath = ".";
		return projectPath;
	}

}
