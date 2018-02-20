package mesfavoris.internal.snippets;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

import mesfavoris.BookmarksException;
import mesfavoris.internal.BookmarksPlugin;
import mesfavoris.internal.views.BookmarksView;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.ui.details.IBookmarkDetailPart;

/**
 * Create component to display bookmark snippet
 * 
 * @author cchabanois
 *
 */
public class SnippetBookmarkDetailPart implements IBookmarkDetailPart {
	private final BookmarkDatabase bookmarkDatabase;
	private BookmarksView bookmarksView;
	private TextViewer textViewer;
	private Bookmark bookmark;

	public SnippetBookmarkDetailPart() {
		this.bookmarkDatabase = BookmarksPlugin.getDefault().getBookmarkDatabase();
	}

	@Override
	public String getTitle() {
		return "Snippet";
	}

	@Override
	public void initialize(BookmarksView bookmarksView) {
		this.bookmarksView = bookmarksView;
	}

	@Override
	public void createControl(Composite parent) {
		textViewer = new TextViewer(parent, SWT.V_SCROLL | SWT.WRAP | bookmarksView.getFormToolkit().getBorderStyle());
		textViewer.setDocument(new Document());
		textViewer.getTextWidget().setFont(getFont());
		textViewer.addTextListener(getTextListener());
	}

	private Font getFont() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		Font font = fontRegistry.get("org.eclipse.ui.workbench.texteditor.blockSelectionModeFont");
		return font;
	}

	private ITextListener getTextListener() {
		return event -> {
			if (bookmark == null || !textViewer.isEditable()) {
				return;
			}
			final String newSnippet = textViewer.getDocument().get();
			try {
				bookmarkDatabase.modify(bookmarksTreeModifier -> {
					bookmarksTreeModifier.setPropertyValue(bookmark.getId(),
							SnippetBookmarkProperties.PROP_SNIPPET_CONTENT, newSnippet);
				});
			} catch (BookmarksException e) {
				// never happen
			}
		};
	}

	@Override
	public Control getControl() {
		return textViewer.getControl();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return textViewer;
	}

	@Override
	public void setBookmark(Bookmark bookmark) {
		this.bookmark = bookmark;
		if (bookmark == null) {
			textViewer.getDocument().set("");
			textViewer.setEditable(false);
			return;
		}
		String snippet = bookmark.getPropertyValue(SnippetBookmarkProperties.PROP_SNIPPET_CONTENT);
		if (snippet == null) {
			snippet = "";
		}
		textViewer.getDocument().set(snippet);
		textViewer.setEditable(bookmarkDatabase.getBookmarksModificationValidator()
				.validateModification(bookmarkDatabase.getBookmarksTree(), bookmark.getId()).isOK());
	}

	@Override
	public boolean canHandle(Bookmark bookmark) {
		String snippetContent = bookmark.getPropertyValue(SnippetBookmarkProperties.PROP_SNIPPET_CONTENT);
		return snippetContent != null;
	}

	@Override
	public void dispose() {

	}

}
