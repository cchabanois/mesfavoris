package mesfavoris.java;

import org.eclipse.jdt.core.IMember;

import mesfavoris.bookmarktype.IBookmarkLocation;

public class JavaTypeMemberBookmarkLocation implements IBookmarkLocation {
	private final IMember member;
	private final Integer lineNumber;

	public JavaTypeMemberBookmarkLocation(IMember member, Integer lineNumber) {
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