package mesfavoris.path.internal.resource;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.Path;
import org.junit.Test;

import mesfavoris.path.internal.resource.ResourcePathDistanceComputer;

public class ResourcePathDistanceComputerTest {
	private final ResourcePathDistanceComputer resourcePathDistanceComputer = new ResourcePathDistanceComputer();

	@Test
	public void testNoDistanceBetweenSamePath() {
		assertEquals(0, resourcePathDistanceComputer.distance(new Path("/project1/src/package/MyClass.java"),
				new Path("/project1/src/package/MyClass.java")));
	}

	@Test
	public void testPathSegmentChanged() {
		assertEquals(1, resourcePathDistanceComputer.distance(new Path("/project1/src/packageRenamed/MyClass.java"),
				new Path("/project1/src/package/MyClass.java")));
	}

	@Test
	public void testPathSegmentAdded() {
		assertEquals(1, resourcePathDistanceComputer.distance(new Path("/project1/src/package/subPackage/MyClass.java"),
				new Path("/project1/src/package/MyClass.java")));
	}

	@Test
	public void testPathSegmentRemoved() {
		assertEquals(1, resourcePathDistanceComputer.distance(new Path("/project1/src/MyClass.java"),
				new Path("/project1/src/package/MyClass.java")));
	}

	@Test
	public void testDifferentFileName() {
		assertEquals(Integer.MAX_VALUE, resourcePathDistanceComputer.distance(
				new Path("/project1/src/package/AnotherClass.java"), new Path("/project1/src/package/MyClass.java")));
	}

	@Test
	public void testDifferentProjectName() {
		assertEquals(ResourcePathDistanceComputer.PROJECT_DIFFERENT_PENALTY + 1, resourcePathDistanceComputer.distance(
				new Path("/project2/src/package/MyClass.java"), new Path("/project1/src/package/MyClass.java")));
	}

}
