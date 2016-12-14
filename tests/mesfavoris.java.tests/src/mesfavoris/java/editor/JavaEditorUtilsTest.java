package mesfavoris.java.editor;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;

public class JavaEditorUtilsTest {
	private IJavaProject javaProject;

	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate("JavaEditorUtilsTest", "commons-cli");

	}

	@Before
	public void setUp() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("JavaEditorUtilsTest");
		javaProject = JavaCore.create(project);
	}

	@Test
	public void testGetLineNumber() throws JavaModelException {
		// Given
		IType type = javaProject.findType("org.apache.commons.cli.BasicParser");

		// When
		int lineNumber = JavaEditorUtils.getLineNumber(type);

		// Then
		assertEquals(27, lineNumber);
	}

	@Test
	public void testMethodSimpleSignature() throws JavaModelException {
		// Given
		IType type = javaProject.findType("org.apache.commons.cli.BasicParser");
		IMethod method = getAnyMethodWithName(type, "flatten").get();

		// When
		String simpleSignature = JavaEditorUtils.getMethodSimpleSignature(method);

		// Then
		assertEquals("String[] flatten(Options,String[],boolean)", simpleSignature);
	}

	private Optional<IMethod> getAnyMethodWithName(IType type, String name) throws JavaModelException {
		return Arrays.stream(type.getMethods()).filter(method -> method.getElementName().equals(name)).findFirst();
	}

	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.java.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}

}
