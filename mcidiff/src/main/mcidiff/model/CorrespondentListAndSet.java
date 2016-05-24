package mcidiff.model;

public class CorrespondentListAndSet{
	private TokenMultiset[] multisetList; 
	private Token[] commonTokenList;
	
	/**
	 * @param multisetList
	 * @param commonTokenList
	 */
	public CorrespondentListAndSet(TokenMultiset[] multisetList,
			Token[] commonTokenList) {
		super();
		this.multisetList = multisetList;
		this.commonTokenList = commonTokenList;
	}
	/**
	 * @return the multisetList
	 */
	public TokenMultiset[] getMultisetList() {
		return multisetList;
	}
	/**
	 * @param multisetList the multisetList to set
	 */
	public void setMultisetList(TokenMultiset[] multisetList) {
		this.multisetList = multisetList;
	}
	/**
	 * @return the commonTokenList
	 */
	public Token[] getCommonTokenList() {
		return commonTokenList;
	}
	/**
	 * @param commonTokenList the commonTokenList to set
	 */
	public void setCommonTokenList(Token[] commonTokenList) {
		this.commonTokenList = commonTokenList;
	}
}