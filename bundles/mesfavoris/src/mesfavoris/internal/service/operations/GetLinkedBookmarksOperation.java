package mesfavoris.internal.service.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.internal.markers.BookmarksMarkers;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

/**
 * Get bookmarks linked to current selection
 * 
 * @author cchabanois
 *
 */
public class GetLinkedBookmarksOperation {
	private final BookmarkDatabase bookmarkDatabase;

	public GetLinkedBookmarksOperation(BookmarkDatabase bookmarkDatabase) {
		this.bookmarkDatabase = bookmarkDatabase;
	}

	public List<Bookmark> getLinkedBookmarks(IWorkbenchPart part, ISelection selection) {
		ITextEditor textEditor = AdapterUtils.getAdapter(part, ITextEditor.class);
		if (textEditor != part) {
			selection = textEditor.getSelectionProvider().getSelection();
		}
		if (textEditor != null && selection instanceof TextSelection) {
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
		final IDocument document = getDocument(textEditor);
		if (document == null)
			return Collections.emptyList();

		final AbstractMarkerAnnotationModel model = getAnnotationModel(textEditor);
		if (model == null)
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
				MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
				IMarker marker = markerAnnotation.getMarker();
				try {
					if (BookmarksMarkers.MARKER_TYPE.equals(marker.getType())) {
						Position position = model.getPosition(markerAnnotation);
						if (includesLine(document, position, activeLine)) {
							markers.add(marker);
						}
					}
				} catch (CoreException e) {
					// could not get marker type
				}
			}
		}

		return Collections.unmodifiableList(markers);
	}

	private boolean includesLine(IDocument document, Position position, int line) {
		if (position == null) {
			return false;
		}
		try {
			int line1 = document.getLineOfOffset(position.getOffset());
			int line2 = position.getLength() == 0 ? line1
					: document.getLineOfOffset(position.getOffset() + position.getLength() - 1);
			return line >= line1 && line <= line2;
		} catch (BadLocationException x) {
			return false;
		}
	}

	private IDocument getDocument(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		return provider.getDocument(editor.getEditorInput());
	}

	private AbstractMarkerAnnotationModel getAnnotationModel(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
		if (model instanceof AbstractMarkerAnnotationModel)
			return (AbstractMarkerAnnotationModel) model;
		return null;
	}

}
