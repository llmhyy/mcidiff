package mcidiff.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;

public class FastASTNodeComparator {
	public static double computeNodeSim(ASTNode node1, ASTNode node2){
		if(node1 == null || node2 == null){
			return 0;
		}
		
		//Character[] str1 = transferChar(node1.toString());
		//Character[] str2 = transferChar(node2.toString());
		
		ArrayList<String> str1 = ASTUtil.parseTokens(node1.toString());
		ArrayList<String> str2 = ASTUtil.parseTokens(node2.toString());
		
		//double length = DiffUtil.buildLeveshteinTable(str1, str2, new DefaultComparator())[str1.length][str2.length];
		//return 2*length/(str1.length+str2.length);
		
		double sim = getSim(str1, str2);
		return sim;
	}
	
	
	
	private static double getSim(ArrayList<String> str1, ArrayList<String> str2) {
		HashSet<String> set = new HashSet<>();
		HashMap<String, Integer> map1 = new HashMap<>(); 
		HashMap<String, Integer> map2 = new HashMap<>(); 
		
		for(String token: str1){
			set.add(token);
			Integer count = map1.get(token);
			if(count == null){
				count = 1;
			}
			else{
				count++;
			}
			map1.put(token, count);
		}
		for(String token: str2){
			set.add(token);
			Integer count = map2.get(token);
			if(count == null){
				count = 1;
			}
			else{
				count++;
			}
			map2.put(token, count);
		}
		
		double numberator = 0;
		double delimeter1 = 0;
		double delimeter2 = 0;
		
		double common = 0;
		double total = 0;
		
		for(String key: set){
			Integer count1 = map1.get(key);
			count1 = (count1==null)? 0 : count1;
			
			Integer count2 = map2.get(key);
			count2 = (count2==null)? 0 : count2;
			
			//double count = count1 + count2;
			//double ratio1 = count1/count;
			//double ratio2 = count2/count;
			
			numberator += count1*count2;
			delimeter1 += count1*count1;
			delimeter2 += count2*count2;
			
			if(count1 > 0 && count2 > 0){
				common++;
			}
			total++;
		}
		
		double consine = numberator / (Math.sqrt(delimeter1)*Math.sqrt(delimeter2));
		double commonality = common/total;
		
		return consine * commonality;
	}

	public static Character[] transferChar(String string){
		char[] chars = string.toCharArray();
		Character[] charactors = new Character[chars.length];
		for(int i=0; i<chars.length; i++){
			charactors[i] = new Character(chars[i]);
		}
		
		return charactors;
	}
}
