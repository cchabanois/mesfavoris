package mesfavoris.git;

import static mesfavoris.git.GitBookmarkProperties.PROP_BRANCH;
import static mesfavoris.git.GitBookmarkProperties.PROP_PROJECT_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_RESOURCE_PATH;
import static mesfavoris.git.GitBookmarkProperties.PROP_URL;
import static mesfavoris.path.PathBookmarkPropertiesProviderHelper.getWorkspaceResource;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.info.GitItemState;
import org.eclipse.egit.core.internal.info.GitItemStateFactory;
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
		IResource resource = getWorkspaceResource(bookmarkProperties, part, selection);
		if (resource == null) {
			return;
		}
		IProject project = resource.getProject();
		if (project == null) {
			return;
		}
		GitItemState state = GitItemStateFactory.getInstance().get(resource.getLocation().toFile());
		if (!state.isTracked()) {
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
		String projectPath = mapping.getRepoRelativePath(project);
		String resourcePath = mapping.getRepoRelativePath(resource);
		putIfAbsent(bookmarkProperties, PROP_BRANCH, branch);
		putIfAbsent(bookmarkProperties, PROP_URL, url);
		putIfAbsent(bookmarkProperties, PROP_PROJECT_PATH, projectPath);
		putIfAbsent(bookmarkProperties, PROP_RESOURCE_PATH, resourcePath);
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

}
