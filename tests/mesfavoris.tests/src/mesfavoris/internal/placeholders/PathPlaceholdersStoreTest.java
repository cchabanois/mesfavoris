package mesfavoris.internal.placeholders;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

import mesfavoris.placeholders.PathPlaceholder;
import mesfavoris.topics.BookmarksEvents;

public class PathPlaceholdersStoreTest {
	private PathPlaceholdersStore pathPlaceholdersStore;
	private File file;
	private IEventBroker eventBroker = mock(IEventBroker.class);

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() throws IOException {
		file = temporaryFolder.newFile();
		pathPlaceholdersStore = new PathPlaceholdersStore(eventBroker, file);
	}

	@Test
	public void testSaveAndLoad() throws Exception {
		// Given
		pathPlaceholdersStore.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));
		pathPlaceholdersStore.add(new PathPlaceholder("BLT", new Path("/home/cchabanois/blt")));

		// When
		pathPlaceholdersStore.close();
		pathPlaceholdersStore = new PathPlaceholdersStore(eventBroker, file);
		pathPlaceholdersStore.init();

		// Then
		assertEquals(new Path("/home/cchabanois/blt"), pathPlaceholdersStore.get("BLT").getPath());
	}

	@Test
	public void addPathPlaceholder() {
		// Given

		// When
		pathPlaceholdersStore.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));

		// Then
		verify(eventBroker).post(BookmarksEvents.TOPIC_PATH_PLACEHOLDERS_CHANGED,
				ImmutableMap.of("name", "HOME", "path", "/home/cchabanois"));
	}

	@Test
	public void removePathPlaceholder() {
		// Given
		pathPlaceholdersStore.add(new PathPlaceholder("HOME", new Path("/home/cchabanois")));

		// When
		pathPlaceholdersStore.remove("HOME");

		// Then
		verify(eventBroker).post(BookmarksEvents.TOPIC_PATH_PLACEHOLDERS_CHANGED,
				ImmutableMap.of("name", "HOME"));
	}

}
