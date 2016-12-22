package mesfavoris.texteditor.internal;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROPERTY_NAME;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_CONTENT;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_PROJECT_NAME;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.BookmarksPlugin;
import mesfavoris.bookmarktype.AbstractBookmarkPropertiesProvider;
import mesfavoris.placeholders.PathPlaceholderResolver;
import mesfavoris.texteditor.TextEditorUtils;

public class TextEditorBookmarkPropertiesProvider extends AbstractBookmarkPropertiesProvider {
	private final PathPlaceholderResolver pathPlaceholderResolver;

	public TextEditorBookmarkPropertiesProvider() {
		this(new PathPlaceholderResolver(BookmarksPlugin.getPathPlaceholdersStore()));
	}

	public TextEditorBookmarkPropertiesProvider(PathPlaceholderResolver pathPlaceholders) {
		this.pathPlaceholderResolver = pathPlaceholders;
	}

	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part, ISelection selection,
			IProgressMonitor monitor) {
		ITextEditor textEditor = getTextEditor(part);
		if (textEditor == null) {
			return;
		}
		if (textEditor != part) {
			selection = getSelection(textEditor);
		}
		if (!(selection instanceof ITextSelection)) {
			return;
		}
		ITextSelection textSelection = (ITextSelection) selection;
		addBookmarkProperties(bookmarkProperties, textEditor, textSelection);
	}

	private void addBookmarkProperties(Map<String, String> properties, ITextEditor textEditor,
			ITextSelection textSelection) {
		int lineNumber = textSelection.getStartLine();
		addLineNumber(properties, lineNumber);
		addLineContent(properties, textEditor, lineNumber);
		addWorkspacePath(properties, textEditor);
		putIfAbsent(properties, PROPERTY_NAME, () -> {
			if (lineNumber > 0) {
				return textEditor.getEditorInput().getName() + ":" + (lineNumber + 1);
			} else {
				return textEditor.getEditorInput().getName();
			}
		});
		IPath filePath = getFilePath(textEditor);
		if (filePath != null) {
			addFilePath(properties, filePath);
		}
	}

	private void addLineContent(Map<String, String> properties, ITextEditor textEditor, int lineNumber) {
		putIfAbsent(properties, PROP_LINE_CONTENT, () -> {
			String content = TextEditorUtils.getLineContent(textEditor, lineNumber);
			return content == null ? null : content.trim();
		});
	}

	private void addLineNumber(Map<String, String> properties, int lineNumber) {
		putIfAbsent(properties, PROP_LINE_NUMBER, Integer.toString(lineNumber));
	}

	private void addWorkspacePath(Map<String, String> properties, ITextEditor textEditor) {
		IEditorInput editorInput = textEditor.getEditorInput();
		IFile file = ResourceUtil.getFile(editorInput);
		if (file == null) {
			return;
		}
		putIfAbsent(properties, PROP_WORKSPACE_PATH, file.getFullPath().toPortableString());
		putIfAbsent(properties, PROP_PROJECT_NAME, file.getProject().getName());
	}

	private void addFilePath(Map<String, String> properties, IPath filePath) {
		putIfAbsent(properties, PROP_FILE_PATH, () -> pathPlaceholderResolver.collapse(filePath));
	}

	private IPath getFilePath(ITextEditor textEditor) {
		IEditorInput editorInput = textEditor.getEditorInput();
		IFile file = ResourceUtil.getFile(editorInput);
		File localFile = null;
		if (file != null) {
			localFile = file.getLocation().toFile();
		} else if (editorInput instanceof FileStoreEditorInput) {
			FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput) editorInput;
			URI uri = fileStoreEditorInput.getURI();
			IFileStore location = EFS.getLocalFileSystem().getStore(uri);
			try {
				localFile = location.toLocalFile(EFS.NONE, null);
			} catch (CoreException e) {
				// ignore
			}
		}
		if (localFile == null) {
			return null;
		} else {
			return Path.fromOSString(localFile.toString());
		}
	}

}
