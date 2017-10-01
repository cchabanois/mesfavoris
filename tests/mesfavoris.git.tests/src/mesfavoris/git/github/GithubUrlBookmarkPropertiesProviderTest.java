package mesfavoris.git.github;

import static mesfavoris.git.GitBookmarkProperties.PROP_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Test;

public class GithubUrlBookmarkPropertiesProviderTest {
	private final GithubUrlBookmarkPropertiesProvider bookmarkPropertiesProvider = new GithubUrlBookmarkPropertiesProvider();
	private final Map<String, String> bookmarkProperties = new HashMap<>();

	@Test
	public void testGithubRepository() throws Exception {
		// Given
		String url = "https://github.com/cchabanois/mesfavoris";

		// When
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, null,
				new StructuredSelection(new URL(url)), new NullProgressMonitor());

		// Then
		assertThat(bookmarkProperties).containsEntry(PROP_URL, "https://github.com/cchabanois/mesfavoris.git");
	}

	@Test
	public void testNonGithubRepository() throws Exception {
		// Given
		String url = "https://github.com/marketplace/circleci";
		
		// When
		bookmarkPropertiesProvider.addBookmarkProperties(bookmarkProperties, null,
				new StructuredSelection(new URL(url)), new NullProgressMonitor());
		
		// Then
		assertThat(bookmarkProperties.isEmpty());
	}

}
