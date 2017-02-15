package mesfavoris.internal.problems.ui;

import java.util.Optional;
import java.util.Set;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

import mesfavoris.BookmarksException;
import mesfavoris.internal.problems.extension.BookmarkProblemHandlers;
import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.problems.IBookmarkProblemHandler;
import mesfavoris.problems.IBookmarkProblems;

public class BookmarkProblemsTooltip extends ToolTip {
	private static final String IMAGE_WARNING_KEY = "imageWarning";
	private static final String IMAGE_ERROR_KEY = "imageError";
	private final FormToolkit formToolkit;
	private final IBookmarkProblems bookmarkProblems;
	private final BookmarkProblemHandlers bookmarkProblemHandlers;
	private FormText bookmarkProblemsFormText;
	private BookmarkId bookmarkId;

	public BookmarkProblemsTooltip(FormToolkit formToolkit, Control control, int style,
			IBookmarkProblems bookmarkProblems, BookmarkProblemHandlers bookmarkProblemHandlers) {
		super(control, style, false);
		this.formToolkit = formToolkit;
		this.bookmarkProblems = bookmarkProblems;
		this.bookmarkProblemHandlers = bookmarkProblemHandlers;
	}

	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		Composite composite = formToolkit.createComposite(parent);
		GridLayout gridLayout = new GridLayout();
		composite.setLayout(gridLayout);
		bookmarkProblemsFormText = formToolkit.createFormText(composite, true);
		bookmarkProblemsFormText.setWhitespaceNormalized(false);
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		bookmarkProblemsFormText.setImage(IMAGE_ERROR_KEY, sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
		bookmarkProblemsFormText.setImage(IMAGE_WARNING_KEY, sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK));
		GridData gridData = new GridData(GridData.FILL_BOTH);
		bookmarkProblemsFormText.setLayoutData(gridData);

		Set<BookmarkProblem> problems = bookmarkProblems.getBookmarkProblems(bookmarkId);

		StringBuilder sb = new StringBuilder();
		sb.append("<form>");
		sb.append("<p>List of bookmark problems :</p>");
		for (BookmarkProblem problem : problems) {
			IBookmarkProblemHandler handler = bookmarkProblemHandlers
					.getBookmarkProblemHandler(problem.getProblemType());
			if (handler != null) {
				String value = problem.getSeverity() == Severity.ERROR ? IMAGE_ERROR_KEY : IMAGE_WARNING_KEY;
				String message = handler.getErrorMessage(problem);
				sb.append(String.format("<li style=\"image\" value=\"%s\">", value));
				sb.append("<span nowrap=\"true\">");
				sb.append(message);
				String actionMessage = handler.getActionMessage(problem);
				if (actionMessage != null) {
					sb.append(" - ");
				}
				sb.append("</span>");
				if (actionMessage != null) {
					sb.append(String.format("<a href=\"%s\" nowrap=\"true\">%s</a>", problem.getProblemType(),
							actionMessage));
				}
				sb.append("</li>");
			}
		}
		sb.append("</form>");
		bookmarkProblemsFormText.setText(sb.toString(), true, false);
		bookmarkProblemsFormText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				handleAction((String) e.getHref());
			}
		});
		return composite;
	}

	private void handleAction(String problemType) {
		Optional<BookmarkProblem> bookmarkProblem = bookmarkProblems.getBookmarkProblem(bookmarkId, problemType);
		if (!bookmarkProblem.isPresent()) {
			return;
		}
		IBookmarkProblemHandler handler = bookmarkProblemHandlers
				.getBookmarkProblemHandler(bookmarkProblem.get().getProblemType());
		if (handler == null) {
			return;
		}
		try {
			handler.handleAction(bookmarkProblem.get());
		} catch (BookmarksException e) {
			ErrorDialog.openError(null, "Error", "Could not solve bookmark problem", e.getStatus());
		}
	}

	public void setBookmark(BookmarkId bookmarkId) {
		this.bookmarkId = bookmarkId;
	}

}
