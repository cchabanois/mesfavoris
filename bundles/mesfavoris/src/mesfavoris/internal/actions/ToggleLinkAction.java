package mesfavoris.internal.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;

import mesfavoris.BookmarksPlugin;
import mesfavoris.internal.operations.GetLinkedBookmarksOperation;
import mesfavoris.internal.views.BookmarksTreeViewer;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;

public class ToggleLinkAction extends Action {
	private static final String SYNCED_GIF = "synced.gif";
	private static final String SYNC_BROKEN_GIF = "sync_broken.gif";
	private final BookmarkDatabase bookmarkDatabase;
	private boolean linking = true;
	private String fIconName;
	private IWorkbenchPart lastSelectionProviderPart;
	private final BookmarksTreeViewer bookmarksTreeViewer;
	private final IWorkbenchPartSite workbenchPartSite;

	public ToggleLinkAction(BookmarkDatabase bookmarkDatabase, IWorkbenchPartSite workbenchPartSite,BookmarksTreeViewer bookmarksTreeViewer) {
		super("Link with Selection", SWT.TOGGLE);
		this.bookmarkDatabase = bookmarkDatabase;
		this.workbenchPartSite = workbenchPartSite;
		this.bookmarksTreeViewer = bookmarksTreeViewer;
		setToolTipText("Link with Selection");
		setChecked(linking);
		setActionDefinitionId(IWorkbenchCommandConstants.NAVIGATE_TOGGLE_LINK_WITH_EDITOR);
		updateLinkImage(false);
	}

	public void init() {
		workbenchPartSite.getWorkbenchWindow().getPartService().addPartListener(partListener);
	}
	
	public void dispose() {
		workbenchPartSite.getWorkbenchWindow().getPartService().removePartListener(partListener);
	}
	
	@Override
	public void run() {
		setLinkingEnabled(!linking);
		updateLinkImage(false);
	}

	private void updateLinkImage(boolean isBroken) {
		String iconName = isBroken ? SYNC_BROKEN_GIF : SYNCED_GIF;
		if (!iconName.equals(fIconName)) {
			ImageDescriptor id = BookmarksPlugin.getImageDescriptor("icons/dlcl16/" + iconName);
			if (id != null)
				this.setDisabledImageDescriptor(id);

			ImageDescriptor descriptor = BookmarksPlugin.getImageDescriptor("icons/elcl16/" + iconName);
			this.setHoverImageDescriptor(descriptor);
			this.setImageDescriptor(descriptor);

			setToolTipText(isBroken ? "Link with Selection (showing last valid input)" : "Link with Selection");
			fIconName = iconName;
		}
	}

	private void setLinkingEnabled(boolean enabled) {
		linking = enabled;

		if (linking && lastSelectionProviderPart != null) {
			selectBookmarkFromLinkedPart(lastSelectionProviderPart);
		}
	}

	/**
	 * Select the bookmark corresponding to the selection in given
	 * {@link IWorkbenchPart}
	 * 
	 * @param part
	 */
	private void selectBookmarkFromLinkedPart(IWorkbenchPart part) {
		ISelectionProvider provider = part.getSite().getSelectionProvider();
		if (provider == null) {
			updateLinkImage(true);
			return;
		}

		ISelection selection = provider.getSelection();
		if (selection == null) {
			updateLinkImage(true);
			return;
		}
		GetLinkedBookmarksOperation getLinkedBookmarksOperation = new GetLinkedBookmarksOperation(bookmarkDatabase);
		List<Bookmark> bookmarks = getLinkedBookmarksOperation.getLinkedBookmarks(part, selection);
		if (bookmarks.isEmpty()) {
			updateLinkImage(true);
			return;
		}
		updateLinkImage(false);
		bookmarksTreeViewer.setSelection(new StructuredSelection(bookmarks.get(0)), true);
	}	
	
	/**
	 * Start to listen for selection changes.
	 */
	public void startListeningForSelectionChanges() {
		workbenchPartSite.getPage().addPostSelectionListener(selectionListener);
	}

	/**
	 * Stop to listen for selection changes.
	 */
	public void stopListeningForSelectionChanges() {
		workbenchPartSite.getPage().removePostSelectionListener(selectionListener);
	}

	private ISelectionListener selectionListener = new ISelectionListener() {

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part.equals(this))
				return;

			lastSelectionProviderPart = part;

			if (linking) {
				selectBookmarkFromLinkedPart(part);
			}
		}
	};	
	
	private IPartListener2 partListener = new IPartListener2() {
		public void partVisible(IWorkbenchPartReference ref) {
			if (ref.getId().equals(workbenchPartSite.getId())) {
				IWorkbenchPart activePart = ref.getPage().getActivePart();
				if (activePart != null)
					selectionListener.selectionChanged(activePart, ref.getPage().getSelection());
				startListeningForSelectionChanges();
			}
		}

		public void partHidden(IWorkbenchPartReference ref) {
			if (ref.getId().equals(workbenchPartSite.getId()))
				stopListeningForSelectionChanges();
		}

		public void partInputChanged(IWorkbenchPartReference ref) {
			if (!ref.getId().equals(workbenchPartSite.getId())) {
				selectBookmarkFromLinkedPart(ref.getPart(false));
			}
		}

		public void partActivated(IWorkbenchPartReference ref) {
		}

		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}

		public void partClosed(IWorkbenchPartReference ref) {
		}

		public void partDeactivated(IWorkbenchPartReference ref) {
		}

		public void partOpened(IWorkbenchPartReference ref) {
		}
	};
	
}