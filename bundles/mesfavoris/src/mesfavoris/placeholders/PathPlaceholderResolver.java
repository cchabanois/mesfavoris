package mesfavoris.placeholders;

import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathPlaceholderResolver {
	private final IPathPlaceholders mappings;

	public PathPlaceholderResolver(IPathPlaceholders mappings) {
		this.mappings = mappings;
	}

	/**
	 * Expand
	 * 
	 * @param pathWithPlaceholder
	 * @return
	 */
	public IPath expand(String pathWithPlaceholder) {
		String variableName = getPlaceholderName(pathWithPlaceholder);
		if (variableName == null) {
			try {
				return new Path(pathWithPlaceholder);
			} catch (InvalidPathException e) {
				return null;
			}
		}
		String other = pathWithPlaceholder.substring(variableName.length() + 4);
		PathPlaceholder pathPlaceholder = mappings.get(variableName);
		if (pathPlaceholder == null || pathPlaceholder.getPath() == null) {
			return null;
		}
		return pathPlaceholder.getPath().append(other);
	}

	public static String getPlaceholderName(String pathWithPlaceholder) {
		int index1 = pathWithPlaceholder.indexOf("${");
		int index2 = pathWithPlaceholder.indexOf("}/");
		if (index1 != 0 || index2 == -1) {
			return null;
		}
		String variableName = pathWithPlaceholder.substring(2, index2);
		return variableName;
	}

	public String collapse(IPath path, String... placeholderNames) {
		path = path.makeAbsolute();
		String bestCollapsedPath = null;
		int bestScore = Integer.MAX_VALUE;

		Iterable<PathPlaceholder> pathPlaceholders;
		if (placeholderNames.length == 0) {
			pathPlaceholders = mappings;
		} else {
			pathPlaceholders = Arrays.stream(placeholderNames).map(name -> mappings.get(name)).filter(Objects::nonNull)
					.collect(Collectors.toList());
		}

		for (PathPlaceholder pathPlaceholder : pathPlaceholders) {
			String variable = pathPlaceholder.getName();
			IPath variablePath = pathPlaceholder.getPath();
			if (variablePath != null && variablePath.isPrefixOf(path)) {
				IPath relativePath = path.makeRelativeTo(variablePath);
				int score = relativePath.toString().length();
				if (score < bestScore) {
					bestCollapsedPath = "${" + variable + "}/" + relativePath;
					bestScore = score;
				}
			}
		}
		if (bestCollapsedPath == null) {
			bestCollapsedPath = path.toString();
		}
		return bestCollapsedPath;
	}

}
