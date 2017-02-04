package mesfavoris.internal.problems.ui;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

import mesfavoris.internal.problems.extension.BookmarkProblemHandlers;
import mesfavoris.model.BookmarkId;
import mesfavoris.problems.BookmarkProblem;
import mesfavoris.problems.BookmarkProblem.Severity;
import mesfavoris.problems.IBookmarkProblemHandler;
import mesfavoris.problems.IBookmarkProblems;

public class BookmarkProblemsControl extends Composite {
	private static final String IMAGE_WARNING_KEY = "imageWarning";
	private static final String IMAGE_ERROR_KEY = "imageError";
	private final FormToolkit toolkit;
	private final IBookmarkProblems bookmarkProblems;
	private final BookmarkProblemHandlers bookmarkProblemHandlers;
	private final FormText bookmarkProblemsFormText;
	private final GridData gridData;

	public BookmarkProblemsControl(Composite parent, IBookmarkProblems bookmarkProblems,
			BookmarkProblemHandlers bookmarkProblemHandlers) {
		super(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		toolkit = new FormToolkit(parent.getDisplay());
		this.bookmarkProblems = bookmarkProblems;
		this.bookmarkProblemHandlers = bookmarkProblemHandlers;
		bookmarkProblemsFormText = toolkit.createFormText(this, true);
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		bookmarkProblemsFormText.setImage(IMAGE_ERROR_KEY, sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
		bookmarkProblemsFormText.setImage(IMAGE_WARNING_KEY, sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK));
		gridData = new GridData();
		gridData.minimumWidth = 1;
		gridData.minimumHeight = 1;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.exclude = true;
		bookmarkProblemsFormText.setLayoutData(gridData);
		bookmarkProblemsFormText.setVisible(false);
	}
	
	public void setBookmark(BookmarkId bookmarkId) {
		Set<BookmarkProblem> problems = bookmarkProblems.getBookmarkProblems(bookmarkId);
		if (problems.isEmpty()) {
			bookmarkProblemsFormText.setText("", false, false);
			bookmarkProblemsFormText.setVisible(false);
			gridData.exclude = true;
		} else {
			bookmarkProblemsFormText.setVisible(true);
			StringBuilder sb = new StringBuilder();
			sb.append("<form>");
			for (BookmarkProblem problem : problems) {
				IBookmarkProblemHandler handler = bookmarkProblemHandlers
						.getBookmarkProblemHandler(problem.getProblemType());
				if (handler != null) {
					String value = problem.getSeverity() == Severity.ERROR ? IMAGE_ERROR_KEY : IMAGE_WARNING_KEY;
					String message = handler.getErrorMessage(problem);
					sb.append(String.format("<li style=\"image\" value=\"%s\">%s</li>", value, message));
				}
			}
			sb.append("</form>");
			bookmarkProblemsFormText.setText(sb.toString(), true, false);
			gridData.exclude = false;
		}
		layout();
		redraw();
	}

}
