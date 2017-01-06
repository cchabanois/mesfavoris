package mesfavoris.path.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import mesfavoris.commons.ui.wizards.datatransfer.BundleProjectImportOperation;
import mesfavoris.path.resource.FuzzyResourceFinder;

public class FuzzyResourceFinderTest {
	private final FuzzyResourceFinder fuzzyResourceFinder = new FuzzyResourceFinder();
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		importProjectFromTemplate("fuzzyResourceFinderTest", "commons-cli");
	}
	
	@Test
	public void testExactPath() {
		// When
		Optional<IResource> resource = fuzzyResourceFinder.find(new Path("/fuzzyResourceFinderTest/NOTICE.txt"), IResource.FILE);
		
		// Then
		assertTrue(resource.isPresent());
		assertEquals(new Path("/fuzzyResourceFinderTest/NOTICE.txt"), resource.get().getFullPath());
	}

	@Test
	public void testFuzzyPath() {
		// When
		Optional<IResource> resource = fuzzyResourceFinder.find(new Path("/fuzzyResourceFinderTest/org/apache/commons/cli/CommandLine.java"), IResource.FILE);
		
		// Then
		assertTrue(resource.isPresent());
		assertEquals(new Path("/fuzzyResourceFinderTest/src/main/java/org/apache/commons/cli/CommandLine.java"), resource.get().getFullPath());
	}	
	
	private static void importProjectFromTemplate(String projectName, String templateName)
			throws InvocationTargetException, InterruptedException {
		Bundle bundle = Platform.getBundle("mesfavoris.texteditor.tests");
		new BundleProjectImportOperation(bundle, projectName, "/projects/" + templateName + "/").run(null);
	}	
	
}
