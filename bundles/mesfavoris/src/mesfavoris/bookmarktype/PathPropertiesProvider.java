package mesfavoris.bookmarktype;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import mesfavoris.bookmarktype.BookmarkPropertyDescriptor.BookmarkPropertyType;

public class PathPropertiesProvider implements Supplier<Set<String>> {
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
