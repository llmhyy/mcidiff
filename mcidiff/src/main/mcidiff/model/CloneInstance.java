package mcidiff.model;

import java.util.ArrayList;


public class CloneInstance {
	
	private CloneSet set;
	
	/**
	 * @return the set
	 */
	public CloneSet getSet() {
		return set;
	}

	/**
	 * @param set the set to set
	 */
	public void setSet(CloneSet set) {
		this.set = set;
	}

	private String fileName;
	private int startLine;
	private int endLine;
	
	private String fileContent;
	
	private ArrayList<Token> tokenList = new ArrayList<>();
	
	/**
	 * @param fileName
	 * @param startLine
	 * @param endLine
	 */
	public CloneInstance(String fileName, int startLine, int endLine) {
		super();
		this.fileName = fileName;
		this.startLine = startLine;
		this.endLine = endLine;
	}
	
	public CloneInstance(CloneSet set, String fileName, int startLine, int endLine) {
		super();
		this.set = set;
		this.fileName = fileName;
		this.startLine = startLine;
		this.endLine = endLine;
	}
	
	public void computeInnerTokenRelativePosition(){
		for(int i=0; i<getTokenList().size(); i++){
			Token token = getTokenList().get(i);
			double ratio = ((double)i)/getTokenList().size();
			token.setRelativePositionRatio(ratio);
		}
	}
	
	@Override
	public String toString() {
		return "Set ID: " + set.getId() + "; CloneInstance [fileName=" + fileName + ", startLine="
				+ startLine + ", endLine=" + endLine + "]";
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof CloneInstance){
			CloneInstance cloneInstance = (CloneInstance)obj;
			return cloneInstance.getFileName().equals(getFileName()) &&
					cloneInstance.getStartLine() == getStartLine() &&
					cloneInstance.getEndLine() == getEndLine();
		}
		
		return false;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the startLine
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * @param startLine the startLine to set
	 */
	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	/**
	 * @return the endLine
	 */
	public int getEndLine() {
		return endLine;
	}
	
	public int getLength(){
		return getEndLine() - getStartLine() + 1;
	}

	/**
	 * @param endLine the endLine to set
	 */
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}
	
	/**
	 * @return the tokenList
	 */
	public ArrayList<Token> getTokenList() {
		return tokenList;
	}

	/**
	 * @param tokenList the tokenList to set
	 */
	public void setTokenList(ArrayList<Token> tokenList) {
		this.tokenList = tokenList;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String content) {
		this.fileContent = content;
	}
}
