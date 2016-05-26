package mesfavoris.internal.bookmarktypes;

import java.util.List;
import java.util.Optional;

import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.model.Bookmark;

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
