package mesfavoris.texteditor.resource;

import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import mesfavoris.texteditor.internal.StatusHelper;
import mesfavoris.texteditor.internal.resource.ResourcePathDistanceComputer;
import static BookmarkB

/**
 * Find a resource. Name has to be the same than the given one but path can be
 * different. A resource with same name and the most similar path will be returned.
 * 
 * @author cchabanois
 *
 */
public class FuzzyResourceFinder {

	public Optional<IResource> find(IPath expectedPath, int type) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = workspaceRoot.findMember(expectedPath);
		if (resource != null && resource.getType() == type) {
			return Optional.of(resource);
		}
		FindResourceProxyVisitor findResourceProxyVisitor = new FindResourceProxyVisitor(expectedPath, type);
		try {
			workspaceRoot.accept(findResourceProxyVisitor, IResource.NONE);
			return findResourceProxyVisitor.getBestMatchingResource();
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not find resource", e);
			return Optional.empty();
		}
	}

	private static class FindResourceProxyVisitor implements IResourceProxyVisitor {
		private IResource resourceCandidate;
		private int resourceCandidateDistance = Integer.MAX_VALUE;
		private final IPath expectedPath;
		private final String expectedName;
		private final int type;
		private final ResourcePathDistanceComputer resourcePathDistanceComputer;

		public FindResourceProxyVisitor(IPath expectedPath, int type) {
			this.expectedPath = expectedPath;
			this.expectedName = expectedPath.segment(expectedPath.segmentCount() - 1);
			this.type = type;
			this.resourcePathDistanceComputer = new ResourcePathDistanceComputer();
		}

		@Override
		public boolean visit(IResourceProxy proxy) throws CoreException {
			if (proxy.getName().equals(expectedName) && proxy.getType() == type) {
				IResource resource = proxy.requestResource();
				int distance = resourcePathDistanceComputer.distance(expectedPath, resource.getFullPath());
				if (distance < resourceCandidateDistance) {
					resourceCandidate = resource;
					resourceCandidateDistance = distance;
				}
			}
			return true;
		}

		public Optional<IResource> getBestMatchingResource() {
			if (resourceCandidate == null) {
				return Optional.empty();
			}
			return Optional.of(resourceCandidate);
		}

	}

}
