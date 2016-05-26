package mesfavoris.internal.handlers;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;

import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkId;

public abstract class AbstractBookmarkHandler extends AbstractHandler{
	protected IEvaluationContext evaluationContext;
	
	protected List<BookmarkId> getAsBookmarkIds(IStructuredSelection selection) {
		List<Bookmark> bookmarks = ((List<Bookmark>)(selection.toList()));
		return bookmarks.stream().map(e -> e.getId()).collect(Collectors.toList());
	}
	
	protected IStructuredSelection getSelection() {
		return getSelection(ISources.ACTIVE_CURRENT_SELECTION_NAME);
	}

	private IStructuredSelection getSelection(String variableName) {
		if (evaluationContext == null) {
			return new StructuredSelection();
		}
		Object selection = evaluationContext
				.getVariable(variableName);
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			return new StructuredSelection();
		}
		return (IStructuredSelection) selection;
	}	
	
	@Override
	public void setEnabled(Object evaluationContext) {
		if (evaluationContext instanceof IEvaluationContext)
			this.evaluationContext = (IEvaluationContext) evaluationContext;
		else
			this.evaluationContext = null;
	}	
	
}
