package mcidiff.main;

import java.util.ArrayList;

import mcidiff.model.CloneInstance;
import mcidiff.model.Token;

public class TokenSequence {
	private int startIndex = 0;
	private int endIndex = 0;
	private CloneInstance cloneInstance;
	
	/**
	 * @param tokenList
	 */
	public TokenSequence(CloneInstance cloneInstance) {
		super();
		this.setCloneInstance(cloneInstance);
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i=startIndex+1; i<=endIndex-1; i++){
			buffer.append(getCloneInstance().getTokenList().get(i));
		}
		return buffer.toString();
	}
	
	/**
	 * @return the cursorIndex
	 */
	public int getEndIndex() {
		return endIndex;
	}
	/**
	 * @param cursorIndex the cursorIndex to set
	 */
	public void setCursorIndex(int cursorIndex) {
		this.endIndex = cursorIndex;
	}
	/**
	 * @return the tokenList
	 */
	public ArrayList<Token> getTokenList() {
		return getCloneInstance().getTokenList();
	}
	
	/**
	 * @return the startIndex
	 */
	public int getStartIndex() {
		return startIndex;
	}
	/**
	 * @param startIndex the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	
	/**
	 * move cursor to the nearest following token equal to commonToken.
	 * @param commonToken
	 */
	public Token moveEndCursorTo(Token commonToken) {
		for(int i=startIndex+1; i<getLength(); i++){
			if(getTokenList().get(i) == commonToken){
				this.endIndex = i;
				return getTokenList().get(i);
			}
		}
		
		return null;
	}
	
	public Token get(int index){
		return getCloneInstance().getTokenList().get(index);
	}
	
	public void moveStartCursorToEndCursor() {
		this.startIndex = this.endIndex;
	}
	
	public void moveEndCursorToSeqEnd() {
		this.endIndex = getLength();
	}
	
	public int getLength(){
		return getTokenList().size();
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
	
	public boolean isRangeEmpty(){
		return this.startIndex == this.endIndex - 1;
	}
}
