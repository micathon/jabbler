package dictext;
import dict.*;

public class UseDict {

	public static void main(String[] args) {
		String word, s;
		Dict myDict = new Dict();
		boolean flag;
		int count;
		
		myDict.showInfo();
		// display first 10 words in dictionary
		for (int i=0; i < 10; i++) {
			word = myDict.getWord();
			System.out.println("Word " + i + ": " + word);
		}
		if (args.length == 0) {
			word = "cat";
		}
		else {
			word = args[0];
		}
		if (myDict.lookupWord(word)) {
			s = "found";
		}
		else {
			s = "not found";
		}
		System.out.println("Lookup: '" + word + "': " + s);
		count = myDict.chkFalseNegatives();
		flag = (count >= 0);
		if (!flag) {
			count = -count;
		}
		System.out.println("Check false negatives: " + getOkStr(flag));
		System.out.println("Words checked above: " + count);
	}
	
	private static String getOkStr(boolean flag) {
		if (flag) {
			return "OK";
		}
		else {
			return "not OK!";
		}
	}
	
}
