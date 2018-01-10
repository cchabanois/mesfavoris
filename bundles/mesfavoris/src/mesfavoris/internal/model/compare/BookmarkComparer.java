package mesfavoris.internal.model.compare;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import mesfavoris.model.Bookmark;

public class BookmarkComparer {

	public BookmarkComparer() {

	}

	public BookmarkDifferences compare(Bookmark bookmarkSource, Bookmark bookmarkTarget) {
		Set<String> addedProperties = new HashSet<>();
		Set<String> modifiedProperties = new HashSet<>();
		Set<String> deletedProperties = new HashSet<>();
		for (Map.Entry<String, String> entry : bookmarkSource.getProperties().entrySet()) {
			String propertyName = entry.getKey();
			String sourceValue = bookmarkSource.getPropertyValue(propertyName);
			String targetValue = bookmarkTarget.getPropertyValue(propertyName);
			if (targetValue == null) {
				deletedProperties.add(propertyName);
			} else if (!targetValue.equals(sourceValue)) {
				modifiedProperties.add(propertyName);
			}
		}

		for (Map.Entry<String, String> entry : bookmarkTarget.getProperties().entrySet()) {
			String propertyName = entry.getKey();
			String sourceValue = bookmarkSource.getPropertyValue(propertyName);
			if (sourceValue == null) {
				addedProperties.add(propertyName);
			}
		}
		return new BookmarkDifferences(addedProperties, modifiedProperties, deletedProperties);
	}

	public final static class BookmarkDifferences {
		private final Set<String> addedProperties;
		private final Set<String> modifiedProperties;
		private final Set<String> deletedProperties;

		public BookmarkDifferences(Set<String> addedProperties, Set<String> modifiedProperties,
				Set<String> deletedProperties) {
			this.addedProperties = ImmutableSet.copyOf(addedProperties);
			this.modifiedProperties = ImmutableSet.copyOf(modifiedProperties);
			this.deletedProperties = ImmutableSet.copyOf(deletedProperties);
		}
		
		public Set<String> getAddedProperties() {
			return addedProperties;
		}
		
		public Set<String> getDeletedProperties() {
			return deletedProperties;
		}
		
		public Set<String> getModifiedProperties() {
			return modifiedProperties;
		}
		
		public boolean isEmpty() {
			return addedProperties.isEmpty() && deletedProperties.isEmpty() && getModifiedProperties().isEmpty();
		}
		
	}

}
