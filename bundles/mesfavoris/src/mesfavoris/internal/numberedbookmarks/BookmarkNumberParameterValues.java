package mesfavoris.internal.numberedbookmarks;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

/**
 * Provides parameter values for command mesfavoris.command.addNumberedFavori
 * 
 * @author cchabanois
 *
 */
public class BookmarkNumberParameterValues implements IParameterValues {

	@Override
	public Map getParameterValues() {
		Map<String, String> result = new HashMap<>();
		for (BookmarkNumber bookmarkNumber : BookmarkNumber.values()) {
			result.put(bookmarkNumber.toString(), bookmarkNumber.toString());
		}
		return result;
	}

}
