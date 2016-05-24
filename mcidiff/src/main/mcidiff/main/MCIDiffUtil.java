package mcidiff.main;

import java.util.ArrayList;
import java.util.Iterator;

import mcidiff.model.CloneInstance;
import mcidiff.model.CloneSet;
import mcidiff.model.Multiset;
import mcidiff.model.TokenMultiset;

public class MCIDiffUtil {
	
	public static TokenSequence[] transferToModel(CloneSet set){
		TokenSequence[] sequence = new TokenSequence[set.getInstances().size()];
		for(int i=0; i<sequence.length; i++){
			CloneInstance instance = set.getInstances().get(i);
			sequence[i] = new TokenSequence(instance);
		}
		return sequence;
	}
	
	public static void filterCommonSet(ArrayList<? extends Multiset> results){
		Iterator<? extends Multiset> iterator = results.iterator();
		while(iterator.hasNext()){
			Multiset obj = iterator.next();
			if(obj instanceof TokenMultiset){
				TokenMultiset set = (TokenMultiset)obj;
				if(set.isCommon()){
					iterator.remove();
				}
			}
		}
	}
}
