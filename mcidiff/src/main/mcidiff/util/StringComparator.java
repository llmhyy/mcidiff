package mcidiff.util;


public class StringComparator implements IObjComparator{
	public boolean ignoreCase;
	
	/**
	 * @param ignoreCase
	 */
	public StringComparator(boolean ignoreCase) {
		super();
		this.ignoreCase = ignoreCase;
	}

	public boolean isEquals(Object obj1, Object obj2){
		String str1 = obj1.toString();
		String str2 = obj2.toString();
		
		if(ignoreCase){
			return str1.toLowerCase().equals(str2.toLowerCase());
		}
		else{
			return str1.equals(str2);
		}
	}
}
