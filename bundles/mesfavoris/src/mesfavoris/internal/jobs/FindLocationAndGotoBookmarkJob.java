package mesfavoris.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import mesfavoris.BookmarksException;
import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.bookmarktype.IGotoBookmark;
import mesfavoris.model.Bookmark;

public class FindLocationAndGotoBookmarkJob extends Job {
	private final IBookmarkLocationProvider bookmarkLocationProvider;
	private final IGotoBookmark gotoBookmark;
	private final Bookmark bookmark;

	public FindLocationAndGotoBookmarkJob(Bookmark bookmark) {
		this(BookmarksPlugin.getBookmarkLocationProvider(), BookmarksPlugin.getGotoBookmark(), bookmark);
	}
	
	public FindLocationAndGotoBookmarkJob(IBookmarkLocationProvider bookmarkLocationProvider,
			IGotoBookmark gotoBookmark, Bookmark bookmark) {
		super("Goto bookmark...");
		this.bookmarkLocationProvider = bookmarkLocationProvider;
		this.gotoBookmark = gotoBookmark;
		this.bookmark = bookmark;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IBookmarkLocation bookmarkLocation = bookmarkLocationProvider.getBookmarkLocation(bookmark, monitor);
		if (bookmarkLocation != null) {
			Display.getDefault().asyncExec(() -> {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				gotoBookmark.gotoBookmark(workbenchWindow, bookmark, bookmarkLocation);
			});
			return Status.OK_STATUS;
		} else {
			return new BookmarksException("Could not find bookmark").getStatus();
		}
	}

}
