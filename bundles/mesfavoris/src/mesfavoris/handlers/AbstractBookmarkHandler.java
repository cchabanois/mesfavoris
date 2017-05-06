package mesfavoris.handlers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;

import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.model.merge.BookmarksTreeIterable;
import mesfavoris.internal.model.merge.BookmarksTreeIterable.Algorithm;
import mesfavoris.internal.views.virtual.BookmarkLink;
import mesfavoris.internal.views.virtual.VirtualBookmarkFolder;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.service.IBookmarksService;

public abstract class AbstractBookmarkHandler extends AbstractHandler{
	protected IEvaluationContext evaluationContext;
	protected final IBookmarksService bookmarksService;
	
	public AbstractBookmarkHandler() {
		this.bookmarksService = BookmarksPlugin.getDefault().getBookmarksService();
	}
	
	protected List<BookmarkId> getAsBookmarkIds(IStructuredSelection selection) {
		List<Bookmark> bookmarks = ((List<Bookmark>)(selection.toList()));
		return bookmarks.stream().map(e -> e.getId()).collect(Collectors.toList());
	}
	
	protected IStructuredSelection getSelection() {
		return getSelection(ISources.ACTIVE_CURRENT_SELECTION_NAME);
	}

	private IStructuredSelection getSelection(String variableName) {
		if (evaluationContext == null) {
			return new StructuredSelection();
		}
		Object selection = evaluationContext
				.getVariable(variableName);
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			return new StructuredSelection();
		}
		return (IStructuredSelection) selection;
	}	
	
	@Override
	public void setEnabled(Object evaluationContext) {
		if (evaluationContext instanceof IEvaluationContext)
			this.evaluationContext = (IEvaluationContext) evaluationContext;
		else
			this.evaluationContext = null;
	}	
	
	protected Set<Bookmark> getSelectedBookmarksRecursively(BookmarksTree bookmarksTree, IStructuredSelection selection, Predicate<Bookmark> filter) {
		Set<Bookmark> bookmarkIds = new LinkedHashSet<>();
		for (Object element : selection.toList()) {
			if (element instanceof VirtualBookmarkFolder) {
				VirtualBookmarkFolder virtualBookmarkFolder = (VirtualBookmarkFolder) element;
				for (BookmarkLink bookmarkLink : virtualBookmarkFolder.getChildren()) {
					Bookmark bookmark = Adapters.adapt(bookmarkLink, Bookmark.class);
					if (bookmark instanceof BookmarkFolder) {
						bookmarkIds.addAll(getBookmarksRecursively(bookmarksTree, bookmark.getId(), filter));
					} else if (bookmark != null && filter.test(bookmark)) {
						bookmarkIds.add(bookmark);
					}
				}
			} else {
				Bookmark bookmark = Adapters.adapt(element, Bookmark.class);
				if (bookmark instanceof BookmarkFolder) {
					bookmarkIds.addAll(getBookmarksRecursively(bookmarksTree, bookmark.getId(), filter));
				} else if (bookmark != null && filter.test(bookmark)) {
					bookmarkIds.add(bookmark);
				}
			}
		}
		return bookmarkIds;
	}
	
	protected List<Bookmark> getBookmarksRecursively(BookmarksTree bookmarksTree, BookmarkId folderId, Predicate<Bookmark> filter) {
		BookmarksTreeIterable bookmarksTreeIterable = new BookmarksTreeIterable(bookmarksTree, folderId,
				Algorithm.PRE_ORDER, filter);
		return StreamSupport.stream(bookmarksTreeIterable.spliterator(), false).collect(Collectors.toList());
	}
	
}
