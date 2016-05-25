package org.chabanois.mesfavoris.internal.model.merge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class MinimumMovesSolver {

	public static <E> Iterable<Move<E>> getMinimumMoves(List<E> source, List<E> target) {
		Map<E, Integer> sourceIndexes = getElementToIndexMap(source);
		Map<E, Integer> targetIndexes = getElementToIndexMap(target);
		LongestIncreasingSubsequenceSolver<E> lisSolver = new LongestIncreasingSubsequenceSolver<>(
				new ByIndexComparator<>(targetIndexes));
		List<E> nonMovingElements = lisSolver.lis(source);
		int numMovingElements = source.size() - nonMovingElements.size();
		int[] from = new int[numMovingElements];
		int[] to = new int[numMovingElements];
		Object[] elements = new Object[numMovingElements];
		int i = 0;
		for (E element : source) {
			boolean needMove = !nonMovingElements.contains(element);
			if (needMove) {
				elements[i] = element;
				from[i] = sourceIndexes.get(element);
				to[i] = targetIndexes.get(element);
				i++;
			}
		}
		// source positions are expressed in the prior order while the destination positions are in the final order.
		// We need to make adjustements for all the moves
		// see http://stackoverflow.com/questions/26322023/sort-array-in-the-minimum-number-of-moves
		adjustMoves(from,to);
		
		List<Move<E>> moves = new ArrayList<>(numMovingElements);
		for (i = 0; i < numMovingElements; i++) {
			moves.add(new Move<E>((E)elements[i], from[i], to[i]));
		}
		return moves;
	}

	private static void adjustMoves(int[] from, int[] to) {
		int size = from.length;
		for (int i = 0; i < size; i++) {
			for (int j = i+1; j < size;j++) {
				if (from[j] >= from[i]) {
					from[j]--;
				}
			}
		}
		for (int i = size-1; i >= 0; i--) {
			for (int j = i-1; j >= 0;j--) {
				if (to[j] > to[i]) {
					to[j]--;
				}
			}
		}
		for (int i = 0; i <= size-1; i++) {
			for (int j = size-1; j >= i+1; j--) {
				if (from[j] < to[i]) {
					to[i]++;
				} else {
					from[j]++;
				}
			}
		}		
	}
	
	private static <E> Map<E, Integer> getElementToIndexMap(Iterable<E> source) {
		Map<E, Integer> indexes = Maps.newHashMap();
		int i = 0;
		for (E element : source) {
			indexes.put(element, i);
			i++;
		}
		return indexes;
	}

	private static class ByIndexComparator<E> implements Comparator<E> {
		private final Map<E, Integer> indexes;

		public ByIndexComparator(Map<E, Integer> indexes) {
			this.indexes = indexes;
		}

		@Override
		public int compare(E e1, E e2) {
			int index1 = indexes.get(e1);
			int index2 = indexes.get(e2);
			return Integer.compare(index1, index2);
		}

	}

	public static class Move<E> {
		private final int from;
		private final int to;
		private final E element;
		
		public Move(E element, int from, int to) {
			this.element = element;
			this.from = from;
			this.to = to;
		}
		
		public E getElement() {
			return element;
		}
		
		public int getFrom() {
			return from;
		}
		
		public int getTo() {
			return to;
		}

		@Override
		public String toString() {
			return "[from=" + from + ", to=" + to + "]";
		}
		
		
	}
	
}
