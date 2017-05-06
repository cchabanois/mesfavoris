package mesfavoris.internal.service.operations;

import static mesfavoris.tests.commons.bookmarks.BookmarkBuilder.bookmark;
import static mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder.bookmarksTree;
import static mesfavoris.tests.commons.waits.Waiter.waitUntil;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_LINE_NUMBER;
import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_WORKSPACE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import com.google.common.collect.Lists;

import mesfavoris.BookmarksException;
import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkMarkerAttributesProvider;
import mesfavoris.internal.bookmarktypes.extension.PluginBookmarkTypes;
import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.tests.commons.bookmarks.BookmarksTreeBuilder;

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
		PluginBookmarkTypes pluginBookmarkTypes = new PluginBookmarkTypes();
		this.bookmarksMarkers = new BookmarksMarkers(bookmarkDatabase,
				new PluginBookmarkMarkerAttributesProvider(pluginBookmarkTypes));
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
		Bookmark bookmark = bookmark(new BookmarkId(), "LICENSE.txt")
				.withProperty(PROP_WORKSPACE_PATH, "/testGetLinkedBookmarks/LICENSE.txt")
				.withProperty(PROP_LINE_NUMBER, "10").build();
		addBookmark(rootFolderId, bookmark);
		waitUntil("Cannot find marker", () -> bookmarksMarkers.findMarker(bookmark.getId(), null));

		// When
		IEditorPart editorPart = openEditor(new Path("/testGetLinkedBookmarks/LICENSE.txt"));
		ITextEditor textEditor = getTextEditor(editorPart);
		selectAndReveal(textEditor, getOffset(textEditor, 10));
		List<Bookmark> bookmarks = operation.getLinkedBookmarks(textEditor, getSelection(textEditor));

		// Then
		assertEquals(1, bookmarks.size());
		assertEquals(bookmark, bookmarks.get(0));
	}

	@Test
	public void testGetLinkedBookmarksInMultipageEditor() throws Exception {
		// Given
		importProjectFromTemplate("testGetLinkedBookmarksInMultipageEditor", "commons-cli");
		Bookmark bookmark = bookmark("pom.xml")
				.withProperty(PROP_WORKSPACE_PATH, "/testGetLinkedBookmarksInMultipageEditor/pom.xml")
				.withProperty(PROP_LINE_NUMBER, "10").build();
		addBookmark(rootFolderId, bookmark);
		waitUntil("Cannot find marker", () -> bookmarksMarkers.findMarker(bookmark.getId(), null));

		// When
		IEditorPart editorPart = openEditor(new Path("/testGetLinkedBookmarksInMultipageEditor/pom.xml"));
		// that's why we require org.eclipse.m2e.editor in MANIFEST.MF
		assertThat(editorPart).isInstanceOf(MultiPageEditorPart.class);
		ITextEditor textEditor = getTextEditor(editorPart);
		selectAndReveal(textEditor, getOffset(textEditor, 10));
		List<Bookmark> bookmarks = operation.getLinkedBookmarks(textEditor, getSelection(textEditor));

		// Then
		assertEquals(1, bookmarks.size());
		assertEquals(bookmark, bookmarks.get(0));
	}

	private ISelection getSelection(ITextEditor textEditor) {
		return UIThreadRunnable.syncExec(() -> textEditor.getSelectionProvider().getSelection());
	}

	private IEditorPart openEditor(IPath path) throws PartInitException {
		return UIThreadRunnable.syncExec(() -> {
			try {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				return IDE.openEditor(window.getActivePage(), workspaceRoot.getFile(path));
			} catch (PartInitException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private ITextEditor getTextEditor(IEditorPart editorPart) {
		return UIThreadRunnable.syncExec(() -> Adapters.adapt(editorPart, ITextEditor.class));
	}

	private void selectAndReveal(ITextEditor textEditor, int offset) {
		UIThreadRunnable.syncExec(() -> textEditor.selectAndReveal(offset, 0));
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
