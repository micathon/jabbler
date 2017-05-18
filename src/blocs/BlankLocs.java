package blocs;

import iconst.IConst;

public class BlankLocs implements IConst {
	
	private static final int BLOCSIZ = BLSTKSIZ;
	private static final char EMPTYCH = ' ';
	private int[] blocs;      // location(s) of blank(s)
	private int bloccount;    // no. of blanks
	private char[] blankStr;  // value(s) of blank(s)
	
	public BlankLocs() {
		blocs = new int[BLOCSIZ];
		blankStr = new char[BLOCSIZ];
		bloccount = 0;
	}
	
	public int getBlankLoc(int i, int j) {
		// returns 16-bit integer
		// high byte = row no.
		// low byte = col no.
		return (i << 8) + j;
	}
	
	public int getBlocCount() {
		return bloccount;
	}
	
	public void clrStk() {
		bloccount = 0;
	}
	
	public boolean pushBlank(int i, int j, char ch) {
		int blankLoc = getBlankLoc(i, j);
		
		if (bloccount >= BLOCSIZ) {
			return false;
		}
		blocs[bloccount] = blankLoc;
		blankStr[bloccount++] = ch;
		return true;
	}
	
	public boolean popBlank() {
		if (bloccount <= 0) {
			return false;
		}
		bloccount--;
		return true;
	}
	
	public int topBlankLoc() {
		if (bloccount <= 0) {
			return -1;
		}
		return blocs[bloccount - 1];
	}
	
	public char topBlankVal() {
		if (bloccount <= 0) {
			return EMPTYCH;
		}
		return blankStr[bloccount - 1];
	}
	
	public int getBlankRow(int blankLoc) {
		if (blankLoc < 0) {
			return -1;
		}
		return blankLoc >> 8;
	}
	
	public int getBlankCol(int blankLoc) {
		if (blankLoc < 0) {
			return -1;
		}
		return blankLoc & 0xFF;
	}
	
	public int getBlocIdx(int i, int j) {
		int bloc = getBlankLoc(i, j);
		
		for (int k=0; k < bloccount; k++) {
			if (blocs[k] == bloc) {
				return k;
			}
		}
		return -1;
	}
	
	public char getBlankVal(int i, int j) {
		int k = getBlocIdx(i, j);
		if (k < 0) {
			return EMPTYCH;
		}
		else {
			return blankStr[k];
		}
	}
	
	public boolean setBlankVal(int i, int j, char ch) {
		int k = getBlocIdx(i, j);
		if (k < 0) {
			return false;
		}
		else {
			blankStr[k] = ch;
			return true;
		}
	}
	
	public boolean delBloc(int idx) {
		if (idx < 0 || idx >= bloccount) {
			return false;
		}
		blocs[idx] = blocs[--bloccount];
		blankStr[idx] = blankStr[bloccount];
		return true;
	}
	
}
