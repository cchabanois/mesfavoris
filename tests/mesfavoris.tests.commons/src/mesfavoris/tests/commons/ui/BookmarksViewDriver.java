package mesfavoris.tests.commons.ui;

import java.util.stream.Collectors;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;

import mesfavoris.BookmarksException;
import mesfavoris.MesFavoris;
import mesfavoris.model.BookmarkId;
import mesfavoris.service.IBookmarksService;

public class BookmarksViewDriver {
	private final SWTWorkbenchBot bot;
	
	public BookmarksViewDriver(SWTWorkbenchBot bot) {
		this.bot = bot;
	}
	
	public void showView() {
		SWTBotViewHelper.showView(MesFavoris.VIEW_ID);
	}
	
	public SWTBotView view() {
		return bot.viewById(MesFavoris.VIEW_ID);
	}
	
	public SWTBotTree tree() throws WidgetNotFoundException {
		return view().bot().tree();
	}
	
	public void deleteAllBookmarksExceptDefaultBookmarkFolder() throws BookmarksException {
		IBookmarksService bookmarksService = MesFavoris.getBookmarksService();
		BookmarkId rootFolderId = bookmarksService.getBookmarksTree().getRootFolder().getId();
		bookmarksService.deleteBookmarks(bookmarksService.getBookmarksTree().getChildren(rootFolderId).stream()
				.map(bookmark -> bookmark.getId())
				.filter(bookmarkId -> !bookmarkId.equals(MesFavoris.DEFAULT_BOOKMARKFOLDER_ID))
				.collect(Collectors.toList()), true);
	}
}
