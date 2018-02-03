package mesfavoris.internal.snippets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;

import mesfavoris.model.Bookmark;

public class SnippetBookmarkPropertiesProviderTest {
	private final SnippetBookmarkPropertiesProvider propertiesProvider = new SnippetBookmarkPropertiesProvider();
	private final Map<String, String> bookmarkProperties = new HashMap<>();

	@Test
	public void testUseFirstLineAsName() {
		// Given
		String content = "first line\nsecond line\nthird line";
		ISelection selection = getSnippetSelection(content);

		// When
		propertiesProvider.addBookmarkProperties(bookmarkProperties, null, selection, new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties.get(SnippetBookmarkProperties.PROP_SNIPPET_CONTENT)).isEqualTo(content);
		assertThat(bookmarkProperties.get(Bookmark.PROPERTY_NAME)).isEqualTo("first line");
	}

	@Test
	public void testUseFirstNonEmptyLineAsName() {
		// Given
		String content = "  \nfirst line\nsecond line\nthird line";
		ISelection selection = getSnippetSelection(content);

		// When
		propertiesProvider.addBookmarkProperties(bookmarkProperties, null, selection, new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties.get(SnippetBookmarkProperties.PROP_SNIPPET_CONTENT)).isEqualTo(content);
		assertThat(bookmarkProperties.get(Bookmark.PROPERTY_NAME)).isEqualTo("first line");
	}

	private ISelection getSnippetSelection(String content) {
		return new StructuredSelection(new Snippet(content));
	}

}
