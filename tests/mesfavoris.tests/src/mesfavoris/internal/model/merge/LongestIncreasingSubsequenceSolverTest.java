package mesfavoris.internal.model.merge;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import mesfavoris.internal.model.merge.LongestIncreasingSubsequenceSolver;

import static org.junit.Assert.*;

public class LongestIncreasingSubsequenceSolverTest {

	@Test
	public void testLis() {
		// Given
		LongestIncreasingSubsequenceSolver<Integer> lis = new LongestIncreasingSubsequenceSolver<>(Comparator.<Integer>naturalOrder());
		
		// When
		List<Integer> result = lis.lis(Arrays.asList(0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15));
		
		// Then
		assertEquals(6, result.size());
		assertEquals(Arrays.asList(0, 2, 6, 9, 11, 15), result);
	}

}
