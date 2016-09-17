package mesfavoris.java.javadoc;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;

import com.google.common.io.CharStreams;

public class JavadocCommentProvider {

	public String getJavadocCommentShortDescription(IMember member) {
		String javadocComment = getJavadocComment(member);
		if (javadocComment == null) {
			return null;
		}
		return getJavadocCommentShortDescription(javadocComment);
	}
	
	public String getJavadocComment(IMember member) {
		try (Reader reader = JavadocContentAccess.getContentReader(member, true)) {
			if (reader == null) {
				return null;
			}
			return CharStreams.toString(reader);
		} catch (JavaModelException|IOException e) {
			return null;
		}
	}	
	
	public String getJavadocCommentShortDescription(String javadoc) {
		int index = javadoc.indexOf("\n\n");
		if (index != -1) {
			javadoc = javadoc.substring(0, index);
		}
		index = javadoc.indexOf("<p>");
		if (index != -1) {
			javadoc = javadoc.substring(0, index);
		}
		javadoc = javadoc.replaceAll("<[a-zA-Z]*>", "");
		javadoc = javadoc.replaceAll("</[a-zA-Z]*>", "");
		javadoc = javadoc.replaceAll("\\{@inheritDoc\\}", "");
		javadoc = javadoc.replaceAll("\\{@(link|value|code|literal) (.*)\\}", "$2");
		return javadoc;
	}
	
}
