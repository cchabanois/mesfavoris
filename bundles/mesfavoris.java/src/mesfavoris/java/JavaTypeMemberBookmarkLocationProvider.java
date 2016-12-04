package mesfavoris.java;

import static mesfavoris.java.JavaBookmarkProperties.KIND_FIELD;
import static mesfavoris.java.JavaBookmarkProperties.KIND_METHOD;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_DECLARING_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_KIND;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_NAME;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_METHOD_SIGNATURE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.google.common.collect.Lists;

import mesfavoris.bookmarktype.IBookmarkLocationProvider;
import mesfavoris.java.editor.JavaEditorUtils;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.text.matching.DocumentFuzzySearcher;

public class JavaTypeMemberBookmarkLocationProvider implements IBookmarkLocationProvider {

	@Override
	public JavaTypeMemberBookmarkLocation getBookmarkLocation(Bookmark bookmark, IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		List<IMember> memberCandidates = getMemberCandidates(bookmark, subMonitor.newChild(30));
		for (IMember member : memberCandidates) {
			LinePosition linePosition = getLineNumber(member, bookmark, subMonitor.newChild(70));
			if (linePosition != null) {
				return new JavaTypeMemberBookmarkLocation(member, linePosition.lineNumber, linePosition.lineOffset);
			}
		}
		if (memberCandidates.isEmpty()) {
			return null;
		} else {
			return new JavaTypeMemberBookmarkLocation(memberCandidates.get(0), null, null);
		}
	}

	private LinePosition getLineNumber(IMember member, Bookmark bookmark, IProgressMonitor monitor) {
		Integer estimatedLineNumber = getEstimatedLineNumber(member, bookmark);
		Integer lineNumber = estimatedLineNumber;
		Integer lineOffset = null;
		String lineContent = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_CONTENT);
		try {
			ITypeRoot typeRoot = member.getTypeRoot();
			if (typeRoot.getBuffer() == null) {
				return null;
			}
			Document document = new Document(typeRoot.getBuffer().getContents());
			if (lineContent != null) {
				DocumentFuzzySearcher searcher = new DocumentFuzzySearcher(document);

				int foundLineNumber = searcher.findLineNumber(getRegion(member.getSourceRange()),
						estimatedLineNumber == null ? -1 : estimatedLineNumber, lineContent, monitor);
				lineNumber = foundLineNumber == -1 ? estimatedLineNumber : foundLineNumber;
			}
			if (lineNumber == null) {
				return null;
			}
			lineOffset = getLineOffset(document, lineNumber);
			if (lineOffset == null) {
				return null;
			}
			return new LinePosition(lineNumber, lineOffset);
		} catch (JavaModelException e) {
			return null;
		}
	}

	private Integer getLineOffset(IDocument document, int lineNumber) {
		try {
			return document.getLineOffset(lineNumber);
		} catch (BadLocationException e) {
			return null;
		}
	}

	private IRegion getRegion(ISourceRange sourceRange) {
		return new Region(sourceRange.getOffset(), sourceRange.getLength());
	}

	private Integer getEstimatedLineNumber(IMember member, Bookmark bookmark) {
		try {
			Integer lineNumber = JavaEditorUtils.getLineNumber(member);
			if (lineNumber == null) {
				return null;
			}
			Integer lineNumberInsideElement = getLineNumberInsideElement(bookmark);
			if (lineNumberInsideElement != null) {
				lineNumber += lineNumberInsideElement;
			}
			return lineNumber;
		} catch (JavaModelException e) {
			return null;
		}
	}

	private Integer getLineNumberInsideElement(Bookmark bookmark) {
		String lineNumberString = bookmark.getPropertyValue(PROP_LINE_NUMBER_INSIDE_ELEMENT);
		if (lineNumberString == null) {
			return null;
		}
		try {
			return Integer.parseInt(lineNumberString);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private List<IMember> getMemberCandidates(Bookmark javaBookmark, IProgressMonitor monitor) {
		String type = javaBookmark.getPropertyValue(PROP_JAVA_TYPE);
		if (type != null) {
			List<IType> matchingTypes = searchType(type, monitor);
			return Lists.newArrayList(matchingTypes);
		}
		String declaringType = javaBookmark.getPropertyValue(PROP_JAVA_DECLARING_TYPE);
		if (declaringType == null) {
			return Collections.emptyList();
		}
		List<IMember> matchingMembers = searchType(declaringType, monitor).stream()
				.flatMap(matchingType -> getMemberCandidates(matchingType, javaBookmark).stream())
				.collect(Collectors.toList());
		return matchingMembers;
	}

	private List<IMember> getMemberCandidates(IType type, Bookmark javaBookmark) {
		String elementKind = javaBookmark.getPropertyValue(PROP_JAVA_ELEMENT_KIND);
		String elementName = javaBookmark.getPropertyValue(PROP_JAVA_ELEMENT_NAME);
		if (KIND_FIELD.equals(elementKind)) {
			IField field = type.getField(elementName);
			return field.exists() ? Lists.newArrayList(field) : Collections.emptyList();
		}
		if (KIND_METHOD.equals(elementKind)) {
			String signature = javaBookmark.getPropertyValue(PROP_JAVA_METHOD_SIGNATURE);
			IMethod method = null;
			if (signature != null) {
				method = getMethod(type, elementName, signature);
			}
			if (method == null) {
				List<IMethod> candidates = getMethodsWithName(type, elementName);
				return Lists.newArrayList(candidates);
			}
			return Lists.newArrayList(method);
		}
		if (JavaEditorUtils.isType(elementKind) && elementName != null) {
			IType memberType = type.getType(elementName);
			return memberType.exists() ? Lists.newArrayList(memberType) : Collections.emptyList();
		}
		return Collections.emptyList();
	}

	private IMethod getMethod(IType type, String name, String signature) {
		return getMethodsWithName(type, name).stream()
				.filter(method -> signature.equals(JavaEditorUtils.getMethodSimpleSignature(method))).findAny()
				.orElse(null);
	}

	private List<IMethod> getMethodsWithName(IType type, String name) {
		List<IMethod> methodsWithName = new ArrayList<IMethod>();
		try {
			for (IMethod method : type.getMethods()) {
				if (name.equals(method.getElementName())) {
					methodsWithName.add(method);
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
		return methodsWithName;
	}

	private List<IType> searchType(String classFQN, IProgressMonitor monitor) {
		classFQN = classFQN.replace('$', '.');
		final List<IType> types = new ArrayList<IType>();

		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(classFQN, IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);

		SearchRequestor requestor = new SearchRequestor() {
			public void acceptSearchMatch(final SearchMatch match) throws CoreException {
				TypeDeclarationMatch typeMatch = (TypeDeclarationMatch) match;
				IType type = (IType) typeMatch.getElement();
				types.add(type);
			}
		};
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
					requestor, monitor);
		} catch (final CoreException e) {
			return types;
		}

		return types;
	}

	private static class LinePosition {
		public final int lineNumber;
		public final int lineOffset;

		public LinePosition(int lineNumber, int lineOffset) {
			super();
			this.lineNumber = lineNumber;
			this.lineOffset = lineOffset;
		}

	}

}
