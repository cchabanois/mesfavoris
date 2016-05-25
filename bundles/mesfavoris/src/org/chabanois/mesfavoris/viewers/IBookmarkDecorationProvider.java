package org.chabanois.mesfavoris.viewers;

import java.util.function.Function;

import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.jface.resource.ImageDescriptor;

public interface IBookmarkDecorationProvider extends Function<Bookmark, ImageDescriptor[]> {

}
