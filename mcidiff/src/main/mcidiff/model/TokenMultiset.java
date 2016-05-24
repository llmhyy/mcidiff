package mcidiff.model;

import java.util.ArrayList;

public class TokenMultiset extends Multiset{
	
	private ArrayList<Token> tokens = new ArrayList<>();
	private boolean isCommon = false;
	
	/**
	 * @return the isCommon
	 */
	public boolean isCommon() {
		return isCommon;
	}

	/**
	 * @param isCommon the isCommon to set
	 */
	public void setCommon(boolean isCommon) {
		this.isCommon = isCommon;
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
	
	public void add(Token token){
		this.tokens.add(token);
	}
	
	public void addAll(TokenMultiset set){
		this.tokens.addAll(set.getTokens());
	}
	
	public String toString(){
		return tokens.toString();
	}
	
	public ArrayList<Token> findOtherTokens(Token token){
		ArrayList<Token> otherList = new ArrayList<>();
		for(Token t: tokens){
			if(t != token){
				otherList.add(t);
			}
		}
		return otherList;
	}
	
	public Token findToken(CloneInstance instance){
		for(Token token: getTokens()){
			if(token.getCloneInstance().equals(instance)){
				return token;
			}
		}
		
		return null;
	}
	
	public boolean isParamertized(){
		for(int i=0; i<getTokens().size(); i++){
			Token token1 = getTokens().get(i);
			if(!token1.isEpisolon()){
				for(int j=i+1; j<getTokens().size(); j++){
					Token token2 = getTokens().get(j);
					if(!token2.isEpisolon()){
						if(!token1.equals(token2)){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean isGapped(){
		for(Token token: getTokens()){
			if(token.isEpisolon()){
				return true;
			}
		}
		return false;
	}
	
	public boolean isPartiallySame(){
		for(int i=0; i<getTokens().size(); i++){
			Token token1 = getTokens().get(i);
			if(!token1.isEpisolon()){
				for(int j=i+1; j<getTokens().size(); j++){
					Token token2 = getTokens().get(j);
					if(!token2.isEpisolon()){
						if(token1.equals(token2)){
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
}
