package mesfavoris.java;

import org.eclipse.jdt.core.IMember;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class JavaTypeMemberBookmarkLocation implements IBookmarkLocation {
	private final IMember member;
	private final Integer lineNumber;
	private final Integer lineOffset;

	public JavaTypeMemberBookmarkLocation(IMember member, Integer lineNumber, Integer lineOffset) {
		this.member = member;
		this.lineNumber = lineNumber;
		this.lineOffset = lineOffset;
	}

	public IMember getMember() {
		return member;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public Integer getLineOffset() {
		return lineOffset;
	}
	
}