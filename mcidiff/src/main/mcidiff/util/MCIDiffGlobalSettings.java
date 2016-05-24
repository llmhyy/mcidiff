package mcidiff.util;

public class MCIDiffGlobalSettings {

	public static double tokenSimilarityThreshold = 0.0d;
	
	public static int tokenGapForMergeDiffRange = 1;
	
	/**
	 * When comparing two token w.r.t their context, I will check the parent ASTNode.
	 * However, if the parent is too large, e.g., the AST node containing thousands of
	 * tokens, it is meaningless to take this parent into consideration.
	 * 
	 * Therefore, a threshold is set to limit such parent-child bias. That is, if the
	 * length of parent node is n (e.g., 10) times larger than that of child node, such
	 * parent will not be considered.
	 */
	public static int godParentRatio = 5;
	
	/**
	 * the idea is that if the two to-be-compared synonym tokens are very far way considering
	 * their relative position, they are highly likely to be unmatched.
	 */
	public static double relativeThreshold = 0.0;
	
	/**
	 * Temporarily, I use this parameter to sacrifice accuracy. I need it when we are analyzing
	 * code history, in this case, efficiency is a more important fact.
	 */
	public static boolean roughCompare = false;
}
