package mesfavoris.bookmarktype;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NonUpdatablePropertiesProvider implements Supplier<Set<String>> {
	private final IBookmarkPropertyDescriptors bookmarkPropertyDescriptors;

	public NonUpdatablePropertiesProvider(IBookmarkPropertyDescriptors bookmarkPropertyDescriptors) {
		this.bookmarkPropertyDescriptors = bookmarkPropertyDescriptors;
	}
	
	@Override
	public Set<String> get() {
		Set<String> nonUpdatableProperties = bookmarkPropertyDescriptors.getPropertyDescriptors().stream()
				.filter(descriptor -> !descriptor.isUpdatable()).map(descriptor -> descriptor.getName())
				.collect(Collectors.toSet());
		return nonUpdatableProperties;
	}
	
}
