package mesfavoris.texteditor.placeholders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import mesfavoris.texteditor.placeholders.PathPlaceholder;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;
import mesfavoris.texteditor.placeholders.PathPlaceholdersMap;

public class PathPlaceholderResolverTest {
	private final PathPlaceholdersMap mappings = new PathPlaceholdersMap();
	private final PathPlaceholderResolver pathPlaceholderResolver = new PathPlaceholderResolver(mappings);

	@Test
	public void testCollapse() {
		// Given
		mappings.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		mappings.add(new PathPlaceholder("BLT", new Path("/home/cchabanois/blt")));

		// When
		String result = pathPlaceholderResolver.collapse(new Path("/home/cchabanois/blt/app/main/core"));

		// Then
		assertEquals("${BLT}/app/main/core", result);
	}

	@Test
	public void testCollapseWithGivenPlaceholder() {
		// Given
		mappings.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		mappings.add(new PathPlaceholder("BLT", new Path("/home/cchabanois/blt")));

		// When
		String result = pathPlaceholderResolver.collapse(new Path("/home/cchabanois/blt/app/main/core"), "HOME");

		// Then
		assertEquals("${HOME}/blt/app/main/core", result);
	}	
	
	@Test
	public void testExpand() {
		// Given
		mappings.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		mappings.add(new PathPlaceholder("BLT", new Path("/home/cchabanois/blt")));

		// When
		IPath result = pathPlaceholderResolver.expand("${BLT}/app/main/core");

		// Then
		assertEquals(new Path("/home/cchabanois/blt/app/main/core"), result);
	}

	@Test
	public void testExpandNoMatchingPlaceholder() {
		// When
		IPath result = pathPlaceholderResolver.expand("${BLT}/app/main/core");

		// Then
		assertNull(result);
	}

	@Test
	public void testExpandAlreadyExpanded() {
		// When
		IPath result = pathPlaceholderResolver.expand("/home/cchabanois/blt/app/main/core");

		// Then
		assertEquals(new Path("/home/cchabanois/blt/app/main/core"), result);
	}

	@Test
	public void testGetPlaceholderName() {
		// Given
		mappings.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		mappings.add(new PathPlaceholder("BLT", new Path("/home/cchabanois/blt")));

		// When
		String result = PathPlaceholderResolver.getPlaceholderName("${BLT}/app/main/core");

		// Then
		assertEquals("BLT", result);
	}

}
