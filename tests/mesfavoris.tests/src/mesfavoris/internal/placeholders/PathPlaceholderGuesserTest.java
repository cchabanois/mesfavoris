package mesfavoris.internal.placeholders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import mesfavoris.model.Bookmark;
import mesfavoris.placeholders.IPathPlaceholderResolver;
import mesfavoris.placeholders.PathPlaceholder;

public class PathPlaceholderGuesserTest {
	private PathPlaceholderGuesser pathPlaceholderGuesser;
	private Set<String> pathProperties = Sets.newHashSet("git.repositoryDir", "folderPath", "filePath");
	private PathPlaceholdersMap pathPlaceholders = new PathPlaceholdersMap();

	@Before
	public void setUp() {
		IPathPlaceholderResolver pathPlaceholderResolver = new PathPlaceholderResolver(pathPlaceholders);
		pathPlaceholderGuesser = new PathPlaceholderGuesser(pathPlaceholderResolver, pathProperties);
	}

	@Test
	public void testGuessFilePathPlaceholder() {
		assertThat(pathPlaceholderGuesser.guessUndefinedPlaceholder("${DOCUMENTS}/open/myFile.odt",
				new Path("/home/cchabanois/documents/open/myFile.odt")).get())
						.isEqualTo(new PathPlaceholder("DOCUMENTS", new Path("/home/cchabanois/documents")));
	}

	@Test
	public void testCannotGuessPathPlaceholder() {
		assertThat(pathPlaceholderGuesser.guessUndefinedPlaceholder("${DOCUMENTS}/open/myFile.odt",
				new Path("/home/cchabanois/documents/open/myFile-renamed.odt"))).isEmpty();
	}

	@Test
	public void testGuessFolderPathPlaceholder() {
		assertThat(pathPlaceholderGuesser.guessUndefinedPlaceholder("${DOCUMENTS}/open/folder",
				new Path("/home/cchabanois/documents/open/folder/")).get())
						.isEqualTo(new PathPlaceholder("DOCUMENTS", new Path("/home/cchabanois/documents")));
	}

	@Test
	public void testGuessUndefinedPlaceholders() {
		// Given
		Map<String, String> properties = ImmutableMap.of(Bookmark.PROPERTY_NAME, "my bookmark", "filePath",
				"${DOCUMENTS}/open/myFile.odt");
		Map<String, String> updatedProperties = ImmutableMap.of(Bookmark.PROPERTY_NAME, "my bookmark", "filePath",
				"/home/cchabanois/documents/open/myFile.odt");
		// When
		Set<PathPlaceholder> pathPlaceholders = pathPlaceholderGuesser.guessUndefinedPlaceholders(properties,
				updatedProperties);

		// Then
		assertThat(pathPlaceholders)
				.containsExactly(new PathPlaceholder("DOCUMENTS", new Path("/home/cchabanois/documents")));
	}

	@Test
	public void testCannotGuessUndefinedPlaceholdersWhenPropertyNotUpdated() {
		// Given
		Map<String, String> properties = ImmutableMap.of(Bookmark.PROPERTY_NAME, "my bookmark", "filePath",
				"${DOCUMENTS}/open/myFile.odt");
		Map<String, String> updatedProperties = ImmutableMap.of(Bookmark.PROPERTY_NAME, "my bookmark");
		// When
		Set<PathPlaceholder> pathPlaceholders = pathPlaceholderGuesser.guessUndefinedPlaceholders(properties,
				updatedProperties);

		// Then
		assertThat(pathPlaceholders).isEmpty();
	}	
	
}
