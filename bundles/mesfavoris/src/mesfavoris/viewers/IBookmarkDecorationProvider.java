package mesfavoris.viewers;

import java.util.function.Function;

import org.eclipse.jface.resource.ImageDescriptor;

import mesfavoris.model.Bookmark;

public interface IBookmarkDecorationProvider extends Function<Bookmark, ImageDescriptor[]> {

}
