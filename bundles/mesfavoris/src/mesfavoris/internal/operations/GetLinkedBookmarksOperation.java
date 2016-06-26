package mesfavoris.internal.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class GetLinkedBookmarksOperation {
	private final BookmarkDatabase bookmarkDatabase;

	public GetLinkedBookmarksOperation(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	public List<Bookmark> getLinkedBookmarks(IWorkbenchPart part, ISelection selection) {
		if (part instanceof ITextEditor && selection instanceof TextSelection) {
			ITextEditor textEditor = (ITextEditor) part;
			TextSelection textSelection = (TextSelection) selection;
			int line = textSelection.getStartLine();
			return getLinkedBookmarks(textEditor, line);
		}
		return Collections.emptyList();
	}
	
	private List<Bookmark> getLinkedBookmarks(ITextEditor textEditor, int activeLine) {
		List<IMarker> bookmarkMarkers = getBookmarkMarkers(textEditor, activeLine);
		final BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
		return bookmarkMarkers.stream().map(marker -> getBookmarkId(marker)).filter(Optional::isPresent)
				.map(Optional::get).map(bookmarkId -> bookmarksTree.getBookmark(bookmarkId))
				.filter(bookmark -> bookmark != null).collect(Collectors.toList());
	}

	private Optional<BookmarkId> getBookmarkId(IMarker marker) {
		String attributeBookmarkId;
		try {
			attributeBookmarkId = (String) marker.getAttribute(BookmarksMarkers.BOOKMARK_ID);
			if (attributeBookmarkId == null) {
				return Optional.empty();
			}
			return Optional.of(new BookmarkId(attributeBookmarkId));
		} catch (CoreException e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private List<IMarker> getBookmarkMarkers(ITextEditor textEditor, int activeLine) {
		final IResource resource = getResource(textEditor);
		if (resource == null || !resource.exists())
			return Collections.emptyList();

		final IDocument document = getDocument(textEditor);
		if (document == null)
			return Collections.emptyList();

		final AbstractMarkerAnnotationModel model = getAnnotationModel(textEditor);
		if (model == null)
			return Collections.emptyList();

		final IMarker[] allMarkers;
		try {
			allMarkers = resource.findMarkers(BookmarksMarkers.MARKER_TYPE, true, IResource.DEPTH_ZERO);
		} catch (CoreException x) {
			// handleCoreException(x,
			// TextEditorMessages.SelectMarkerRulerAction_getMarker);
			return Collections.emptyList();
		}

		if (allMarkers.length == 0)
			return Collections.emptyList();

		if (activeLine == -1)
			return Collections.emptyList();

		Iterator<Annotation> it;
		try {
			IRegion line = document.getLineInformation(activeLine);
			it = model.getAnnotationIterator(line.getOffset(), line.getLength() + 1, true, true);
		} catch (BadLocationException e) {
			it = model.getAnnotationIterator();
		}
		List<IMarker> markers = new ArrayList<>();
		while (it.hasNext()) {
			Annotation annotation = it.next();
			if (annotation instanceof MarkerAnnotation) {
				Position position = model.getPosition(annotation);
				if (includesLine(position, document, activeLine)) {
					markers.add(((MarkerAnnotation) annotation).getMarker());
				}
			}
		}

		return Collections.unmodifiableList(markers);
	}

	private boolean includesLine(Position position, IDocument document, int line) {
		if (position != null) {
			try {
				int markerLine = document.getLineOfOffset(position.getOffset());
				if (line == markerLine)
					return true;
			} catch (BadLocationException x) {
			}
		}

		return false;
	}

	private IDocument getDocument(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		return provider.getDocument(editor.getEditorInput());
	}

	private IResource getResource(ITextEditor editor) {
		IEditorInput input = editor.getEditorInput();

		IResource resource = (IResource) input.getAdapter(IFile.class);

		if (resource == null)
			resource = (IResource) input.getAdapter(IResource.class);

		return resource;
	}

	private AbstractMarkerAnnotationModel getAnnotationModel(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel)
			return (AbstractMarkerAnnotationModel) model;
		return null;
	}

}
