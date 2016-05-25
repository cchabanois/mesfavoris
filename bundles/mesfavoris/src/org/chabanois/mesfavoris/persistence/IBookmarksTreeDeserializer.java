package org.chabanois.mesfavoris.persistence;

import java.io.IOException;
import java.io.Reader;

import org.chabanois.mesfavoris.model.BookmarksTree;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IBookmarksTreeDeserializer {

	/**
	 * Deserialize a {@link BookmarksTree}
	 * 
	 * @param reader
	 * @param monitor
	 * @return
	 * @throws IOException
	 */
	public BookmarksTree deserialize(Reader reader, IProgressMonitor monitor) throws IOException;

}