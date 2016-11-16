package mesfavoris.internal.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.StatusHelper;
import mesfavoris.model.Bookmark;

public class DeleteLinkedBookmarkHandler extends AbstractBookmarkPartOperationHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		BookmarkPartOperationContext operationContext = getOperationContext(event);
		if (operationContext == null) {
			return null;
		}
		List<Bookmark> linkedBookmarks = bookmarksService.getLinkedBookmarks(operationContext.part, operationContext.selection);
		if (linkedBookmarks.isEmpty()) {
			return null;
		}
		try {
			bookmarksService.deleteBookmarks(Lists.newArrayList(linkedBookmarks.get(0).getId()));
		} catch (BookmarksException e) {
			StatusHelper.showError("Could not delete bookmark", e, false);
		}
		
		return null;
	}

	
	
}
