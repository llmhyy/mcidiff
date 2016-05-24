package mcidiff.main;

import java.util.ArrayList;

import mcidiff.action.Tokenizer;
import mcidiff.comparator.TokenMultisetPositionComparator;
import mcidiff.model.CloneInstance;
import mcidiff.model.CloneSet;
import mcidiff.model.CorrespondentListAndSet;
import mcidiff.model.SeqMultiset;
import mcidiff.model.Token;
import mcidiff.model.TokenMultiset;
import mcidiff.model.TokenSeq;
import mcidiff.util.ASTUtil;
import mcidiff.util.DiffUtil;
import mcidiff.util.MCIDiffGlobalSettings;

import org.eclipse.jdt.core.IJavaProject;

public class TokenMCIDiff{
	/**
	 * Given a clone set, this method return a list of multiset representing the differences across
	 * the clone instances of this clone set.
	 * @param set
	 * @return
	 */
	public ArrayList<TokenMultiset> diff(CloneSet set, IJavaProject project){
		new Tokenizer().tokenize(set, project);
		
		ArrayList<Token>[] lists = set.getTokenLists();
		CorrespondentListAndSet cls = DiffUtil.generateMatchedTokenListFromMultiSequence(lists);
		
		TokenSequence[] sequences = MCIDiffUtil.transferToModel(set);
		ArrayList<TokenMultiset> results = computeDiff(cls, sequences);
		
		ASTUtil.sort(results, new TokenMultisetPositionComparator(results));
		identifyEpsilonTokenPosition(results);
		
		MCIDiffUtil.filterCommonSet(results);
		
		return results;
	}
	
	/**
	 * {@code results} must be sorted for aggregation
	 * @param results
	 * @return
	 */
	@SuppressWarnings("unused")
	private ArrayList<SeqMultiset> aggregateSeperatedTokens(ArrayList<TokenMultiset> results) {

		ArrayList<ArrayList<TokenMultiset>> aggregatedSets = new ArrayList<>();
		ArrayList<TokenMultiset> sets = new ArrayList<>();
		for(int i=0; i<results.size(); i++){
			TokenMultiset set = results.get(i);
			if(sets.size() == 0){
				sets.add(set);
			}
			else{
				TokenMultiset previousSet = results.get(i-1);
				if(isConsecutive(set, previousSet)
						/*&& (set.isGapped() && !set.isParamertized())*/){
					sets.add(set);
				}
				else{
					aggregatedSets.add(sets);
					sets = new ArrayList<>();
					sets.add(set);
				}
			}
		}
		
		aggregatedSets.add(sets);
		
		ArrayList<SeqMultiset> seqMultisets = transferToSeqMultiset(aggregatedSets);
		
		return seqMultisets;
	}



	/**
	 * @param aggregatedSets
	 * @return
	 */
	private ArrayList<SeqMultiset> transferToSeqMultiset(
			ArrayList<ArrayList<TokenMultiset>> aggregatedSets) {
		ArrayList<SeqMultiset> seqMultisets = new ArrayList<>();
		for(ArrayList<TokenMultiset> setList: aggregatedSets){
			SeqMultiset seqMultiset = new SeqMultiset();
			
			TokenMultiset set = setList.get(0);
			/**
			 * for each clone instance, I build a token sequence.
			 */
			for(Token t: set.getTokens()){
				CloneInstance instance = t.getCloneInstance();
				TokenSeq sequence = new TokenSeq();
				
				for(TokenMultiset s: setList){
					Token token = s.findToken(instance);
					sequence.addToken(token);
				}
				
				sequence.retrieveTextFromDoc();
				sequence.filterUselessEpsilonToken();
				seqMultiset.addTokenSeq(sequence);
			}
			
			seqMultisets.add(seqMultiset);
		}
		return seqMultisets;
	}
	
	private boolean isConsecutive(TokenMultiset set, TokenMultiset previousSet) {
		for(Token prevToken: previousSet.getTokens()){
			Token currentCorrspondingToken = set.findToken(prevToken.getCloneInstance());
			
			/**
			 * for two consecutive episolon token
			 */
			if(prevToken.isEpisolon() && currentCorrspondingToken.isEpisolon()){
				try{
					if(!prevToken.getPreviousToken().equals(currentCorrspondingToken.getPreviousToken()) ||
							!prevToken.getPostToken().equals(currentCorrspondingToken.getPostToken())){
						return false;
					}					
				}
				catch(NullPointerException e){
					//Do nothing for now.
				}
			}
			
			else if(!(prevToken.getPostToken().equals(currentCorrspondingToken) 
					|| currentCorrspondingToken.getPreviousToken().equals(prevToken))){
				return false;
			}
		}
		return true;
	}

	
	/**
	 * results is a sorted multisets w.r.t position.
	 * @param results
	 */
	private void identifyEpsilonTokenPosition(ArrayList<TokenMultiset> results){
		for(int i=0; i<results.size(); i++){
			TokenMultiset set = results.get(i);
			for(Token token: set.getTokens()){
				if(token.isEpisolon()){
					/**
					 * set episolon's previous token
					 */
					if(i != 0){
						Token prevToken = findPreviousNonEpisolonToken(i, results, token);
						if(prevToken != null){
							token.setPreviousToken(prevToken);
							//token.setStartPosition(prevToken.getEndPosition()+3);
							//token.setEndPosition(prevToken.getEndPosition()+3);		
						}
					}
					
					/**
					 * set episolon's post token
					 */
					if(i != results.size()-1){
						Token postToken = findPostNonEpisolonToken(i, results, token);
						if(postToken != null){
							token.setPostToken(postToken);		
							token.setStartPosition(postToken.getStartPosition());
							token.setEndPosition(postToken.getStartPosition());	
						}
					}
				}
			}
		}
	}
	
	private Token findPreviousNonEpisolonToken(int index, ArrayList<TokenMultiset> results, Token episolonToken) {
		int cursor = index-1;
		while(cursor >= 0){
			TokenMultiset prevSet = results.get(cursor);
			Token previousToken = prevSet.findToken(episolonToken.getCloneInstance());
			if(!previousToken.isEpisolon()){
				return previousToken;
			}
			cursor--;
		}
		
		return null;
	}
	
	private Token findPostNonEpisolonToken(int index, ArrayList<TokenMultiset> results, Token episolonToken) {
		int cursor = index+1;
		while(cursor < results.size()){
			TokenMultiset postSet = results.get(cursor);
			Token postToken = postSet.findToken(episolonToken.getCloneInstance());
			if(!postToken.isEpisolon()){
				return postToken;
			}
			cursor++;
		}
		
		return null;
	}
	
	public ArrayList<TokenMultiset> computeDiff(CorrespondentListAndSet cls, TokenSequence[] sequences) {
		ArrayList<TokenMultiset> multisetList = new ArrayList<>();
		
		for(int i=1; i<cls.getCommonTokenList().length; i++){
			
			TokenMultiset commonSet = cls.getMultisetList()[i];
			commonSet.setCommon(true);
			for(int j=0; j<sequences.length; j++){
				CloneInstance instance = sequences[j].getCloneInstance();
				Token cToken = commonSet.findToken(instance);
				sequences[j].moveEndCursorTo(cToken);
			}
			multisetList.add(commonSet);
			
			ArrayList<TokenMultiset> partialSet = computeInDiffRange(sequences);
			if(partialSet.size() != 0){
				multisetList.addAll(partialSet);
			}
			
			for(int j=0; j<sequences.length; j++){
				sequences[j].moveStartCursorToEndCursor();
			}
		}
		
		multisetList.remove(multisetList.size()-1);
		
		return multisetList;
	}

	/**
	 * compute the differences in differential ranges. A *range* in a token sequence is between
	 * start index and cursor index - 1.
	 * 
	 * @param sequences
	 * @return
	 */
	private ArrayList<TokenMultiset> computeInDiffRange(TokenSequence[] sequences) {
		ArrayList<TokenMultiset> multisetList = new ArrayList<>();
		for(int i=0; i<sequences.length; i++){
			TokenSequence seedSequence = sequences[i];
			TokenSequence[] otherSeqs = findOtherSequences(sequences, sequences[i]);
			
			for(int j=seedSequence.getStartIndex()+1; j<=seedSequence.getEndIndex()-1; j++){
				Token seedToken = seedSequence.getTokenList().get(j);
				
				if(!seedToken.isMarked()){
					TokenMultiset multiset = new TokenMultiset();
					multiset.add(seedToken);
					seedToken.setMarked(true);
					for(int k=0; k<otherSeqs.length; k++){
						TokenSequence otherSeq = otherSeqs[k];
						Token correspondingToken = findBestMatchToken(seedToken, otherSeq);
						correspondingToken.setMarked(true);
						multiset.add(correspondingToken);
					}
					multisetList.add(multiset);
				}
			}
		}
		
		return multisetList;
	}
	
	/**
	 * Given a seed token, return the best matcher in otherSeq, if there is no such matcher, it will return
	 * a episolon token. In addition, the episolon token's clone instance should also be specified.
	 * @param seedToken
	 * @param otherSeq
	 * @return
	 */
	private Token findBestMatchToken(Token seedToken, TokenSequence otherSeq) {
		double similarity = -1;
		Token bestMatcher = null;
		
		for(int i=otherSeq.getStartIndex()+1; i<=otherSeq.getEndIndex()-1; i++){
			Token otherToken = otherSeq.getTokenList().get(i);
			if(!otherToken.isMarked()){
				double sim = otherToken.compareWith(seedToken);
				if(sim > MCIDiffGlobalSettings.tokenSimilarityThreshold){
					if(similarity == -1){
						similarity = sim;
						bestMatcher = otherToken;
					}
					else{
						if(similarity < sim){
							similarity = sim;
							bestMatcher = otherToken;
						}
					}
				}
			}
		}
		
		if(bestMatcher == null){
			bestMatcher = new Token(Token.episolonSymbol, null, otherSeq.getCloneInstance(), -1, -1);
		}
		
		return bestMatcher;
	}

	private TokenSequence[] findOtherSequences(TokenSequence[] sequences, TokenSequence sequence){
		ArrayList<TokenSequence> otherSeqList = new ArrayList<>();
		for(int i=0; i<sequences.length; i++){
			if(sequences[i] != sequence){
				otherSeqList.add(sequences[i]);
			}
		}
		
		return otherSeqList.toArray(new TokenSequence[0]);
	}
}
