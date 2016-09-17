package mesfavoris.java.javadoc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JavadocCommentProviderTest {
	private JavadocCommentProvider javadocCommentProvider = new JavadocCommentProvider();

	@Test
	public void testJavadocCommentShortDescription() {
		// Given
		String javadoc = "Query to see if this Option requires an argument\n\n@return boolean flag indicating if an argument is required";
		
		// When
		String shortDescription = shortDescription(javadoc);
		
		// Then
		assertEquals("Query to see if this Option requires an argument", shortDescription);
	}

	@Test
	public void testJavadocCommentWithHtmlTags() {
		// Given
		String javadoc = "Returns the specified value of this Option or \n"
				+ "<code>null</code> if there is no value.\n" + "\n"
				+ "@return the value/first value of this Option or \n" + " <code>null</code> if there is no value.";
		// When
		String shortDescription = shortDescription(javadoc);

		// Then
		assertEquals("Returns the specified value of this Option or \nnull if there is no value.", shortDescription);
	}
	
	@Test
	public void testJavadocCommentWithTags() {
		String javadoc = "Returns the specified value of this Option or \n"
				+ "{@code null} if there is no value.\n" + "\n"
				+ "@return the value/first value of this Option or \n" + " <code>null</code> if there is no value.";
		// When
		String shortDescription = shortDescription(javadoc);

		// Then
		assertEquals("Returns the specified value of this Option or \nnull if there is no value.", shortDescription);		
		
	}
	
	private String shortDescription(String javadoc) {
		return javadocCommentProvider.getJavadocCommentShortDescription(javadoc);
	}

}
