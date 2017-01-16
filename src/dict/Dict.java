package dict;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

public class Dict {

	private String dictFileName = "ospd.txt";
	private String dictFolderName = "dat";
	private String dictFilePath;
	private static final int BLOCKSIZ = 1000;
	private static final int BLOCKCOUNT = 617;
	private DictBuf[] dictBuffers = new DictBuf[BLOCKCOUNT];
	private int wordCount;
	private int blkCount;
	private int blkPos;
	private int wordPos;
	
	public Dict() {
		String s;
		String sep;
		int idx = -1;
		int n;
		
		wordCount = 0;
		blkCount = 0;
		sep = File.separator;
		dictFilePath = ".." + sep + dictFolderName + sep + dictFileName;
		resetPosVars();

		// read in dictionary from text file, one word per line
		
		try (BufferedReader br = new BufferedReader (new FileReader(dictFilePath)))
		{
			while ((s = br.readLine()) != null) {
				wordCount++;
				n = s.length() + 1;
				if (idx < 0 || (idx + n > BLOCKSIZ)) {
					dictBuffers[blkCount++] = new DictBuf(s);
					idx = n;
				}
				else {
					dictBuffers[blkCount - 1].appendWord(s);
					idx += n;
				}
			}
		} catch (IOException exc) {
			System.out.println("I/O Error: " + exc);
		}
	}
	
	public void showInfo() {
		System.out.println("Dict file name = " + dictFilePath);
		System.out.println("Dictionary word count = " + wordCount);
		System.out.println("Dictionary block count = " + blkCount);
	}
	
	public void resetPosVars() {
		blkPos = 0;
		wordPos = 0;
	}
	
	public String getWord() {
		// return next word in dictionary (null string if EOF)
		String word = "";
		char ch;
		byte b;
		DictBuf buf = dictBuffers[blkPos];
		
		if (wordPos >= buf.getBufIdx()) {
			// go to next block:
			if (blkPos >= blkCount - 1) {
				return "";  // EOF
			}
			blkPos++;
			wordPos = 0;
		}
		while ((b = buf.getByte(wordPos++)) != (byte) 0) {
			ch = (char) b;
			word += ch;
		}
		word = word.toUpperCase();
		return word;
	}
	
	public boolean lookupWord(String word) {
		// lookup word in dictionary
		// binary search to find containing block
		// linear search within block
		// return true if found
		int blkA = 0;
		int blkB = blkCount - 1;
		int blkMid;
		int cmpval;
		String dicword;
		
		word = word.toUpperCase();
		wordPos = 0;
		blkPos = blkB;
		dicword = getWord();
		cmpval = word.compareTo(dicword); 
		if (cmpval < 0) { 
			for (;;) {
				blkMid = (blkA + blkB) / 2;
				blkPos = blkMid;
				wordPos = 0;
				dicword = getWord();
				cmpval = word.compareTo(dicword);
				if (cmpval > 0) {
					blkA = blkMid;
				}
				else if (cmpval < 0) {
					blkB = blkMid;
				}
				else {
					return true;
				}
				if ((blkB - blkA) <= 1) {
					blkPos = blkA;
					wordPos = 0;
					break;
				}
			}
		}
		blkMid = blkPos;
		do {
			if (cmpval == 0) {
				return true;
			}
			dicword = getWord();
			if ((blkPos > blkMid) || dicword.equals("")) {
				return false;
			}
			cmpval = word.compareTo(dicword); 
		} while (cmpval >= 0);
		return false;
	}
	
	public int chkFalseNegatives() {
		// returns no. of words checked
		// checks first, second, last words in each block
		// calls lookupWord on each word checked
		// returns negated count on failure
		String word, lastWord;
		int pos;
		int count = 0;

		for (int i=0; i < blkCount; i++) {
			blkPos = i;
			wordPos = 0;
			word = getWord();
			count++;
			if (!lookupWord(word)) {
				return -count;
			}
			blkPos = i;
			wordPos = 0;
			word = getWord();
			word = getWord();
			count++;
			if (!lookupWord(word)) {
				return -count;
			}
			blkPos = i;
			wordPos = 0;
			do {
				pos = wordPos;
				lastWord = word;
				word = getWord();
			} while (!word.equals("") && blkPos == i);
			blkPos = i;
			wordPos = pos;
			count++;
			if (!lookupWord(lastWord)) {
				return -count;
			}
		}
		return count;
	}

}

class DictBuf {

	private static final int BLOCKSIZ = 1000;
	private byte[] block;
	private int bufidx;
	
	DictBuf(String word) {
		// create new block with single word at beginning of block
		block = new byte[BLOCKSIZ];
		bufidx = word.length();
		for (int i=0; i < bufidx; i++) {
			setByte(i, (byte) word.charAt(i));
		}
		setByte(bufidx++, (byte) 0);
	}
	
	public byte getByte(int idx) {
		return block[idx];
	}
	
	public void setByte(int idx, byte val) {
		block[idx] = val;
	}
	
	public int getBufIdx() {
		return bufidx;
	}
	
	public void appendWord(String word) {
		// append word (null-terminated) at current block position
		int n = word.length();
		
		if (bufidx + n + 1 > BLOCKSIZ) {
			return;
		}
		for (int i=0; i < n; i++) {
			setByte(bufidx + i, (byte) word.charAt(i));
		}
		bufidx += n;
		setByte(bufidx++, (byte) 0);
	}
	
}
