package mesfavoris.git.rules;

import static mesfavoris.git.GitTestHelper.tryDeleteRepository;
import static org.eclipse.egit.core.GitCorePreferences.core_defaultRepositoryDir;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.egit.core.Activator;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.jgit.lib.Repository;
import org.junit.rules.TemporaryFolder;

import mesfavoris.git.GitTestHelper;

/**
 * Rule to set a default repository dir that will be deleted when the test
 * method finishes
 *
 */
public class TemporaryDefaultRepositoryDirRule extends TemporaryFolder {
	private String previousDefaultRepositoryDir;

	@Override
	protected void before() throws Throwable {
		super.before();
		IEclipsePreferences p = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		previousDefaultRepositoryDir = p.get(core_defaultRepositoryDir, null);
		p.put(core_defaultRepositoryDir, getRoot().getAbsolutePath());

	}

	@Override
	protected void after() {
		try {
			IPath path = new Path(getRoot().getCanonicalPath());
			tryDeleteProjectsUnder(path, false);
			tryDeleteRepositoriesUnder(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IEclipsePreferences p = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
			if (previousDefaultRepositoryDir != null) {
				p.put(core_defaultRepositoryDir, previousDefaultRepositoryDir);
			} else {
				p.remove(core_defaultRepositoryDir);
			}
			super.after();
		}
	}

	private void tryDeleteRepositoriesUnder(IPath rootPath) {
		RepositoryCache repositoryCache = RepositoryCache.INSTANCE;
		Repository[] repositories = repositoryCache.getAllRepositories();
		for (Repository repository : repositories) {
			if (!repository.isBare()) {
				IPath repoPath = new Path(repository.getWorkTree().getAbsolutePath());
				if (rootPath.isPrefixOf(repoPath)) {
					tryDeleteRepository(repository);
				}
			}
		}
	}

	private void tryDeleteProjectsUnder(IPath rootPath, boolean deleteContent) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects(IWorkspaceRoot.INCLUDE_HIDDEN);
		for (IProject project : projects) {
			if (rootPath.isPrefixOf(project.getLocation())) {
				try {
					project.delete(deleteContent, true, new NullProgressMonitor());
				} catch (CoreException e) {

				}
			}
		}

	}

}
