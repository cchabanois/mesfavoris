package mesfavoris.internal.model.merge;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.chabanois.mesfavoris.internal.model.merge.MinimumMovesSolver;
import org.chabanois.mesfavoris.internal.model.merge.MinimumMovesSolver.Move;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MinimumMovesSolverTest {

	// http://stackoverflow.com/questions/26322023/sort-array-in-the-minimum-number-of-moves
	@Test
	public void testMinimumMoves() {
		// Given
		List<String> source = Lists.newArrayList("1", "8", "5", "2", "4", "6", "3", "9", "7", "10");
		List<String> target = Lists.newArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
		
		// When
		Iterable<Move<String>> moves = MinimumMovesSolver.getMinimumMoves(source, target);
		applyMoves(source, moves);
		
		// Then
		assertEquals(4, Iterables.size(moves));
		assertEquals(target, source);
	}

	private void applyMoves(List<String> source, Iterable<Move<String>> moves) {
		for (Move<String> move : moves) {
			String element = source.remove(move.getFrom());
			assertEquals(move.getElement(), element);
			source.add(move.getTo(), element);
		}
	}
	

	
}
