package mesfavoris.git;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.ui.internal.commit.CommitEditorInput;
import org.eclipse.egit.ui.internal.commit.RepositoryCommit;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.MesFavoris;
import mesfavoris.placeholders.IPathPlaceholderResolver;

public class CommitEditorBookmarkPropertiesProvider extends CommitBookmarkPropertiesProvider {
	private final IPathPlaceholderResolver pathPlaceholderResolver;

	public CommitEditorBookmarkPropertiesProvider() {
		this(MesFavoris.getPathPlaceholderResolver());
	}

	public CommitEditorBookmarkPropertiesProvider(IPathPlaceholderResolver pathPlaceholderResolver) {
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		if (!(part instanceof IEditorPart)) {
			return;
		}
		IEditorPart editorPart = (IEditorPart) part;
		if (!(editorPart.getEditorInput() instanceof CommitEditorInput)) {
			return;
		}
		CommitEditorInput commitEditorInput = (CommitEditorInput) editorPart.getEditorInput();
		RepositoryCommit repositoryCommit = commitEditorInput.getCommit();
		super.addBookmarkProperties(bookmarkProperties, editorPart, new StructuredSelection(repositoryCommit), monitor);
	}
}
