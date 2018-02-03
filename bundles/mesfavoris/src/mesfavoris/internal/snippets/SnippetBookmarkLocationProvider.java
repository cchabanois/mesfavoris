package mesfavoris.internal.snippets;

import org.eclipse.core.runtime.IProgressMonitor;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;

public class SnippetBookmarkLocationProvider implements IBookmarkLocationProvider {

	@Override
	public IBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		String snippetContent = bookmark.getPropertyValue(SnippetBookmarkProperties.PROP_SNIPPET_CONTENT);
		if (snippetContent == null) {
			return null;
		}
		return new Snippet(snippetContent);
	}

}
