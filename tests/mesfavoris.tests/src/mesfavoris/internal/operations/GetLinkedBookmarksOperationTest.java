package mesfavoris.internal.operations;

import static mesfavoris.tests.commons.waits.Waiter.waitUntil;
import static mesfavoris.testutils.BookmarkBuilder.bookmark;
import static mesfavoris.testutils.BookmarksTreeBuilder.bookmarksTree;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.internal.bookmarktypes.PluginBookmarkMarkerAttributesProvider;
import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.testutils.BookmarksTreeBuilder;

public class GetLinkedBookmarksOperationTest {

	private GetLinkedBookmarksOperation operation;
	private BookmarkDatabase bookmarkDatabase;
	private BookmarksMarkers bookmarksMarkers;
	private BookmarkId rootFolderId;

	@Before
	public void setUp() {
		this.bookmarkDatabase = new BookmarkDatabase("testId", getInitialTree());
		this.rootFolderId = bookmarkDatabase.getBookmarksTree().getRootFolder().getId();
		this.operation = new GetLinkedBookmarksOperation(bookmarkDatabase);
		this.bookmarksMarkers = new BookmarksMarkers(bookmarkDatabase, new PluginBookmarkMarkerAttributesProvider());
		this.bookmarksMarkers.init();
	}

	@After
	public void tearDown() {
		this.bookmarksMarkers.close();
	}

	@Test
	public void testGetLinkedBookmarks() throws Exception {
		// Given
		importProjectFromTemplate("testGetLinkedBookmarks", "commons-cli");
		Bookmark bookmark = bookmark("LICENSE.txt")
				.withProperty(PROP_WORKSPACE_PATH, "/testGetLinkedBookmarks/LICENSE.txt")
				.withProperty(PROP_LINE_NUMBER, "10").build();
		addBookmark(rootFolderId, bookmark);
		waitUntil("Cannot find marker", () -> bookmarksMarkers.findMarker(bookmark.getId(), null));

		// When
		ITextEditor textEditor = openTextEditor(new Path("/testGetLinkedBookmarks/LICENSE.txt"));
		textEditor.selectAndReveal(getOffset(textEditor, 10), 0);
		List<Bookmark> bookmarks = operation.getLinkedBookmarks(textEditor, textEditor.getSelectionProvider().getSelection());

		// Then
		assertEquals(1, bookmarks.size());
		assertEquals(bookmark, bookmarks.get(0));
	}

	private ITextEditor openTextEditor(IPath path) throws PartInitException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IEditorPart editorPart = IDE.openEditor(window.getActivePage(), workspaceRoot.getFile(path));
		return (ITextEditor) editorPart;
	}

	private BookmarksTree getInitialTree() {
		BookmarksTreeBuilder bookmarksTreeBuilder = bookmarksTree("rootFolder");
		return bookmarksTreeBuilder.build();
	}

	private void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

	private void addBookmark(BookmarkId parentId, Bookmark bookmark) throws BookmarksException {
		bookmarkDatabase.modify(
				bookmarksTreeModifier -> bookmarksTreeModifier.addBookmarks(parentId, Lists.newArrayList(bookmark)));
	}
	
	private int getOffset(ITextEditor editor, int lineNumber) throws BadLocationException {
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		return document.getLineOffset(lineNumber);
	}

}
