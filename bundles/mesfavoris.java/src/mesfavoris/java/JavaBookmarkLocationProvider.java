package mesfavoris.java;

import static mesfavoris.java.JavaBookmarkProperties.KIND_FIELD;
import static mesfavoris.java.JavaBookmarkProperties.KIND_METHOD;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_DECLARING_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_KIND;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_ELEMENT_NAME;
import static mesfavoris.java.JavaBookmarkProperties.PROP_JAVA_TYPE;
import static mesfavoris.java.JavaBookmarkProperties.PROP_LINE_NUMBER_INSIDE_ELEMENT;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import mesfavoris.java.editor.JavaEditorUtils;
import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.TextEditorBookmarkProperties;
import mesfavoris.texteditor.text.matching.DocumentFuzzySearcher;

/**
 * Get the
 * 
 * @author cchabanois
 *
 */
public class JavaBookmarkLocationProvider {

	public JavaEditorBookmarkLocation findLocation(Bookmark bookmark) {
		IMember member = getMember(bookmark);
		if (member == null) {
			return null;
		}
		Integer lineNumber = getLineNumber(member, bookmark);
		return new JavaEditorBookmarkLocation(member, lineNumber);
	}

	private Integer getLineNumber(IMember member, Bookmark bookmark) {
		Integer estimatedLineNumber = getEstimatedLineNumber(member, bookmark);
		String lineContent = bookmark.getPropertyValue(TextEditorBookmarkProperties.PROP_LINE_CONTENT);
		if (lineContent == null) {
			return estimatedLineNumber;
		}
		try {
			ITypeRoot typeRoot = member.getTypeRoot();
			Document document = new Document(typeRoot.getBuffer().getContents());
			DocumentFuzzySearcher searcher = new DocumentFuzzySearcher(document);

			int lineNumber = searcher.findLineNumber(getRegion(member.getSourceRange()),
					estimatedLineNumber == null ? -1 : estimatedLineNumber, lineContent, new NullProgressMonitor());
			return lineNumber == -1 ? null : lineNumber;
		} catch (JavaModelException e) {
			return estimatedLineNumber;
		}
	}

	private IRegion getRegion(ISourceRange sourceRange) {
		return new Region(sourceRange.getOffset(), sourceRange.getLength());
	}

	private Integer getEstimatedLineNumber(IMember member, Bookmark bookmark) {
		try {
			int lineNumber = JavaEditorUtils.getLineNumber(member);
			Integer lineNumberInsideElement = getLineNumberInsideElement(bookmark);
			if (lineNumberInsideElement != null) {
				lineNumber += lineNumberInsideElement;
			}
			return lineNumber;
		} catch (JavaModelException e) {
			return null;
		} catch (BadLocationException e) {
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

	private IMember getMember(Bookmark javaBookmark) {
		String type = javaBookmark.getPropertyValue(PROP_JAVA_TYPE);
		if (type != null) {
			List<IType> matchingTypes = searchType(type);
			if (matchingTypes.size() == 0) {
				return null;
			}
			IType matchingType = matchingTypes.get(0);
			return matchingType;
		}
		String declaringType = javaBookmark.getPropertyValue(PROP_JAVA_DECLARING_TYPE);
		if (declaringType == null) {
			return null;
		}
		List<IType> matchingTypes = searchType(declaringType);
		if (matchingTypes.size() == 0) {
			return null;
		}
		IType matchingType = matchingTypes.get(0);
		IMember member = getMember(matchingType, javaBookmark);
		return member;
	}

	private IMember getMember(IType type, Bookmark javaBookmark) {
		String elementKind = javaBookmark.getPropertyValue(PROP_JAVA_ELEMENT_KIND);
		String elementName = javaBookmark.getPropertyValue(PROP_JAVA_ELEMENT_NAME);
		if (KIND_FIELD.equals(elementKind)) {
			IField field = type.getField(elementName);
			return field.exists() ? field : null;
		}
		if (KIND_METHOD.equals(elementKind)) {
			List<IMethod> candidates = getMethodsWithName(type, elementName);
			return candidates.get(0);
		}
		if (JavaEditorUtils.isType(elementKind) && elementName != null) {
			IType memberType = type.getType(elementName);
			return memberType.exists() ? memberType : null;
		}
		return null;
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

	private List<IType> searchType(String classFQN) {
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
					requestor, new NullProgressMonitor());
		} catch (final CoreException e) {
			return types;
		}

		return types;
	}

	public static class JavaEditorBookmarkLocation {
		private final IMember member;
		private final Integer lineNumber;

		public JavaEditorBookmarkLocation(IMember member, Integer lineNumber) {
			this.member = member;
			this.lineNumber = lineNumber;
		}

		public IMember getMember() {
			return member;
		}

		public Integer getLineNumber() {
			return lineNumber;
		}

	}

}
