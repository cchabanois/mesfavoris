package mesfavoris.placeholders;

import org.eclipse.core.runtime.IPath;

public interface IPathPlaceholderResolver {

	/**
	 * Expand
	 * 
	 * @param pathWithPlaceholder
	 * @return
	 */
	IPath expand(String pathWithPlaceholder);

	String collapse(IPath path, String... placeholderNames);

}