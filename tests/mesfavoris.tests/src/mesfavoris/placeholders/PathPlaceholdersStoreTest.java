package mesfavoris.placeholders;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import mesfavoris.internal.placeholders.PathPlaceholdersStore;

public class PathPlaceholdersStoreTest {
	private PathPlaceholdersStore pathPlaceholdersStore;
	private File file;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws IOException {
		file = temporaryFolder.newFile();
		pathPlaceholdersStore = new PathPlaceholdersStore(file);
	}

	@Test
	public void testSaveAndLoad() throws Exception {
		// Given
		pathPlaceholdersStore.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		pathPlaceholdersStore.add(new PathPlaceholder("BLT", new Path("/home/cchabanois/blt")));

		// When
		pathPlaceholdersStore.close();
		pathPlaceholdersStore = new PathPlaceholdersStore(file);
		pathPlaceholdersStore.init();

		// Then
		assertEquals(new Path("/home/cchabanois/blt"), pathPlaceholdersStore.get("BLT").getPath());
	}

}
