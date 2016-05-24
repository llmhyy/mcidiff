package mcidiff.model;

import mcidiff.util.ASTUtil;
import mcidiff.util.DiffUtil;
import mcidiff.util.TokenSimilarityComparator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;


public class Token {
	
	public static String episolonSymbol = "e*";
	
	private String tokenName;
	/**
	 * the minimum AST node which encloses the token.
	 */
	private ASTNode node;
	private CloneInstance cloneInstance;
	
	private int startPosition;
	private int endPosition;
	
	private Token previousToken;
	private Token postToken;
	
	private boolean isMarked;
	
	private double relativePositionRatio = 0;
	/**
	 * @param tokenName
	 * @param node
	 */
	public Token(String tokenName, ASTNode node, CloneInstance cloneInstance, 
			int startPosition, int endPosition) {
		
		super();
		if(cloneInstance == null){
			System.currentTimeMillis();
		}
		this.tokenName = tokenName;
		this.node = node;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.setCloneInstance(cloneInstance);
	}
	
	@Override
	public String toString(){
		return this.tokenName;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Token){
			Token thatToken = (Token)obj;

			if(thatToken.getNode() == null && getNode() == null){
				return getTokenName().equals(thatToken.getTokenName());
			}
			else if(thatToken.getNode() != null && getNode() != null){
				return getTokenName().equals(thatToken.getTokenName()) &&
						astMatch(thatToken.getNode(), getNode());
			}
		}
		
		return false;
	}
	
	/**
	 * mainly used to tolerant "." symbol.
	 * @param node1
	 * @param node2
	 * @return
	 */
	private boolean astMatch(ASTNode node1, ASTNode node2){
		if(node1 instanceof MethodInvocation && node2 instanceof SuperMethodInvocation){
			return true;
		}
		else if(node1 instanceof SuperMethodInvocation && node2 instanceof MethodInvocation){
			return true;
		}
		if(node1 instanceof FieldAccess && node2 instanceof QualifiedName){
			return true;
		}
		else if(node1 instanceof QualifiedName && node2 instanceof FieldAccess){
			return true;
		}
		else{
			if(node1 instanceof SimpleName && node2 instanceof SimpleName){
				SimpleName name1 = (SimpleName)node1;
				SimpleName name2 = (SimpleName)node2;
				
				boolean isUnmatchable = isUnmatchableSimpleNames(name1, name2);
				return !isUnmatchable;
			}
		}
		
		return node1.getNodeType() == node2.getNodeType();
	}
	
	/**
	 * @return the tokenName
	 */
	public String getTokenName() {
		return tokenName;
	}
	/**
	 * @param tokenName the tokenName to set
	 */
	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}
	/**
	 * @return the node
	 */
	public ASTNode getNode() {
		return node;
	}
	/**
	 * @param node the node to set
	 */
	public void setNode(ASTNode node) {
		this.node = node;
	}

	/**
	 * @return the cloneInstance
	 */
	public CloneInstance getCloneInstance() {
		return cloneInstance;
	}

	/**
	 * @param cloneInstance the cloneInstance to set
	 */
	public void setCloneInstance(CloneInstance cloneInstance) {
		this.cloneInstance = cloneInstance;
	}

	/**
	 * @return the isMarked
	 */
	public boolean isMarked() {
		return isMarked;
	}

	/**
	 * @param isMarked the isMarked to set
	 */
	public void setMarked(boolean isMarked) {
		this.isMarked = isMarked;
	}
	
	/**
	 * @return the startPosition
	 */
	public int getStartPosition() {
		return startPosition;
	}

	/**
	 * @param startPosition the startPosition to set
	 */
	public void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}

	/**
	 * @return the endPosition
	 */
	public int getEndPosition() {
		return endPosition;
	}

	/**
	 * @param endPosition the endPosition to set
	 */
	public void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}

	/**
	 * @return the previousToken
	 */
	public Token getPreviousToken() {
		return previousToken;
	}

	/**
	 * @param previousToken the previousToken to set
	 */
	public void setPreviousToken(Token previousToken) {
		this.previousToken = previousToken;
	}

	/**
	 * @return the postToken
	 */
	public Token getPostToken() {
		return postToken;
	}

	/**
	 * @param postToken the postToken to set
	 */
	public void setPostToken(Token postToken) {
		this.postToken = postToken;
	}

	public double compareWith(Token seedToken) {
		ASTNode seedNode = seedToken.getNode();
		ASTNode thisNode = getNode();
		if(seedNode.getNodeType() != thisNode.getNodeType()){
			return 0;
		}
		else{
			if(seedNode.getNodeType() == ASTNode.SIMPLE_NAME 
					&& thisNode.getNodeType() == ASTNode.SIMPLE_NAME){
				SimpleName seedName = (SimpleName)seedNode;
				SimpleName thisName = (SimpleName)thisNode;
				
				String str = seedName + ":" + thisName;
				if(str.contains("newValue") && str.contains("name")){
					System.currentTimeMillis();
					System.currentTimeMillis();
				}
				
				boolean isUnmatchable = isUnmatchableSimpleNames(seedName, thisName);
				if(isUnmatchable){
					return 0;
				}
			}
			
			double contextSim = new TokenSimilarityComparator().compute(this, seedToken);
			double textualSim = DiffUtil.compareStringSimilarity(seedToken.getTokenName(), getTokenName());
			
			return 0.1 + 0.8*textualSim + 0.2*contextSim;
		}
		
	}
	
	private boolean isUnmatchableSimpleNames(SimpleName seedName, SimpleName thisName){
		IBinding seedBinding = seedName.resolveBinding();
		IBinding thisBinding = thisName.resolveBinding();
		
		if(seedBinding != null && thisBinding != null){
			if(seedBinding.getKind() == IBinding.VARIABLE && thisBinding.getKind() == IBinding.METHOD){
				//viable for comparison
			}
			else if(seedBinding.getKind() == IBinding.METHOD && thisBinding.getKind() == IBinding.VARIABLE){
				//viable for comparison
			}
			else if(seedBinding.getKind() != thisBinding.getKind()){
				return true;
			}
			
			boolean isSeedDeclaration = ASTUtil.isSimpleNameDeclaration(seedBinding, seedName);
			boolean isThisDeclaration = ASTUtil.isSimpleNameDeclaration(thisBinding, thisName);

			if(isSeedDeclaration ^ isThisDeclaration){
				return true;
			}
			else if(isSeedDeclaration && isThisDeclaration){
				if(seedBinding.getKind() != thisBinding.getKind()){
					return true;
				}
			}
		}
		else{
			ASTNode seedParent = seedName.getParent();
			ASTNode thisParent = thisName.getParent();
			/**
			 * type should not be matched to method/field/variable
			 */
			if(seedParent instanceof Type || thisParent instanceof Type){
				if(seedParent.getNodeType() != thisParent.getNodeType()){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isEpisolon(){
		return getTokenName().equals(Token.episolonSymbol);
	}
	
	/**
	 * @return the relativePositionRatio
	 */
	public double getRelativePositionRatio() {
		return relativePositionRatio;
	}

	/**
	 * @param relativePositionRatio the relativePositionRatio to set
	 */
	public void setRelativePositionRatio(double relativePositionRatio) {
		this.relativePositionRatio = relativePositionRatio;
	}
}
