package mcidiff.comparator;

import java.util.ArrayList;
import java.util.Comparator;

import mcidiff.model.CloneInstance;
import mcidiff.model.SeqMultiset;
import mcidiff.model.TokenSeq;

public class SeqMultisetPositionComparator implements Comparator<SeqMultiset> {
	private ArrayList<SeqMultiset> list;
	
	public SeqMultisetPositionComparator(ArrayList<SeqMultiset> list){
		this.list = list;
	}
	
	@Override
	public int compare(SeqMultiset set1, SeqMultiset set2) {
		if(set1 != null && set2 != null){
			
			boolean isComparable = false;
			
			int sum = 0;
			for(TokenSeq seq1: set1.getSequences()){
				CloneInstance instance = seq1.getCloneInstance();
				TokenSeq seq2 = set2.findTokenSeqByCloneInstance(instance);
				
				if(!seq2.isEpisolonTokenSeq() && !seq1.isEpisolonTokenSeq()){
					isComparable = true;
					sum += seq1.getStartPosition() - seq2.getStartPosition();;							
				}
			}
			
			if(!isComparable){
				for(SeqMultiset set: list){
					if(isComparable(set, set1) && isComparable(set, set2)){
						int value1 = compareValidatedSet(set, set1);
						int value2 = compareValidatedSet(set, set2);
						
						if(value1*value2 < 0){
							if(value1 > 0){
								return -1;
							}
							else{
								return 1;
							}
						}
					}
				}
				
				//System.out.println(set1.toString() + "is not comparable with " + set2.toString());
			}
			
			return sum;
		}
		
		return 0;
	}

	private int compareValidatedSet(SeqMultiset set1, SeqMultiset set2){
		int sum = 0;
		for(TokenSeq seq1: set1.getSequences()){
			CloneInstance instance = seq1.getCloneInstance();
			TokenSeq seq2 = set2.findTokenSeqByCloneInstance(instance);
			
			if(!seq2.isEpisolonTokenSeq() && !seq1.isEpisolonTokenSeq()){
				sum += seq1.getStartPosition() - seq2.getStartPosition();						
			}
		}
		
		return sum;
	}
	
	private boolean isComparable(SeqMultiset set1, SeqMultiset set2){
		for(TokenSeq seq1: set1.getSequences()){
			CloneInstance instance = seq1.getCloneInstance();
			TokenSeq seq2 = set2.findTokenSeqByCloneInstance(instance);
			
			if(!seq2.isEpisolonTokenSeq() && !seq1.isEpisolonTokenSeq()){
				return true;
			}
		}
		
		return false;
	}
}
