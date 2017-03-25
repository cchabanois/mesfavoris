package mesfavoris.gdrive.mappings;

import static mesfavoris.remote.RemoteBookmarkFolder.PROP_READONLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import mesfavoris.model.BookmarkId;

public class BookmarkMappingsPersisterTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private BookmarkMappingsPersister persister;

	@Before
	public void setUp() throws Exception {
		persister = new BookmarkMappingsPersister(temporaryFolder.newFile());
	}

	@Test
	public void testSave() throws Exception {
		// Given
		BookmarkMapping mapping1 = new BookmarkMapping(new BookmarkId("bookmarkFolder1"), "fileId1",
				ImmutableMap.of(PROP_READONLY, "true", "sharingUser", "Cedric Chabanois"));
		BookmarkMapping mapping2 = new BookmarkMapping(new BookmarkId("bookmarkFolder2"), "fileId2",
				ImmutableMap.of(PROP_READONLY, "false"));

		// When
		persister.save(Sets.newHashSet(mapping1, mapping2), new NullProgressMonitor());

		// Then
		Set<BookmarkMapping> loadedBookmarkMappings = persister.load();
		assertThat(loadedBookmarkMappings).containsExactlyInAnyOrder(mapping1, mapping2);
	}

}
