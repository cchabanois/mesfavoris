package mesfavoris.internal.snippets;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;

import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.model.Bookmark;

public class SnippetBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {
	private static int NAME_LENGTH_LIMIT = 80;

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		Object selected = getFirstElement(selection);
		if (!(selected instanceof Snippet)) {
			return;
		}
		Snippet snippet = (Snippet) selected;
		putIfAbsent(bookmarkProperties, Bookmark.PROPERTY_NAME, getName(snippet));
		putIfAbsent(bookmarkProperties, SnippetBookmarkProperties.PROP_SNIPPET_CONTENT, snippet.getContent());
	}

	public static String getName(Snippet snippet) {
		String firstLine = getFirstNonEmptyLine(snippet).orElse("Empty snippet");
		if (firstLine.length() > NAME_LENGTH_LIMIT) {
			String ellipsis = "...";
			return firstLine.substring(0, NAME_LENGTH_LIMIT - ellipsis.length() - 1) + ellipsis;
		} else {
			return firstLine;
		}

	}

	private static Optional<String> getFirstNonEmptyLine(Snippet snippet) {
		String content = snippet.getContent();
		String[] lines = content.split("\\r?\\n");
		for (String line : lines) {
			if (line.trim().length() > 0) {
				return Optional.of(line);
			}
		}
		return Optional.empty();
	}

}
