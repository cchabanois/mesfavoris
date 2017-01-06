package mesfavoris.path.internal.resource;

import org.eclipse.core.runtime.IPath;

/**
 * Compute a distance between two paths with same last segment
 * 
 * @author cchabanois
 *
 */
public class ResourcePathDistanceComputer {
	public final static int PROJECT_DIFFERENT_PENALTY = 4;
	
	public int distance(IPath path1, IPath path2) {
		if (path1.isEmpty() || path2.isEmpty()) {
			return Integer.MAX_VALUE;
		}
		if (!path1.lastSegment().equals(path2.lastSegment())) {
			return Integer.MAX_VALUE;
		}
		int distance = 0;
		if (!path1.segment(0).equals(path2.segment(0))) {
			// penalty if not same project
			distance += PROJECT_DIFFERENT_PENALTY;
		}
		distance += levenshteinDistance(path1.segments(), path2.segments());
		return distance;
	}

	private int levenshteinDistance(String[] path1Segments, String[] path2Segments) {
		int len0 = path1Segments.length + 1;
		int len1 = path2Segments.length + 1;
		int[] cost = new int[len0];
		int[] newCost = new int[len0];
		for (int i = 0; i < len0; i++) {
			cost[i] = i;
		}
		for (int j = 1; j < len1; j++) {
			newCost[0] = j;
			for (int i = 1; i < len0; i++) {
				int match = (path1Segments[i - 1].equals(path2Segments[j - 1])) ? 0 : 1;

				// computing cost for each transformation
				int costReplace = cost[i - 1] + match;
				int costInsert = cost[i] + 1;
				int costDelete = newCost[i - 1] + 1;

				// keep minimum cost
				newCost[i] = Math.min(Math.min(costInsert, costDelete), costReplace);
			}

			// swap cost/newcost arrays
			int[] swap = cost;
			cost = newCost;
			newCost = swap;
		}
		return cost[len0 - 1];
	}

}
