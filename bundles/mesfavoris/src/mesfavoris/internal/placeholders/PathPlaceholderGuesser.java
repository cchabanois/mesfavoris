package mesfavoris.internal.placeholders;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import mesfavoris.placeholders.IPathPlaceholderResolver;
import mesfavoris.placeholders.PathPlaceholder;

public class PathPlaceholderGuesser {
	private final IPathPlaceholderResolver pathPlaceholderResolver;
	private final Set<String> pathProperties;
	
	public PathPlaceholderGuesser(IPathPlaceholderResolver pathPlaceholderResolver, Set<String> pathProperties) {
		this.pathPlaceholderResolver = pathPlaceholderResolver;
		this.pathProperties = pathProperties;
	}
	
	public Set<PathPlaceholder> guessUndefinedPlaceholders(Map<String, String> properties,
			Map<String, String> updatedProperties) {
		Set<PathPlaceholder> pathPlaceholders = properties.entrySet().stream()
				.filter(entry -> pathProperties.contains(entry.getKey()))
				.filter(entry -> PathPlaceholderResolver.getPlaceholderName(entry.getValue()) != null)
				.filter(entry -> updatedProperties.get(entry.getKey()) != null)
				.map(entry -> guessUndefinedPlaceholder(entry.getValue(),
						pathPlaceholderResolver.expand(updatedProperties.get(entry.getKey()))))
				.flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
				.collect(Collectors.toSet());
		return pathPlaceholders;
	}	
	
	/**
	 * Guess the placeholder given its collapsed path and its supposed expanded
	 * path.
	 * 
	 * @param collapsedPath
	 * @param expandedPath
	 * @return
	 */
	public Optional<PathPlaceholder> guessUndefinedPlaceholder(String collapsedPath, IPath expandedPath) {
		String variableName = PathPlaceholderResolver.getPlaceholderName(collapsedPath);
		if (variableName == null) {
			return Optional.empty();
		}
		if (pathPlaceholderResolver.expand(collapsedPath) != null) {
			return Optional.empty();
		}
		IPath relativePath = new Path(collapsedPath.substring(variableName.length() + 4));
		if (matchingLastSegments(relativePath, expandedPath) != relativePath.segmentCount()) {
			return Optional.empty();
		}
		IPath path = expandedPath.removeLastSegments(relativePath.segmentCount());
		return Optional.of(new PathPlaceholder(variableName, path));
	}

	public int matchingLastSegments(IPath path, IPath anotherPath) {
		int count = 0;
		for (int i = path.segmentCount()-1, j = anotherPath.segmentCount()-1; i >=0 && j >= 0; i--,j--) {
			if (!path.segment(i).equals(anotherPath.segment(j))) {
				return count;
			}
			count++;
		}
		return count;
	}	
	
}
