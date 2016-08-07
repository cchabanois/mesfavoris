package mesfavoris.internal.markers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.StatusHelper;
import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.internal.jobs.BackgroundBookmarksModificationsHandler;
import mesfavoris.internal.jobs.BackgroundBookmarksModificationsHandler.IBookmarksModificationsHandler;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarkPropertiesModification;
import mesfavoris.model.modification.BookmarksAddedModification;
import mesfavoris.model.modification.BookmarksModification;

/**
 * Manage bookmarks markers
 * 
 * @author cchabanois
 *
 */
public class BookmarksMarkers {
	public static final String BOOKMARK_ID = "bookmarkId";
	public static final String MARKER_TYPE = "mesfavoris.bookmark";
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkMarkerAttributesProvider bookmarkMarkerAttributesProvider;
	private final ProjectOpenedChangeListener projectOpenedChangeListener = new ProjectOpenedChangeListener();
	private final BackgroundBookmarksModificationsHandler backgroundBookmarksModificationsHandler;

	public BookmarksMarkers(BookmarkDatabase bookmarkDatabase,
			IBookmarkMarkerAttributesProvider bookmarkMarkerAttributesProvider) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkMarkerAttributesProvider = bookmarkMarkerAttributesProvider;
		this.backgroundBookmarksModificationsHandler = new BackgroundBookmarksModificationsHandler("Updating markers",
				bookmarkDatabase, new BookmarksModificationsHandler(), 1000);
	}

	public void init() {
		backgroundBookmarksModificationsHandler.init();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(projectOpenedChangeListener);
	}

	public void close() {
		backgroundBookmarksModificationsHandler.close();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectOpenedChangeListener);
	}

	private void handleBookmarkModifiedEvent(BookmarksModification event, IProgressMonitor monitor) {
		if (event instanceof BookmarkDeletedModification) {
			BookmarkDeletedModification bookmarkDeletedModification = (BookmarkDeletedModification) event;
			List<Bookmark> deletedBookmarks = Lists.newArrayList(bookmarkDeletedModification.getDeletedBookmarks());
			SubMonitor subMonitor = SubMonitor.convert(monitor, deletedBookmarks.size());
			deletedBookmarks.forEach(b -> bookmarkRemoved(b.getId(), subMonitor.newChild(1)));
		} else if (event instanceof BookmarksAddedModification) {
			BookmarksAddedModification bookmarksAddedModification = (BookmarksAddedModification) event;
			SubMonitor subMonitor = SubMonitor.convert(monitor, bookmarksAddedModification.getBookmarks().size());
			bookmarksAddedModification.getBookmarks().forEach(b -> bookmarkAdded(b, subMonitor.newChild(1)));
		} else if (event instanceof BookmarkPropertiesModification) {
			BookmarkPropertiesModification bookmarkPropertiesModification = (BookmarkPropertiesModification) event;
			bookmarkModified(bookmarkPropertiesModification.getTargetTree()
					.getBookmark(bookmarkPropertiesModification.getBookmarkId()), monitor);
		}
	}

	private void bookmarkModified(Bookmark bookmarkModified, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		IMarker marker = findMarker(bookmarkModified.getId());
		BookmarkMarkerDescriptor descriptor = bookmarkMarkerAttributesProvider.getMarkerDescriptor(bookmarkModified,
				subMonitor.newChild(90));
		try {
			if (descriptor == null) {
				if (marker != null) {
					deleteMarker(marker, subMonitor.newChild(5));
				}
				return;
			}
			Map attributes = descriptor.getAttributes();
			attributes.put(BOOKMARK_ID, bookmarkModified.getId().toString());
			if (marker == null) {
				createMarker(descriptor.getResource(), attributes, subMonitor.newChild(5));
			} else {
				updateMarker(marker, attributes, subMonitor.newChild(5));
			}
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not update marker for bookmark", e);
		}
	}

	private void bookmarkAdded(Bookmark bookmarkAdded, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		BookmarkMarkerDescriptor descriptor = bookmarkMarkerAttributesProvider.getMarkerDescriptor(bookmarkAdded,
				subMonitor.newChild(90));
		if (descriptor == null) {
			return;
		}
		Map attributes = descriptor.getAttributes();
		attributes.put(BOOKMARK_ID, bookmarkAdded.getId().toString());
		try {
			createMarker(descriptor.getResource(), attributes, subMonitor.newChild(10));
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not create marker for bookmark", e);
		}
	}

	private void bookmarkRemoved(BookmarkId bookmarkId, IProgressMonitor monitor) {
		IMarker marker = findMarker(bookmarkId);
		if (marker == null) {
			return;
		}
		try {
			deleteMarker(marker, monitor);
		} catch (CoreException e) {
			StatusHelper.logWarn("Could not delete marker", e);
		}
	}

	private void deleteMarker(final IMarker marker, IProgressMonitor monitor) throws CoreException {
		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				marker.delete();
			}
		};
		run(getMarkerRule(marker.getResource()), wr, monitor);
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

	public void refreshMarker(BookmarkId bookmarkId, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		bookmarkRemoved(bookmarkId, subMonitor.newChild(50));
		Bookmark bookmark = bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId);
		if (bookmark != null) {
			bookmarkAdded(bookmark, subMonitor.newChild(50));
		}
	}

	private IMarker createMarker(final IResource resource, final Map<String, ? extends Object> attributes,
			IProgressMonitor monitor) throws CoreException {
		final IMarker[] marker = new IMarker[1];
		run(getMarkerRule(resource), subMonitor -> {
			marker[0] = resource.createMarker(MARKER_TYPE);
			marker[0].setAttributes(attributes);
		}, monitor);
		return marker[0];
	}

	private IMarker updateMarker(final IMarker marker, final Map<String, ? extends Object> attributes,
			IProgressMonitor monitor) throws CoreException {
		run(getMarkerRule(marker.getResource()), subMonitor -> marker.setAttributes(attributes), monitor);
		return marker;
	}

	private void run(ISchedulingRule rule, IWorkspaceRunnable wr, IProgressMonitor monitor) throws CoreException {
		ResourcesPlugin.getWorkspace().run(wr, rule, 0, monitor);

	}

	private ISchedulingRule getMarkerRule(IResource resource) {
		ISchedulingRule rule = null;
		if (resource != null) {
			IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
			rule = ruleFactory.markerRule(resource);
		}
		return rule;
	}

	private void projectOpened(IProject project, IProgressMonitor monitor) {
		try {
			IMarker[] markers = project.findMarkers(BookmarksMarkers.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
			List<IMarker> invalidMarkers = getInvalidMarkers(Lists.newArrayList(markers));
			SubMonitor subMonitor = SubMonitor.convert(monitor, invalidMarkers.size());
			for (IMarker marker : invalidMarkers) {
				deleteMarker(marker, subMonitor.newChild(1));
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

	private class BookmarksModificationsHandler implements IBookmarksModificationsHandler {

		@Override
		public void handle(List<BookmarksModification> modifications, IProgressMonitor monitor) {
			SubMonitor subMonitor = SubMonitor.convert(monitor, "Updating markers", modifications.size());
			for (BookmarksModification modification : modifications) {
				handleBookmarkModifiedEvent(modification, subMonitor.newChild(1));
			}
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
										projectOpened(project, monitor);
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
