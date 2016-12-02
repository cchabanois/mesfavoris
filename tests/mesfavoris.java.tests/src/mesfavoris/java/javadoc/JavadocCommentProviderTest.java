package mesfavoris.java.javadoc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.JavaElement;
import org.junit.Test;

public class JavadocCommentProviderTest {
	private JavadocCommentProvider javadocCommentProvider = new JavadocCommentProvider();

	@Test
	public void testJavadocCommentShortDescription() throws Exception {
		// Given
		String javadoc = 
				  "/**\n "
				+ " * Query to see if this Option requires an argument\n"
				+ " * \n"
				+ " * @return boolean flag indicating if an argument is required\n"
				+ " */";
		
		// When
		String shortDescription = shortDescription(javadoc);
		
		// Then
		assertEquals("Query to see if this Option requires an argument", shortDescription);
	}

	@Test
	public void testJavadocCommentWithHtmlTags() throws Exception {
		// Given
		String javadoc = 
				  "/**\n "
				+ " * Returns the specified value of this Option or \n"
				+ " * <code>null</code> if there is no value.\n"
				+ " * \n"
				+ " * @return the value/first value of this Option or \n"
				+ " * <code>null</code> if there is no value.\n"
				+ " */";
		// When
		String shortDescription = shortDescription(javadoc);

		// Then
		assertEquals("Returns the specified value of this Option or\nnull if there is no value.", shortDescription);
	}
	
	@Test
	public void testJavadocCommentWithTags() throws Exception {
		// Given
		String javadoc = 
				  "/**\n "
				+ " * Returns the specified value of this Option or \n"
				+ " * {@code null} if there is no value.\n"
				+ " * \n"
				+ " * @return the value/first value of this Option or \n"
				+ " * <code>null</code> if there is no value.\n"
				+ " */";
		// When
		String shortDescription = shortDescription(javadoc);

		// Then
		assertEquals("Returns the specified value of this Option or\nnull if there is no value.", shortDescription);		
		
	}
	
	@Test
	public void testJavadocWithEncodedCharacters() throws Exception {
		// Given
		String javadoc = 
				  "/**\n "
				+ " * A program element annotated &#64;Deprecated is one that programmers are discouraged from using\n"
			    + " */";
	
		// When
		String shortDescription = shortDescription(javadoc);

		// Then
		assertEquals("A program element annotated @Deprecated is one that programmers are discouraged from using", shortDescription);		

		
	}
	
	// javadoc tags are not ignored but this should probably be the case though
	@Test
	public void testJavadocTagNotIgnored() throws Exception {
		// Given
		String javadoc = 
				  "/**\n "
				+ " * @author me\n"
			    + " */";
	
		// When
		String shortDescription = shortDescription(javadoc);

		// Then
		assertEquals("Author:me", shortDescription);			
	}
	
	private String shortDescription(String javadoc) throws Exception {
		IMember member = mock(IMember.class);
		IOpenable openable = (IOpenable)mock(JavaElement.class, withSettings().extraInterfaces(IOpenable.class));
		when(member.getOpenable()).thenReturn(openable);
		IBuffer buffer = BufferManager.createBuffer(openable);
		buffer.setContents(javadoc);
		when(openable.getBuffer()).thenReturn(buffer);
		when(member.getJavadocRange()).thenReturn(new SourceRange(0, javadoc.length()));
		return javadocCommentProvider.getJavadocCommentShortDescription(member);
	}

}
