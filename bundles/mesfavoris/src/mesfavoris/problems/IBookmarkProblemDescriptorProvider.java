package mesfavoris.problems;

public interface IBookmarkProblemDescriptorProvider {

	/**
	 * Get the bookmark problem descriptor for given bookmark problem type
	 * 
	 * @param type
	 * @return the descriptor (never return null)
	 */
	IBookmarkProblemDescriptor getBookmarkProblemDescriptor(String type);

}