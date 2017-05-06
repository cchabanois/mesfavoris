package mesfavoris.internal.bookmarktypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import mesfavoris.bookmarktype.IBookmarkLocation;
import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public class BookmarkLocationProviderTest {

	@Test
	public void testReturnBestLocation() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId());
		IBookmarkLocationProvider bookmarkLocationProvider1 = mock(IBookmarkLocationProvider.class);
		IBookmarkLocationProvider bookmarkLocationProvider2 = mock(IBookmarkLocationProvider.class);
		IBookmarkLocationProvider bookmarkLocationProvider3 = mock(IBookmarkLocationProvider.class);
		IBookmarkLocation bookmarkLocation1 = mock(IBookmarkLocation.class);
		IBookmarkLocation bookmarkLocation2 = mock(IBookmarkLocation.class);
		IBookmarkLocation bookmarkLocation3 = null;
		when(bookmarkLocation1.getScore()).thenReturn(0.5f);
		when(bookmarkLocation2.getScore()).thenReturn(0.75f);
		when(bookmarkLocationProvider1.getBookmarkLocation(eq(bookmark), any(IProgressMonitor.class)))
				.thenReturn(bookmarkLocation1);
		when(bookmarkLocationProvider2.getBookmarkLocation(eq(bookmark), any(IProgressMonitor.class)))
				.thenReturn(bookmarkLocation2);
		when(bookmarkLocationProvider3.getBookmarkLocation(eq(bookmark), any(IProgressMonitor.class)))
				.thenReturn(bookmarkLocation3);
		BookmarkLocationProvider bookmarkLocationProvider = new BookmarkLocationProvider(
				Arrays.asList(bookmarkLocationProvider1, bookmarkLocationProvider2, bookmarkLocationProvider3));

		// When
		IBookmarkLocation bookmarkLocation = bookmarkLocationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

		// Then
		assertThat(bookmarkLocation).isEqualTo(bookmarkLocation2);
	}

	@Test
	public void testReturnFirstLocationWithMaxScore() {
		// Given
		Bookmark bookmark = new Bookmark(new BookmarkId());
		IBookmarkLocationProvider bookmarkLocationProvider1 = mock(IBookmarkLocationProvider.class);
		IBookmarkLocationProvider bookmarkLocationProvider2 = mock(IBookmarkLocationProvider.class);
		IBookmarkLocation bookmarkLocation1 = mock(IBookmarkLocation.class);
		IBookmarkLocation bookmarkLocation2 = mock(IBookmarkLocation.class);
		when(bookmarkLocation1.getScore()).thenReturn(1.0f);
		when(bookmarkLocation2.getScore()).thenReturn(1.0f);
		when(bookmarkLocationProvider1.getBookmarkLocation(eq(bookmark), any(IProgressMonitor.class)))
				.thenReturn(bookmarkLocation1);
		when(bookmarkLocationProvider2.getBookmarkLocation(eq(bookmark), any(IProgressMonitor.class)))
				.thenReturn(bookmarkLocation2);
		BookmarkLocationProvider bookmarkLocationProvider = new BookmarkLocationProvider(
				Arrays.asList(bookmarkLocationProvider1, bookmarkLocationProvider2));

		// When
		IBookmarkLocation bookmarkLocation = bookmarkLocationProvider.getBookmarkLocation(bookmark,
				new NullProgressMonitor());

		// Then
		assertThat(bookmarkLocation).isEqualTo(bookmarkLocation1);
		verify(bookmarkLocationProvider2, never()).getBookmarkLocation(eq(bookmark), any(IProgressMonitor.class));
	}

}
