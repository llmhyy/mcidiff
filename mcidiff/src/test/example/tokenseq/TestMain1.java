package example.tokenseq;

import java.util.ArrayList;

import mcidiff.main.SeqMCIDiff;
import mcidiff.model.CloneInstance;
import mcidiff.model.CloneSet;
import mcidiff.model.SeqMultiset;

public class TestMain1 {
	
	public static void main(String[] ars){
		
		String path1 = "test/test1.java";
		String path2 = "test/test2.java";
		String path3 = "test/test3.java";
		
		CloneInstance instance1 = new CloneInstance(path1, 18, 23);
		CloneInstance instance2 = new CloneInstance(path2, 18, 23);
		CloneInstance instance3 = new CloneInstance(path3, 18, 23);
		
		CloneSet set = new CloneSet("0");
		set.addInstance(instance1);
		set.addInstance(instance2);
		set.addInstance(instance3);
		
		try {
			ArrayList<SeqMultiset> multisets = new SeqMCIDiff().diff(set, null);
			System.out.println(multisets);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
