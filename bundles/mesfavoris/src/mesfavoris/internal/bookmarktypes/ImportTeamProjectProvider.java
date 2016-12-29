package mesfavoris.internal.bookmarktypes;

import java.util.List;
import java.util.Optional;

import mesfavoris.bookmarktype.IImportTeamProject;
import mesfavoris.model.Bookmark;

public class ImportTeamProjectProvider {
	private final PluginBookmarkTypes pluginBookmarkTypes;
	private List<IImportTeamProject> importers;

	public ImportTeamProjectProvider(PluginBookmarkTypes pluginBookmarkTypes) {
		this.pluginBookmarkTypes = pluginBookmarkTypes;
	}

	private synchronized List<IImportTeamProject> getProjectImporters() {
		if (importers != null) {
			return importers;
		}
		this.importers = pluginBookmarkTypes.getImportTeamProjects();
		return importers;
	}

	public Optional<IImportTeamProject> getHandler(Bookmark bookmark) {
		return getProjectImporters().stream().filter(projectImporter -> projectImporter.canHandle(bookmark))
				.findFirst();
	}

}
