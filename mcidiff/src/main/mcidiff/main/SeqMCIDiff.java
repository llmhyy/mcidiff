package mcidiff.main;

import java.util.ArrayList;

import mcidiff.action.Tokenizer;
import mcidiff.comparator.SeqMultisetPositionComparator;
import mcidiff.model.CloneInstance;
import mcidiff.model.CloneSet;
import mcidiff.model.CorrespondentListAndSet;
import mcidiff.model.Multiset;
import mcidiff.model.SeqMultiset;
import mcidiff.model.Token;
import mcidiff.model.TokenMultiset;
import mcidiff.model.TokenSeq;
import mcidiff.util.ASTUtil;
import mcidiff.util.DiffUtil;
import mcidiff.util.MCIDiffGlobalSettings;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

public class SeqMCIDiff{
	
	public ArrayList<SeqMultiset> diff(CloneSet set, IJavaProject project) throws Exception{
		
		new Tokenizer().tokenize(set, project);
		
		ArrayList<Token>[] lists = set.getTokenLists();
		
		long t1 = System.currentTimeMillis();
		CorrespondentListAndSet cls = DiffUtil.generateMatchedTokenListFromMultiSequence(lists);
		long t2 = System.currentTimeMillis();
		System.out.println("Time for optimal subsequence generation: " + (t2-t1));
		
		TokenSequence[] sequences = MCIDiffUtil.transferToModel(set);
		ArrayList<Multiset> results = computeDiff(cls, sequences);
		
		results = mergeDiffRanges(results);
		
		identifyEpsilonTokenPosition(results);
		
		MCIDiffUtil.filterCommonSet(results);
		
		ArrayList<SeqMultiset> seqResults = transferSeqMultiset(results);
		computeText(seqResults);
		
		System.currentTimeMillis();
		
		return seqResults;
	}
	
	private ArrayList<SeqMultiset> transferSeqMultiset(ArrayList<Multiset> results){
		ArrayList<SeqMultiset> sets = new ArrayList<>();
		
		for(Multiset multiset: results){
			SeqMultiset set = (SeqMultiset)multiset;
			sets.add(set);
		}
		
		return sets;
	}

	/**
	 * This method is used to merge some common tokens into diff range.
	 * @param results
	 * @return
	 */
	private ArrayList<Multiset> mergeDiffRanges(ArrayList<Multiset> results) {
		
		int preIndex = findSeqMultisetByOrder(results, -1);
		int postIndex = findSeqMultisetByOrder(results, preIndex);
		
		if(preIndex != -1 && postIndex != -1){
			while(postIndex != -1 && preIndex != -1){
				if(postIndex-preIndex == MCIDiffGlobalSettings.tokenGapForMergeDiffRange+1){
					ArrayList<Multiset> candidateMultisetList = new ArrayList<>();
					for(int i=preIndex; i<=postIndex; i++){
						candidateMultisetList.add(results.get(i));
					}
					
					SeqMultiset seqMultiset = tryMergeMultipleMultisets(candidateMultisetList);
					
					if(seqMultiset != null){
						//adjust the array list to merge
						results.set(preIndex, seqMultiset);
						int offset = MCIDiffGlobalSettings.tokenGapForMergeDiffRange + 1;
						for(int i=preIndex+1; i<results.size()-offset; i++){
							results.set(i, results.get(i+offset));
						}
						
						for(int k=0; k<offset; k++){
							results.remove(results.size()-1-k);
						}
						
						preIndex = findSeqMultisetByOrder(results, preIndex);
						postIndex = findSeqMultisetByOrder(results, preIndex);
					}
					else{
						preIndex = findSeqMultisetByOrder(results, preIndex);
						postIndex = findSeqMultisetByOrder(results, preIndex);
					}
				}
				else{
					preIndex = postIndex;
					postIndex = findSeqMultisetByOrder(results, postIndex);
				}
			}
		}
		
		return results;
	}
	
	/**
	 * if the preSet and postSet are separate syntactic unit, they should not be merged for
	 * more flexible recommendation.
	 * @param preSet
	 * @param postSet
	 * @return
	 */
	private boolean isSeparateSyntacticUnit(SeqMultiset preSet, SeqMultiset postSet){
		for(TokenSeq seq: preSet.getSequences()){
			if(!seq.isCompeleteSyntaxUnit()){
				return false;
			}
		}
		
		for(TokenSeq seq: postSet.getSequences()){
			if(!seq.isCompeleteSyntaxUnit()){
				return false;
			}
		}
		
		return true;
	}

	/**
	 * precondition: {@code candidateMultisetList} is in correct order.
	 * 
	 * try merge the candidate multiset list, if it is not syntax complete, return null. Otherwise,
	 * this method returns a merged sequence multiset.
	 * 
	 * @param candidateMultiset
	 * @return
	 */
	private SeqMultiset tryMergeMultipleMultisets(ArrayList<Multiset> candidateMultisetList) {
		
		SeqMultiset preSet = (SeqMultiset) candidateMultisetList.get(0);
		SeqMultiset postSet = (SeqMultiset) candidateMultisetList.get(candidateMultisetList.size()-1);
		boolean flag = isSeparateSyntacticUnit(preSet, postSet);
		if(flag){
			return null;
		}
		
		SeqMultiset newSeqMultiset = new SeqMultiset();
		
		SeqMultiset set = (SeqMultiset) candidateMultisetList.get(0);
		for(TokenSeq seq0: set.getSequences()){
			CloneInstance instance = seq0.getCloneInstance();
			
			TokenSeq newSeq = new TokenSeq();
			for(Multiset s: candidateMultisetList){
				if(s instanceof TokenMultiset){
					TokenMultiset tm = (TokenMultiset)s;
					Token t = tm.findToken(instance);
					if(!t.isEpisolon()){
						newSeq.addToken(t);
					}
				}
				else if(s instanceof SeqMultiset){
					SeqMultiset seqSet = (SeqMultiset)s;
					TokenSeq seq = seqSet.findTokenSeqByCloneInstance(instance);
					if(!seq.isEpisolonTokenSeq()){
						for(Token t: seq.getTokens()){
							newSeq.addToken(t);
						}
					}
				}
			}
			
			if(newSeq.getTokens().size()==0){
				System.currentTimeMillis();
			}
			
			newSeqMultiset.addTokenSeq(newSeq);
		}
		
		if(isSyntaxComplete(newSeqMultiset)){
			return newSeqMultiset;
		}
		
		return null;
	}

	private boolean isSyntaxComplete(SeqMultiset newSeqMultiset) {
		for(TokenSeq seq: newSeqMultiset.getSequences()){
			if(!seq.isSyntaxComplete()){
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Find the first sequence multiset appearing after the {@code index}th multiset.  
	 * The {@code index}th multiset is not included.
	 * 
	 * If not found, return -1.
	 * 
	 * @param results
	 * @param index
	 * @return
	 */
	private int findSeqMultisetByOrder(ArrayList<Multiset> results,
			int index) {
		for(int i=index+1; i<results.size(); i++){
			Multiset set = results.get(i);
			if(set instanceof SeqMultiset){
				return i;
			}
		}
		return -1;
	}

	/**
	 * This method returns an ordered list of token-multiset and token-seq-multiset.
	 * For example, it returns [{int, int, int}, {a, a, a}, {=, =, =}, {b1+c1, b2+c2, b3-c3},
	 * {;, ;, ;}].
	 * 
	 * @param cls
	 * @param sequences
	 * @return
	 */
	public ArrayList<Multiset> computeDiff(CorrespondentListAndSet cls, TokenSequence[] sequences) {
		ArrayList<Multiset> seqMultisetList = new ArrayList<>();
		
		ArrayList<TokenMultiset> tokenMultisets = new TokenMCIDiff().computeDiff(cls, sequences);
		for(TokenSequence sequence: sequences){
			sequence.setStartIndex(0);
			sequence.setCursorIndex(0);
		}
		
		for(int i=1; i<cls.getCommonTokenList().length; i++){
			
			TokenMultiset commonSet = cls.getMultisetList()[i];
			commonSet.setCommon(true);
			for(int j=0; j<sequences.length; j++){
				CloneInstance instance = sequences[j].getCloneInstance();
				Token cToken = commonSet.findToken(instance);
				sequences[j].moveEndCursorTo(cToken);
			}
			
			SeqMultiset seqMultiset = generateSeqMultiset(sequences);
			if(!seqMultiset.isAllEmpty()){
				ArrayList<SeqMultiset> splitedMultisets = splitDiffRanges(seqMultiset, tokenMultisets);
				seqMultisetList.addAll(splitedMultisets);
				System.currentTimeMillis();
				//seqMultisetList.add(seqMultiset);
			}
			
			seqMultisetList.add(commonSet);
			
			for(int j=0; j<sequences.length; j++){
				sequences[j].moveStartCursorToEndCursor();
			}
		}
		
		return seqMultisetList;
	}
	
	public class CompleteSyntacticUnitVisitor extends ASTVisitor{
		private ArrayList<ASTNode> completeSyntacticUnits = new ArrayList<>();
		private TokenSeq seq;
		
		public CompleteSyntacticUnitVisitor(TokenSeq seq){
			this.seq = seq;
		}
		
		@Override
		public void preVisit(ASTNode node){
			
			if(!(node instanceof Statement || node instanceof BodyDeclaration)){
				return;
			}
			
			int startPosition = this.seq.getStartPosition();
			int endPosition = this.seq.getEndPosition();
			/**
			 * the node is inside the diff range
			 */
			if(node.getStartPosition() >= startPosition && node.getStartPosition()+node.getLength() <= endPosition){
				int coveredTokenNumber = findCoveredToken(this.seq, node).size();
				/**
				 * the pre-order traverse of AST will ensure that the parent node will be
				 * visited before child node.
				 */
				if(isNotCoveredByExistingUnits(node) && coveredTokenNumber>1){
					this.completeSyntacticUnits.add(node);
				}
			}
		}

		private boolean isNotCoveredByExistingUnits(ASTNode node) {
			for(ASTNode existingNode: this.completeSyntacticUnits){
				int existingStart = existingNode.getStartPosition();
				int existingEnd = existingStart + existingNode.getLength();
				
				int start = node.getStartPosition();
				int end = start + node.getLength();
				
				if(existingStart <= start && existingEnd >= end){
					return false;
				}
			}
			
			return true;
		}

		/**
		 * @return the completeSyntacticUnits
		 */
		public ArrayList<ASTNode> getCompleteSyntacticUnits() {
			return completeSyntacticUnits;
		}
		
	}

	private ArrayList<Token> findCoveredToken(TokenSeq seq, ASTNode node) {
		ArrayList<Token> coveredTokenList = new ArrayList<>();
		int start = node.getStartPosition();
		int end = start + node.getLength();
		for(Token token: seq.getTokens()){
			if(start <= token.getStartPosition() && end >= token.getEndPosition()){
				coveredTokenList.add(token);
			}
		}
		return coveredTokenList;
	}
	
	/**
	 * This method is used to split some diff range so that there will be no
	 * mixture of complete and incomplete ASTs in a single differences.
	 * 
	 * For example, a diff range could be "print(); int a", the split process
	 * will make this diff range into "print()" and "int a". In this example,
	 * the mixture of a complete AST and an incomplete AST is distinguished.
	 * @param seqMultiset
	 * @return
	 */
	private ArrayList<SeqMultiset> splitDiffRanges(SeqMultiset seqMultiset, ArrayList<TokenMultiset> tokenMultisets) {
		ArrayList<ArrayList<TokenSeq>> lists = new ArrayList<>();
		for(TokenSeq seq: seqMultiset.getSequences()){
			ArrayList<TokenSeq> list = new ArrayList<>();
			if(!seq.isEpisolonTokenSeq()){
				if(seq.isSingleToken()){
					list.add(seq);
				}
				else{
					CompilationUnit cu = (CompilationUnit) seq.getTokens().get(0).getNode().getRoot();
					CompleteSyntacticUnitVisitor csVisitor = new CompleteSyntacticUnitVisitor(seq);
					
					cu.accept(csVisitor);
					ArrayList<ASTNode> completeUnit = csVisitor.getCompleteSyntacticUnits();
					
					if(completeUnit.size() == 0){
						ArrayList<TokenSeq> seqList = trySplitingByDelimiter(seq);
						
						for(TokenSeq s: seqList){
							if(s.getTokens().size()==0){
								System.currentTimeMillis();
							}
						}
						
						list.addAll(seqList);
					}
					else{
						ArrayList<TokenSeq> seqList = generateSeparateRanges(seq, completeUnit);
						
						for(TokenSeq s: seqList){
							if(s.getTokens().size()==0){
								System.currentTimeMillis();
								seqList = generateSeparateRanges(seq, completeUnit);
							}
						}
						
						list.addAll(seqList);
					}
				}
			}
			//empty candidate range
			else{
				list.add(seq);
			}
			
			lists.add(list);
		}
		
		
		ArrayList<SeqMultiset> multisets = matchRanges(lists, tokenMultisets);
		
		return multisets;
	}
	
	private ArrayList<TokenSeq> trySplitingByDelimiter(TokenSeq seq) {
		int splitIndex = -1;
		for(int i=0; i<seq.size(); i++){
			Token t = seq.getTokens().get(i);
			if(t.getTokenName().equals(";")){
				splitIndex = i;
			}
		}
		
		ArrayList<TokenSeq> list = new ArrayList<TokenSeq>();
		if(splitIndex == -1){
			list.add(seq);
		}
		else{
			TokenSeq preSeq = new TokenSeq();
			for(int i=0; i<=splitIndex; i++){
				preSeq.addToken(seq.getTokens().get(i));
			}
			if(preSeq.size() != 0){
				list.add(preSeq);				
			}
			
			TokenSeq postSeq = new TokenSeq();
			for(int i=splitIndex+1; i<seq.size(); i++){
				postSeq.addToken(seq.getTokens().get(i));
			}
			if(postSeq.size() != 0){
				list.add(postSeq);				
			}
		}
		
		return list;
	}

	private void computeText(ArrayList<SeqMultiset> multisets){
		for(SeqMultiset set: multisets){
			for(TokenSeq seq: set.getSequences()){
				seq.retrieveTextFromDoc();
			}
		}
	}

	private ArrayList<SeqMultiset> matchRanges(ArrayList<ArrayList<TokenSeq>> lists,
			ArrayList<TokenMultiset> tokenMultisets) {
		
		System.currentTimeMillis();
		
		ArrayList<SeqMultiset> seqMultisetList = new ArrayList<>();
		
		for(ArrayList<TokenSeq> seqList: lists){
			ArrayList<ArrayList<TokenSeq>> otherSeqLists = findOtherLists(lists, seqList);
			for(TokenSeq seq: seqList){
				
				if(!seq.isMarked()){
					SeqMultiset seqMultiset = new SeqMultiset();
					seqMultiset.addTokenSeq(seq);
					seq.setMarked(true);
					
					for(ArrayList<TokenSeq> otherSeqList: otherSeqLists){
						CloneInstance cloneInstance = otherSeqList.get(0).getCloneInstance();
						TokenSeq otherSeq = identifyMostSimilarSeq(otherSeqList, seq, tokenMultisets);
						if(otherSeq != null){
							seqMultiset.addTokenSeq(otherSeq);
							if(!isConformToValidatedOrder(seqMultiset, seqMultisetList)){
								seqMultiset.getSequences().remove(otherSeq);
								
								TokenSeq episolonTokenSeq = TokenSeq.createEpisolonTokenSeq(cloneInstance);
								seqMultiset.addTokenSeq(episolonTokenSeq);
							}
							else{
								otherSeq.setMarked(true);								
							}
						}
						else{
							TokenSeq episolonTokenSeq = TokenSeq.createEpisolonTokenSeq(cloneInstance);
							seqMultiset.addTokenSeq(episolonTokenSeq);
						}
					}	
					
					if(!seqMultiset.isAllEmpty()){
						seqMultisetList.add(seqMultiset);						
					}
					
				}
			}
		}
		
		SeqMultisetPositionComparator comparator = new SeqMultisetPositionComparator(seqMultisetList);
		ASTUtil.sort(seqMultisetList, comparator);
		
		return seqMultisetList;
	}

	/**
	 * To ensure that the matched token sequences will not be crossed corresponded. For example, one list is
	 * {"seq1", "seq2"} and the other one is {"seq1'", "seq2'"}, the matched results should never be ["seq1", "seq2'"]
	 * and ["seq2", "seq1'"].
	 * 
	 * @param partialSet
	 * @param seqMultisetList
	 * @return
	 */
	private boolean isConformToValidatedOrder(SeqMultiset partialSet,
			ArrayList<SeqMultiset> seqMultisetList) {
		for(SeqMultiset set: seqMultisetList){
			int result = 0;
			
			for(TokenSeq seq: partialSet.getSequences()){
				TokenSeq corresSeq = set.findTokenSeqByCloneInstance(seq.getCloneInstance());
				
				if(!seq.isEpisolonTokenSeq() && !corresSeq.isEpisolonTokenSeq()){
					int diff = seq.getStartPosition() - corresSeq.getStartPosition();
					if(result == 0){
						result += diff;
					}
					else if(result < 0){
						int r = result + diff;
						if(r > result){
							return false;
						}
						else{
							result = r;
						}
					}
					else if(result > 0){
						int r = result + diff;
						if(r < result){
							return false;
						}
						else{
							result = r;
						}
					}
				}
			}
		}
		
		return true;
	}

	private TokenSeq identifyMostSimilarSeq(ArrayList<TokenSeq> otherSeqList,
			TokenSeq seq, ArrayList<TokenMultiset> tokenMultisets) {
				
		double bestSim = 0;
		TokenSeq bestSeq = null;
		
		for(TokenSeq otherSeq: otherSeqList){
			if(!otherSeq.isMarked()){
				
				double sim = computeTokenSeqSimilarity(seq, otherSeq, tokenMultisets);
				if(sim > 0){
					if(bestSeq == null){
						bestSeq = otherSeq;
						bestSim = sim;
					}
					else{
						if(sim > bestSim){
							bestSeq = otherSeq;
							bestSim = sim;
						}
					}
				}
			}
		}
		
		return bestSeq;
	}

	private double computeTokenSeqSimilarity(TokenSeq seq, TokenSeq otherSeq,
			ArrayList<TokenMultiset> tokenMultisets) {
		if(seq.isEpisolonTokenSeq() && otherSeq.isEpisolonTokenSeq()){
			return 1;
		}
		else if(!seq.isEpisolonTokenSeq() && !otherSeq.isEpisolonTokenSeq()){
			int commonTokenNum = 0;
			for(Token t: seq.getTokens()){
				Token corresToken = findCorrespondenceToken(tokenMultisets, t, otherSeq.getCloneInstance());
				if(otherSeq.getTokens().contains(corresToken)){
					commonTokenNum++;
				}
			}
			
			return ((double)commonTokenNum)*2/(seq.getTokens().size()+otherSeq.getTokens().size());
		}
		else{
			return 0;			
		}
		
	}

	private Token findCorrespondenceToken(
			ArrayList<TokenMultiset> tokenMultisets, Token t, CloneInstance cloneInstance) {
		for(TokenMultiset set: tokenMultisets){
			if(isTokenMultisetContainASpecificToken(set, t)){
				return set.findToken(cloneInstance);
			}
		}
		return null;
	}
	
	private boolean isTokenMultisetContainASpecificToken(TokenMultiset set, Token t){
		for(Token token: set.getTokens()){
			if(token.getCloneInstance().equals(t.getCloneInstance()) &&
					token.getStartPosition() == t.getStartPosition() &&
					token.getEndPosition() == t.getEndPosition()){
				return true;
			}
		}
		
		return false;
	}

	private ArrayList<ArrayList<TokenSeq>> findOtherLists(
			ArrayList<ArrayList<TokenSeq>> lists, ArrayList<TokenSeq> seqList) {
		ArrayList<ArrayList<TokenSeq>> otherLists = new ArrayList<>();
		for(ArrayList<TokenSeq> list: lists){
			if(list != seqList){
				otherLists.add(list);
			}
		}
		return otherLists;
	}

	/**
	 * precondition: the {@code completeUnit} is not empty.
	 * 
	 * Generate the separated ranges given a complete syntax unit which is fully contained in {@code seq}.
	 * For example, a token sequence like "b; int f = 0; double", the token sequence will be divided into
	 * "b;", "int f = 0;" and "double".
	 * 
	 * The method will divide the token sequence into at least 1 sequence and at most three sequences.
	 * 
	 * @param seq
	 * @param completeUnit
	 * @return
	 */
	private ArrayList<TokenSeq> generateSeparateRanges(TokenSeq seq, ArrayList<ASTNode> completeUnit) {
		ArrayList<TokenSeq> seqList = new ArrayList<>();
		
		int firstPosition = findFirstTokenPosition(completeUnit);
		int secondPosition = findLastTokenPosition(completeUnit);
		
		if(firstPosition > secondPosition){
			System.err.println("the first position is larger than the second position when splitting a range");
		}
		
		TokenSeq preTokenSeq = new TokenSeq();
		TokenSeq midTokenSeq = new TokenSeq();
		TokenSeq postTokenSeq = new TokenSeq();
		
		for(Token token: seq.getTokens()){
			if(token.getStartPosition() < firstPosition){
				preTokenSeq.addToken(token);
			}
			else if(token.getStartPosition() > secondPosition){
				postTokenSeq.addToken(token);
			}
			else{
				midTokenSeq.addToken(token);
			}
		}
		
		if(preTokenSeq.getTokens().isEmpty()){
			preTokenSeq = TokenSeq.createEpisolonTokenSeq(seq.getCloneInstance());
		}
		if(midTokenSeq.getTokens().isEmpty()){
			midTokenSeq = TokenSeq.createEpisolonTokenSeq(seq.getCloneInstance());
		}
		if(postTokenSeq.getTokens().isEmpty()){
			postTokenSeq = TokenSeq.createEpisolonTokenSeq(seq.getCloneInstance());
		}
		
		seqList.add(0, preTokenSeq);
		seqList.add(1, midTokenSeq);
		seqList.add(2, postTokenSeq);
		
		return seqList;
	}

	/**
	 * precondition: the {@code completeUnit} is not empty.
	 * 
	 * return the closest token before complete syntax unit
	 * 
	 * @param seq
	 * @param completeUnit
	 * @return
	 */
	private TokenSeq findPrepartialTokenSeq(TokenSeq seq, ArrayList<ASTNode> completeUnit) {
		int leastPosition = findFirstTokenPosition(completeUnit);
		
		TokenSeq preSeq = new TokenSeq();
		
		for(Token token: seq.getTokens()){
			if(token.getStartPosition() < leastPosition){
				preSeq.addToken(token);
			}
		}
		
		if(seq.getTokens().isEmpty()){
			preSeq = TokenSeq.createEpisolonTokenSeq(seq.getCloneInstance());
		}
		
		return preSeq;
	}
	
	private int findFirstTokenPosition(ArrayList<ASTNode> completeUnit){
		int leastPosition = -1;
		for(ASTNode node: completeUnit){
			if(leastPosition == -1){
				leastPosition = node.getStartPosition();
			}
			else{
				if(leastPosition > node.getStartPosition()){
					leastPosition = node.getStartPosition();
				}
			}
		}
		
		return leastPosition;
	}
	
	/**
	 * precondition: the {@code completeUnit} is not empty.
	 * 
	 * return the closest token after complete syntax unit
	 * 
	 * @param seq
	 * @param completeUnit
	 * @return
	 */
	private TokenSeq findPostpartialToken(TokenSeq seq, ArrayList<ASTNode> completeUnit) {
		int lastPosition = findLastTokenPosition(completeUnit);
		
		Token postpartialToken = null;
		int leastInterval = -1;
		
		TokenSeq postSeq = new TokenSeq();
		for(Token token: seq.getTokens()){
			if(token.getEndPosition() > lastPosition){
				postSeq.addToken(token);
			}
		}
		
		if(seq.getTokens().isEmpty()){
			postSeq = TokenSeq.createEpisolonTokenSeq(seq.getCloneInstance());
		}
		
		return postSeq;
	}
	
	private int findLastTokenPosition(ArrayList<ASTNode> completeUnit){
		int lastIndex = -1;
		for(ASTNode node: completeUnit){
			if(lastIndex == -1){
				lastIndex = node.getStartPosition() + node.getLength();
			}
			else{
				if(lastIndex < node.getStartPosition() + node.getLength()){
					lastIndex = node.getStartPosition() + node.getLength();
				}
			}
		}
		
		return lastIndex;
	}

	private SeqMultiset generateSeqMultiset(TokenSequence[] sequences) {
		SeqMultiset seqMultiset = new SeqMultiset();
		for(TokenSequence seq: sequences){
			TokenSeq tokenSeq = new TokenSeq();
			for(int i=seq.getStartIndex()+1; i<=seq.getEndIndex()-1; i++){
				Token token = seq.get(i);
				tokenSeq.addToken(token);
			}
			
			if(tokenSeq.size() == 0){
				Token t = new Token(Token.episolonSymbol, null, seq.getCloneInstance(), -1, -1);
				tokenSeq.addToken(t);
			}
			
			tokenSeq.retrieveTextFromDoc();
			seqMultiset.addTokenSeq(tokenSeq);
		}
		
		return seqMultiset;
	}
	
	private void identifyEpsilonTokenPosition(ArrayList<? extends Multiset> results){
		for(int i=0; i<results.size(); i++){
			Object obj = results.get(i);
			if(obj instanceof SeqMultiset){
				SeqMultiset set = (SeqMultiset)obj;
				for(TokenSeq seq: set.getSequences()){
					if(seq.isEpisolonTokenSeq()){
						Token t = seq.getTokens().get(0);
						
						if(i != 0){
							Token prevToken = findPreviousNonEpisolonToken(i, results, t);
							if(null != prevToken){
								t.setPreviousToken(prevToken);								
							}
						}
						
						if(i != results.size()-1){
							Token postToken = findPostNonEpisolonToken(i, results, t);
							if(null != postToken){
								t.setPostToken(postToken);
								
								t.setStartPosition(postToken.getStartPosition());
								t.setEndPosition(postToken.getStartPosition());	
							}
						}
					}
					
				}
			}
		}
	}

	private Token findPostNonEpisolonToken(int index,
			ArrayList<? extends Multiset> results, Token episolonToken) {
		int cursor = index+1;
		while(cursor < results.size()){
			Multiset postSet = results.get(cursor);
			if(postSet instanceof TokenMultiset){
				Token postToken = ((TokenMultiset)postSet).findToken(episolonToken.getCloneInstance());
				if(!postToken.isEpisolon()){
					return postToken;
				}
				cursor++;				
			}
			else if(postSet instanceof SeqMultiset){
				TokenSeq postSeq = ((SeqMultiset)postSet).findTokenSeqByCloneInstance(episolonToken.getCloneInstance());
				if(!postSeq.isEpisolonTokenSeq()){
					return postSeq.getTokens().get(0);
				}
				cursor++;	
			}
		}
		
		return null;
	}

	private Token findPreviousNonEpisolonToken(int index,
			ArrayList<? extends Multiset> results, Token episolonToken) {
		int cursor = index-1;
		while(cursor >= 0){
			Multiset prevSet = results.get(cursor);
			if(prevSet instanceof TokenMultiset){
				Token previousToken = ((TokenMultiset)prevSet).findToken(episolonToken.getCloneInstance());
				if(!previousToken.isEpisolon()){
					return previousToken;
				}
				cursor--;				
			}
			else if(prevSet instanceof SeqMultiset){
				TokenSeq previousSeq = ((SeqMultiset)prevSet).findTokenSeqByCloneInstance(episolonToken.getCloneInstance());
				if(!previousSeq.isEpisolonTokenSeq()){
					int size = previousSeq.getTokens().size();
					return previousSeq.getTokens().get(size-1);
				}
				cursor--;	
			}
			
		}
		
		return null;
	}
}
