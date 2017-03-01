package mesfavoris.bookmarktype;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Provider;

public class NonUpdatablePropertiesProvider implements Provider<Set<String>> {
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
