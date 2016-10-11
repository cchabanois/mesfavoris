package mesfavoris.java.javadoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Whitelist;

import com.google.common.io.CharStreams;

public class JavadocCommentProvider {

	public String getJavadocCommentShortDescription(IMember member) {
		String javadocComment = getJavadocCommentAsText(member);
		if (javadocComment == null) {
			return null;
		}
		return getJavadocCommentShortDescription(javadocComment);
	}

	private String getJavadocCommentAsText(IMember member) {
		try (Reader reader = JavadocContentAccess.getHTMLContentReader(member, true, true)) {
			if (reader == null) {
				return null;
			}
			String javadocAsHtml = CharStreams.toString(reader);
			String javadocAsString = Jsoup.clean(javadocAsHtml, "", Whitelist.none(), new OutputSettings().prettyPrint(false));
			
			// trim lines
			try (BufferedReader bufferedReader = new BufferedReader(new StringReader(javadocAsString))) {
				return bufferedReader.lines().map(line->line.trim()).collect(Collectors.joining("\n"));
			}
		} catch (JavaModelException | IOException e) {
			return null;
		}
	}

	private String getJavadocCommentShortDescription(String javadoc) {
		int index = javadoc.indexOf("\n\n");
		if (index != -1) {
			javadoc = javadoc.substring(0, index);
		}
		return javadoc;
	}

}
