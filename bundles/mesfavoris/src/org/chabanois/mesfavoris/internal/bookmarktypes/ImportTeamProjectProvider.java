package org.chabanois.mesfavoris.internal.bookmarktypes;

import java.util.List;
import java.util.Optional;

import org.chabanois.mesfavoris.bookmarktype.IImportTeamProject;
import org.chabanois.mesfavoris.model.Bookmark;

public class ImportTeamProjectProvider {
	private final BookmarkTypeConfigElementLoader loader = new BookmarkTypeConfigElementLoader();
	private List<IImportTeamProject> importers;

	private synchronized List<IImportTeamProject> getProjectImporters() {
		if (importers != null) {
			return importers;
		}
		this.importers = loader.load("importTeamProject");
		return importers;
	}

	public Optional<IImportTeamProject> getHandler(Bookmark bookmark) {
		return getProjectImporters().stream().filter(projectImporter -> projectImporter.canHandle(bookmark))
				.findFirst();
	}

}
