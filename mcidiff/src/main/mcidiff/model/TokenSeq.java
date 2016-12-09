package mcidiff.model;

import java.util.ArrayList;
import java.util.Iterator;




//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IWorkspace;
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IPath;
//import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
//import org.eclipse.jface.text.BadLocationException;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.ui.editors.text.TextFileDocumentProvider;

/**
 * TokenSeq contains consecutive differential tokens.
 * 
 * @author linyun
 *
 */
public class TokenSeq implements DiffElement{
	private ArrayList<Token> tokens = new ArrayList<>();
	private String text;
	
	private boolean isMarked = false;
	
	public static TokenSeq createEpisolonTokenSeq(CloneInstance cloneInstance){
		Token episolonToken = new Token(Token.episolonSymbol, null, cloneInstance, -1, -1);
		TokenSeq episolonTokenSeq = new TokenSeq();
		episolonTokenSeq.addToken(episolonToken);
		
		return episolonTokenSeq;
	}

	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(Token token: tokens){
			buffer.append(token.getTokenName() + " ");
		}
		
		return buffer.toString();
	}
	
	public ASTNode getMinimumContainingASTNode(){
		if(isEpisolonTokenSeq()){
			return null;			
		}
		else{
			int bestMargin = -1;
			ASTNode bestNode = null;
			
			for(Token t: getTokens()){
				ASTNode containingNode = t.getNode();
				int margin = getContainingMargin(containingNode, getStartPosition(), getEndPosition());
				while(margin == -1){
					containingNode = containingNode.getParent();
					margin = getContainingMargin(containingNode, getStartPosition(), getEndPosition());
				}
				
				if(margin == 0){
					return containingNode;
				}
				
				if(bestMargin == -1){
					bestMargin = margin;
					bestNode = containingNode;
				}
				else{
					if(margin < bestMargin){
						bestMargin = margin;
						bestNode = containingNode;
					}
				}
			}
			
			return bestNode;
		}
	}
	
	/**
	 * return how many position does a AST node contains a given range,
	 * if the node does not contain the range, return -1.
	 * @return
	 */
	private int getContainingMargin(ASTNode node, int start, int end){
		int nodeStart = node.getStartPosition();
		int nodeEnd = nodeStart + node.getLength();
		
		if(nodeStart <= start && nodeEnd >= end){
			return (start-nodeStart) + (end-nodeEnd);
		}
		else{
			return -1;
		}
	}
	
	public boolean isCompeleteSyntaxUnit(){
		ASTNode node = getMinimumContainingASTNode();
		if(node == null){
			return true;
		}
		else{
			int start = node.getStartPosition();
			int end = start + node.getLength();
			
			return (start == getStartPosition() && end == getEndPosition());
		}
	}
	
	public int size(){
		return this.tokens.size();
	}
	
	public boolean isSingleToken(){
		return this.tokens.size() == 1;
	}
	
	public void retrieveTextFromDoc(){
		if(isEpisolonTokenSeq()){
			setText("");
		}
		else{
			/*Token token = getTokens().get(0);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IPath location = Path.fromOSString(token.getCloneInstance().getFileName());
			IFile file = workspace.getRoot().getFileForLocation(location);
			TextFileDocumentProvider provider = new TextFileDocumentProvider();
			
			try {
				provider.connect(file);
				IDocument doc = provider.getDocument(file);
				String content = doc.get(getStartPosition(), getPositionLength());
				this.text = content;
			} catch (BadLocationException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}*/
			this.text = tokens.toString();
		}
	}
	
	public void filterUselessEpsilonToken(){
		Iterator<Token> iter = this.tokens.iterator();
		while(iter.hasNext()){
			Token t = iter.next();
			if(t.isEpisolon()){
				if(!(tokens.size()==1 && tokens.get(0).isEpisolon())){
					iter.remove();
				}
			}
		}
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

	public String getText(){
		return text;
	}
	
	public void setText(String text){
		this.text = text;
	}

	@Override
	public int hashCode() {
		return getTokens().toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TokenSeq){
			TokenSeq seq = (TokenSeq)obj;
			if(seq.toString().equals(toString())){
				return true;
			}
		}
		return false;
	}

	public boolean isEpisolonTokenSeq(){
		if(getTokens().size() > 0){
			for(Token t: getTokens()){
				if(!t.isEpisolon()){
					return false;
				}
			}
			
			return true;
		}
		
		return true;
	}
	
	public int getPositionLength(){
		return getEndPosition() - getStartPosition();
	}
	
	/**
	 * @return the tokens
	 */
	public ArrayList<Token> getTokens() {
		return tokens;
	}

	/**
	 * @param tokens the tokens to set
	 */
	public void setTokens(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}
	
	public void addToken(Token t){
		if(!t.isEpisolon()){
			this.tokens.add(t);			
		}
		else if(this.tokens.size() == 0){
			this.tokens.add(t);	
		}
	}
	
	public CloneInstance getCloneInstance(){
		if(getTokens().size() != 0){
			return getTokens().get(0).getCloneInstance();
		}
		
		return null;
	}
	
	public int getStartPosition(){
		if(getTokens().size() != 0){
			return getTokens().get(0).getStartPosition();
		}
		
		return -1;
	}
	
	public int getEndPosition(){
		if(getTokens().size() != 0){
			return getTokens().get(getTokens().size()-1).getEndPosition();
		}
		
		return -1;
	}
	
	public class SyntaxCheck extends ASTVisitor{
		private boolean isComplete = false;
		
		@Override
		public void preVisit(ASTNode node){
			int nodeStart = node.getStartPosition();
			int nodeEnd = nodeStart + node.getLength();
			
			if(nodeStart == getStartPosition() && nodeEnd == getEndPosition()){
				isComplete = true;
			}
		}

		/**
		 * @return the isComplete
		 */
		public boolean isComplete() {
			return isComplete;
		}
	}

	public boolean isSyntaxComplete() {
		if(this.tokens.size() >= 2){
			Token firstToken = this.tokens.get(0);
			CompilationUnit cu = (CompilationUnit) firstToken.getNode().getRoot();
			
			SyntaxCheck checker = new SyntaxCheck();
			cu.accept(checker);
			
			return checker.isComplete();
		}
		
		return true;
	}
	
	/**
	 * Find the completely contained methods, fields, or inner classes. Therefore, the ASTNode returned
	 * by this method should only be MethodDeclaration, FieldDeclaration, and TypeDeclaration 
	 * @return
	 */
	public ArrayList<ASTNode> findContainedMembers(){
		ArrayList<ASTNode> nodeList = new ArrayList<>();
		if(!isEpisolonTokenSeq()){
			MemberRetriever retriever = new MemberRetriever();
			CompilationUnit unit = (CompilationUnit) this.getTokens().get(0).getNode().getRoot();
			unit.accept(retriever);
			
			nodeList = retriever.getContainedMembers();
		}
		
		return nodeList;
	}
	
	public class MemberRetriever extends ASTVisitor{
		private ArrayList<ASTNode> nodeList = new ArrayList<>();
		
		public boolean visit(MethodDeclaration method){
			if(containsASTNode(method)){
				nodeList.add(method);
			}	
			return false;
		}
		
		public boolean visit(FieldDeclaration field){
			if(containsASTNode(field)){
				nodeList.add(field);
			}
			return false;
		}
		
		public boolean visit(TypeDeclaration type){
			if(containsASTNode(type)){
				nodeList.add(type);
				return false;
			}
			return true;
		}
		
		private boolean containsASTNode(ASTNode node){
			int start = node.getStartPosition();
			int end = start + node.getLength();
			
			return start >= TokenSeq.this.getStartPosition() && end <= TokenSeq.this.getEndPosition();
		}
		
		public ArrayList<ASTNode> getContainedMembers(){
			return nodeList;
		}
	}
	
	
	
	
}
