package mesfavoris.path;

import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getFirstElement;
import static mesfavoris.bookmarktype.BookmarkPropertiesProviderUtil.getTextEditor;
import static mesfavoris.path.PathBookmarkProperties.PROP_WORKSPACE_PATH;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;

/**
 * Some common utilities for working with {@link IBookmarkPropertiesProvider}
 * 
 * @author cchabanois
 *
 */
public class PathBookmarkPropertiesProviderHelper {

	/**
	 * Get the {@link IResource} corresponding to given parameters
	 * 
	 * @param bookmarkProperties
	 * @param part
	 * @param selection
	 * @return the resource or null if none
	 */
	public static IResource getWorkspaceResource(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection) {
		IResource resource = getWorkspaceResource(bookmarkProperties);
		if (resource != null) {
			return resource;
		}
		resource = getWorkspaceResource(selection);
		if (resource != null) {
			return resource;
		}
		resource = getWorkspaceResource(part);
		return resource;
	}

	private static IResource getWorkspaceResource(Map<String, String> bookmarkProperties) {
		String workspacePath = bookmarkProperties.get(PROP_WORKSPACE_PATH);
		if (workspacePath == null) {
			return null;
		}
		Path path = new Path(workspacePath);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = workspaceRoot.findMember(path);
		return resource;
	}

	private static IResource getWorkspaceResource(ISelection selection) {
		Object selected = getFirstElement(selection);
		return Adapters.adapt(selected, IResource.class);
	}

	private static IResource getWorkspaceResource(IWorkbenchPart part) {
		ITextEditor textEditor = getTextEditor(part);
		if (textEditor == null) {
			return null;
		}
		IEditorInput editorInput = textEditor.getEditorInput();
		IFile file = ResourceUtil.getFile(editorInput);
		return file;
	}

}
