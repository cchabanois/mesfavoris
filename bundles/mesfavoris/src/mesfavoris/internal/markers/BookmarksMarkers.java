package mesfavoris.internal.markers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.StatusHelper;
import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.model.modification.BookmarksModification;

public class BookmarksMarkers {
	public static final String BOOKMARK_ID = "bookmarkId";
	public static final String MARKER_TYPE = "mesfavoris.bookmark";
	private final BookmarkDatabase bookmarkDatabase;
	private final BookmarksListener bookmarksListener = new BookmarksListener();
	private final IBookmarkMarkerAttributesProvider bookmarkMarkerAttributesProvider;
	private final ProjectOpenedChangeListener projectOpenedChangeListener = new ProjectOpenedChangeListener();

	public BookmarksMarkers(BookmarkDatabase bookmarkDatabase,
			IBookmarkMarkerAttributesProvider bookmarkMarkerAttributesProvider) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkMarkerAttributesProvider = bookmarkMarkerAttributesProvider;
	}

	public void init() {
		bookmarkDatabase.addListener(bookmarksListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(projectOpenedChangeListener);
	}

	public void close() {
		bookmarkDatabase.removeListener(bookmarksListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectOpenedChangeListener);
	}

	private void handleBookmarkModifiedEvent(BookmarksModification event) {
		if (event instanceof BookmarkDeletedModification) {
			bookmarkRemoved(((BookmarkDeletedModification) event).getBookmarkId());
		} else if (event instanceof BookmarksAddedModification) {
			BookmarksAddedModification bookmarksAddedModification = (BookmarksAddedModification) event;
			bookmarksAddedModification.getBookmarks().forEach(b -> bookmarkAdded(b));
		} else if (event instanceof BookmarkPropertiesModification) {
			bookmarkModified(null, null, null, null);
		}
	}

	private void bookmarkModified(Bookmark bookmark, String propertyName, Object oldValue, Object newValue) {
		// TODO Auto-generated method stub

	}

	private void bookmarkAdded(Bookmark bookmarkAdded) {
		BookmarkMarkerDescriptor descriptor = bookmarkMarkerAttributesProvider.getMarkerDescriptor(bookmarkAdded);
		if (descriptor == null) {
			return;
		}
		Map attributes = descriptor.getAttributes();
		attributes.put(BOOKMARK_ID, bookmarkAdded.getId().toString());
		try {
			createMarker(descriptor.getResource(), attributes);
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not create marker for bookmark", e);
		}
	}

	private void bookmarkRemoved(BookmarkId bookmarkId) {
		IMarker marker = findMarker(bookmarkId);
		if (marker == null) {
			return;
		}
		try {
			deleteMarker(marker);
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not delete marker", e);
		}
	}

	private void deleteMarker(final IMarker marker) throws CoreException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				marker.delete();
			}
		};
		run(getMarkerRule(marker.getResource()), wr);
	}

	public IMarker findMarker(BookmarkId bookmarkId) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		try {
			IMarker[] markers = workspaceRoot.findMarkers(BookmarksMarkers.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				String attributeBookmarkId = (String) marker.getAttribute(BookmarksMarkers.BOOKMARK_ID);
				if (bookmarkId.toString().equals(attributeBookmarkId)) {
					return marker;
				}
			}
			return null;
		} catch (CoreException e) {
			return null;
		}
	}

	public void refreshMarker(BookmarkId bookmarkId) {
		bookmarkRemoved(bookmarkId);
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		if (bookmark != null) {
			bookmarkAdded(bookmark);
		}
	}

	private void diff(List<Bookmark> oldValue, List<Bookmark> newValue, Set<Bookmark> added, Set<Bookmark> removed) {
		added.addAll(newValue);
		added.removeAll(oldValue);
		removed.addAll(oldValue);
		removed.removeAll(newValue);
	}

	private IMarker createMarker(final IResource resource, final Map<String, ? extends Object> attributes)
			throws CoreException {
		final IMarker[] marker = new IMarker[1];
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				marker[0] = resource.createMarker(MARKER_TYPE);
				marker[0].setAttributes(attributes);
			}
		};
		run(getMarkerRule(resource), wr);
		return marker[0];
	}

	private void run(ISchedulingRule rule, IWorkspaceRunnable wr) throws CoreException {
		ResourcesPlugin.getWorkspace().run(wr, rule, 0, null);

	}

	private ISchedulingRule getMarkerRule(IResource resource) {
		ISchedulingRule rule = null;
		if (resource != null) {
			IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
			rule = ruleFactory.markerRule(resource);
		}
		return rule;
	}

	private void projectOpened(IProject project) {
		try {
			IMarker[] markers = project.findMarkers(BookmarksMarkers.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
			List<IMarker> invalidMarkers = getInvalidMarkers(Lists.newArrayList(markers));
			for (IMarker marker : invalidMarkers) {
				deleteMarker(marker);
			}
			// TODO : update attributes for markers ?
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not update markers", e);
		}
	}

	private List<IMarker> getInvalidMarkers(final List<IMarker> markers) throws BookmarksException {
		BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();

		List<IMarker> invalidMarkers = Lists.newArrayList();
		for (IMarker marker : markers) {
			try {
				String id = (String) marker.getAttribute(BookmarksMarkers.BOOKMARK_ID);
				if (id != null && bookmarksTree.getBookmark(new BookmarkId(id)) == null) {
					invalidMarkers.add(marker);
				}
			} catch (CoreException e) {
				throw new BookmarksException(e.getStatus());
			}
		}
		return invalidMarkers;
	}

	private class BookmarksListener implements IBookmarksListener {

		@Override
		public void bookmarksModified(List<BookmarksModification> events) {
			events.forEach(event -> handleBookmarkModifiedEvent(event));
		}

	}

	private class ProjectOpenedChangeListener implements IResourceChangeListener {
		public void resourceChanged(final IResourceChangeEvent event) {
			try {
				if (event.getDelta() == null) {
					return;
				}
				event.getDelta().accept(new IResourceDeltaVisitor() {
					public boolean visit(final IResourceDelta delta) throws CoreException {
						IResource resource = delta.getResource();
						if ((resource.getType() & IResource.PROJECT) != 0) {
							if (resource.getProject().isOpen() && delta.getKind() == IResourceDelta.CHANGED
									&& ((delta.getFlags() & IResourceDelta.OPEN) != 0)) {

								final IProject project = (IProject) resource;
								new Job("Updating project markers") {

									@Override
									protected IStatus run(IProgressMonitor monitor) {
										projectOpened(project);
										return Status.OK_STATUS;
									}
								}.schedule();
							}
							return false;
						}
						return true;
					}
				});
			} catch (CoreException e) {
				StatusHelper.logWarn("Error while listening to project opened events", e);
			}
		}
	}

}
