package mesfavoris.bookmarktype;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Provider;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor.BookmarkPropertyType;

public class PathPropertiesProvider implements Provider<Set<String>> {
	private final IBookmarkPropertyDescriptors bookmarkPropertyDescriptors;

	public PathPropertiesProvider(IBookmarkPropertyDescriptors bookmarkPropertyDescriptors) {
		this.bookmarkPropertyDescriptors = bookmarkPropertyDescriptors;
	}

	@Override
	public Set<String> get() {
		Set<String> pathProperties = bookmarkPropertyDescriptors.getPropertyDescriptors().stream()
				.filter(descriptor -> descriptor.getType() == BookmarkPropertyType.PATH)
				.map(descriptor -> descriptor.getName()).collect(Collectors.toSet());
		return pathProperties;
	}
}
