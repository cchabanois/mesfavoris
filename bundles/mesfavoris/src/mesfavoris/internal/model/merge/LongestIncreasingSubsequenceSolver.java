package mesfavoris.internal.model.merge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Determine longest increasing subsequence in O(N * logN)
 * 
 * https://sites.google.com/site/indy256/algo/lis_nlogn
 *
 */
public class LongestIncreasingSubsequenceSolver<E> {

	private final NodeComparator<E> nodeComparator;

	public LongestIncreasingSubsequenceSolver(Comparator<E> comparator) {
		this.nodeComparator = new NodeComparator<E>(comparator);
	}

	public List<E> lis(Iterable<E> elements) {
		List<Node<E>> pileTops = new ArrayList<Node<E>>();
		// sort into piles
		for (E element : elements) {
			Node<E> node = new Node<E>();
			node.element = element;
			int index = Collections.binarySearch(pileTops, node, nodeComparator);
			if (index < 0)
				index = ~index;
			if (index != 0)
				node.pointer = pileTops.get(index - 1);
			if (index != pileTops.size())
				pileTops.set(index, node);
			else
				pileTops.add(node);
		}
		// extract LIS from nodes
		List<E> result = new ArrayList<E>();
		for (Node<E> node = pileTops.size() == 0 ? null
				: pileTops.get(pileTops.size() - 1); node != null; node = node.pointer) {
			result.add(node.element);
		}
		Collections.reverse(result);
		return result;
	}

	private static class Node<E> {
		public E element;
		public Node<E> pointer;
	}

	private static class NodeComparator<E> implements Comparator<Node<E>> {
		private final Comparator<E> comparator;

		public NodeComparator(Comparator<E> comparator) {
			this.comparator = comparator;
		}

		@Override
		public int compare(Node<E> n1, Node<E> n2) {
			return comparator.compare(n1.element, n2.element);
		}

	}

}