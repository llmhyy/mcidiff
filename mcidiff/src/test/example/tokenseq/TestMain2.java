package example.tokenseq;

import java.util.ArrayList;

import mcidiff.main.SeqMCIDiff;
import mcidiff.model.CloneInstance;
import mcidiff.model.CloneSet;
import mcidiff.model.SeqMultiset;

public class TestMain2 {
	public static void main(String[] ars) {

		String path1 = "test/MemberDeclarationVisitor1.java";
		String path2 = "test/MemberDeclarationVisitor2.java";

		CloneInstance instance1 = new CloneInstance(path1, 1, 432);
		CloneInstance instance2 = new CloneInstance(path2, 1, 475);
		
//		CloneInstance instance1 = new CloneInstance(path1, 1, 103);
//		CloneInstance instance2 = new CloneInstance(path2, 1, 113);

		CloneSet set = new CloneSet("0");
		set.addInstance(instance1);
		set.addInstance(instance2);

		try {
			ArrayList<SeqMultiset> multisets = new SeqMCIDiff().diff(set, null);
			System.out.println(multisets);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
