package mcidiff.util;

import mcidiff.model.Token;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class TokenSimilarityComparator{
	public double compute(Token token1, Token token2){
		
		if(token1.isEpisolon() && token2.isEpisolon()){
			return 1;
		}
		
		ASTNode node1 = token1.getNode();
		ASTNode node2 = token2.getNode();
		
		if(node1 != null && node2 != null){
			
			double positionSim = 1-Math.abs(token1.getRelativePositionRatio() - token2.getRelativePositionRatio());
			
			double textualSim = positionSim;
			double contextualSim = positionSim;
			
			// the idea is that if the two to-be-compared synonym tokens are very far way considering
			// their relative position, they are highly likely to be unmatched
			if(MCIDiffGlobalSettings.roughCompare == false && positionSim > MCIDiffGlobalSettings.relativeThreshold){
				if(node1 instanceof CompilationUnit && node2 instanceof CompilationUnit){
					//String title1 = ((AbstractTypeDeclaration)((CompilationUnit)node1).types().get(0)).getName().getIdentifier();
					//String title2 = ((AbstractTypeDeclaration)((CompilationUnit)node2).types().get(0)).getName().getIdentifier();
					//textualSim = (title1.equals(title2))? 1 : 0;
					textualSim = 1;
				}
				else{
					textualSim = FastASTNodeComparator.computeNodeSim(node1, node2);					
				}
				
				contextualSim = textualSim;
				
				// the following code could be commented for improving efficiency
				ASTNode richParent1 = getRichParent(token1);
				ASTNode richParent2 = getRichParent(token2);
				
//				if(token1.getTokenName().equals("app") && token2.getTokenName().equals("app")){
//					System.out.println("parent1: " + richParent1);
//					System.out.println("parent2: " + richParent2);
//				}
				
				if(!isGodParent(richParent1, node1) && !isGodParent(richParent2, node2)){
					contextualSim = FastASTNodeComparator.computeNodeSim(node1.getParent(), node2.getParent());
				}				
			}
			else{
				System.currentTimeMillis();
			}
			
			double sim = 0.7*contextualSim + 0.1*textualSim + 0.2*positionSim;
			
//			if(token1.getTokenName().equals("app") && token2.getTokenName().equals("app")){
//				System.out.println(sim);
//				System.out.println();
//			}
			
			return sim;
		}
		
		return 0;
	};	
	
	/**
	 * In some cases, the AST node of a token is exactly the same as the token name, in this case,
	 * the AST node used for the context cannot contain information to distinguish token context any more.
	 * For example, if the AST node is a SimpleName, then the context information is exactly as the 
	 * token itself. In such case, I need to find a better context, i.e., a parent AST node with more
	 * information.
	 * 
	 * @param node
	 * @return
	 */
	private ASTNode getRichParent(Token token) {
		ASTNode node = token.getNode();
		if(node == null){
			return node;
		} else{
			String nodeText = node.toString();
			while(nodeText.equals(token.getTokenName())){
				node = node.getParent();
				nodeText = node.toString();
				System.currentTimeMillis();
			}
			
			return node;
		}
	}

	private boolean isGodParent(ASTNode parent, ASTNode child){
		if(parent == null){
			return true;
		}
		else{
			int parentLen = parent.getLength();
			int childLen = child.getLength();
		
			return parentLen > MCIDiffGlobalSettings.godParentRatio*childLen;
		}
	}
}
