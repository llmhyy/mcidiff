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
				if(!isGodParent(node1) && !isGodParent(node2)){
					contextualSim = FastASTNodeComparator.computeNodeSim(node1.getParent(), node2.getParent());
				}				
			}
			else{
				System.currentTimeMillis();
			}
			
			//double avgWeight = (1.0)/3;
			return 0.3*contextualSim + 0.4*textualSim + 0.3*positionSim;
		}
		
		return 0;
	};	
	
	private boolean isGodParent(ASTNode child){
		ASTNode parent = child.getParent();
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
