package mesfavoris.internal.snippets;

import static mesfavoris.internal.snippets.SnippetBookmarkProperties.PROP_SNIPPET_CONTENT;
import static mesfavoris.model.Bookmark.PROPERTY_NAME;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

import com.google.common.base.Objects;

import mesfavoris.BookmarksException;
import mesfavoris.internal.views.details.AbstractBookmarkDetailPart;
import mesfavoris.model.Bookmark;

/**
 * Create component to display bookmark snippet
 * 
 * @author cchabanois
 *
 */
public class SnippetBookmarkDetailPart extends AbstractBookmarkDetailPart {
	private TextViewer textViewer;

	@Override
	public String getTitle() {
		return "Snippet";
	}

	@Override
	public void createControl(Composite parent, FormToolkit formToolkit) {
		super.createControl(parent, formToolkit);
		textViewer = new TextViewer(parent, SWT.V_SCROLL | SWT.WRAP | formToolkit.getBorderStyle());
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
			final Snippet newSnippet = new Snippet(textViewer.getDocument().get());
			try {
				bookmarkDatabase.modify(bookmarksTreeModifier -> {
					String previousName = bookmark.getPropertyValue(PROPERTY_NAME);
					String previousSnippetContent = bookmark.getPropertyValue(PROP_SNIPPET_CONTENT);
					bookmarksTreeModifier.setPropertyValue(bookmark.getId(), PROP_SNIPPET_CONTENT,
							newSnippet.getContent());					
					if (previousName != null && previousSnippetContent != null && previousName.equals(SnippetBookmarkPropertiesProvider.getName(new Snippet(previousSnippetContent)))) {
						// only change name if it has not been modified
						bookmarksTreeModifier.setPropertyValue(bookmark.getId(), PROPERTY_NAME,
								SnippetBookmarkPropertiesProvider.getName(newSnippet));
					}
				}, (bookmarksTree) -> {
					bookmark = bookmarksTree.getBookmark(bookmark.getId());
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
		super.setBookmark(bookmark);
		if (bookmark == null) {
			textViewer.getDocument().set("");
			textViewer.setEditable(false);
			return;
		}
		String snippet = bookmark.getPropertyValue(PROP_SNIPPET_CONTENT);
		if (snippet == null) {
			snippet = "";
		}
		textViewer.getDocument().set(snippet);
		textViewer.setEditable(bookmarkDatabase.getBookmarksModificationValidator()
				.validateModification(bookmarkDatabase.getBookmarksTree(), bookmark.getId()).isOK());
	}

	@Override
	public boolean canHandle(Bookmark bookmark) {
		if (bookmark == null) {
			return false;
		}
		String snippetContent = bookmark.getPropertyValue(PROP_SNIPPET_CONTENT);
		return snippetContent != null;
	}

	@Override
	protected void bookmarkModified(Bookmark oldBookmark, Bookmark newBookmark) {
		if (newBookmark == null || !Objects.equal(oldBookmark.getPropertyValue(PROP_SNIPPET_CONTENT),
				newBookmark.getPropertyValue(PROP_SNIPPET_CONTENT))) {
			Display.getDefault().asyncExec(() -> setBookmark(newBookmark));
		}
	}

}
