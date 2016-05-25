package mesfavoris.texteditor.placeholders;

public interface IPathPlaceholders extends Iterable<PathPlaceholder> {
	
	PathPlaceholder get(String name);
	
}
