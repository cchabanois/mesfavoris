package mesfavoris.bookmarktype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import mesfavoris.model.Bookmark;

public abstract class AbstractBookmarkMarkerPropertiesProvider implements IBookmarkMarkerAttributesProvider {

	protected Optional<String> getMessage(Bookmark bookmark) {
		String comment = bookmark.getPropertyValue(Bookmark.PROPERTY_COMMENT);
		if (comment == null) {
			return Optional.empty();
		}
		try (BufferedReader br = new BufferedReader(new StringReader(comment))) {
			return Optional.ofNullable(br.readLine());
		} catch (IOException e) {
			return Optional.empty();
		}
	}
	
}
