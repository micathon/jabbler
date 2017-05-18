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
	private int errBlkNo = 5; //* old error
	private boolean debug = false;
	public boolean errflag;
	public String errmsg;
	
	public Dict() {
		String s;
		String sep;
		int idx = -1;
		int n;
		
		wordCount = 0;
		blkCount = 0;
		errflag = false;
		errmsg = "";
		sep = File.separator;
		dictFilePath = ".." + sep + dictFolderName + sep + dictFileName;
		resetPosVars();

		// read in dictionary from text file, one word per line
		
		try (BufferedReader br = new BufferedReader (new FileReader(dictFilePath)))
		{
			while ((s = br.readLine()) != null) {
				s = s.trim();
				if (s.equals("")) {  
					logMsg("Warning: blank line in Dict.");
					continue;
				}
				wordCount++;
				n = s.length() + 1;
				if (idx < 0 || (idx + n > BLOCKSIZ)) {
					dictBuffers[blkCount++] = new DictBuf(s);
					idx = n;
					if (blkCount == errBlkNo) {  //*
						logMsg("Start of nth block, word = "+s+", len = "+(n - 1));
					}
				}
				else {
					dictBuffers[blkCount - 1].appendWord(s);
					idx += n;
					if (blkCount == errBlkNo) {  //*
						logMsg("Middle of nth block, word = "+s+", len = "+(n - 1));
					}
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
		int count;
		DictBuf buf = dictBuffers[blkPos];
		
		if (wordPos >= buf.getBufIdx()) {
			// go to next block:
			if (blkPos >= blkCount - 1) {
				return "";  // EOF
			}
			blkPos++;
			wordPos = 0;
			buf = dictBuffers[blkPos];
		}
		count = 0;
		while ((b = buf.getByte(wordPos++)) != (byte) 0) {
			ch = (char) b;
			word += ch;
			count++;
		}
		if (count == 0) {
			logMsg("blkPos = "+blkPos+", wordPos = "+wordPos);
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
	
	private void logMsg(String msg) {
		errmsg = msg;
		errflag = true;
		if (debug) {
			System.out.println(msg);
		}
	}

}

class DictBuf {

	private static final int BLOCKSIZ = 1000;
	private byte[] block;
	private int bufidx;
	private boolean debug = false;
	
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
		byte b;
		
		if (n == 0) {  
			logMsg("Error: null string passed to appendWord.");
		}
		if (bufidx + n + 1 > BLOCKSIZ) {
			logMsg("Warning: appendWord returns on bufidx overflow.");
			return;
		}
		for (int i=0; i < n; i++) {
			b = (byte) word.charAt(i);
			setByte(bufidx + i, b);
			if (b == 0) { 
				logMsg("Error: null byte encountered in dict.");
			}
		}
		bufidx += n;
		setByte(bufidx++, (byte) 0);
	}
	
	private void logMsg(String msg) {
		if (debug) {
			System.out.println(msg);
		}
	}

}
