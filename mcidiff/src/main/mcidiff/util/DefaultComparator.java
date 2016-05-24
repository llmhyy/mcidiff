package mcidiff.util;

public class DefaultComparator implements IObjComparator {

	@Override
	public boolean isEquals(Object obj1, Object obj2) {
		return obj1.equals(obj2);
	}

}
