package rack;

import board.Board;
import iconst.IConst;

public class Rack implements IConst {
	
	private static final char EMPTYCH = ' ';
	private static final char OUTBLANKCH = '*';
	private Board bo;
	private char[] rackbuf;
	private int rackLen;
	private int rackPos;

	public Rack(Board bo) {
		this.bo = bo;
		rackbuf = new char[RACKSIZ];
		rackLen = 0;
		rackPos = 0;
	}
	
	public int fillRack() {
		int count = 0;
		
		for (int i=0; i < RACKSIZ; i++) {
			if (bagToRack()) {
				count++;
			}
		}
		return count;
	}
	
	public void clearRack() {
		for (int i=0; i < RACKSIZ; i++) {
			rackbuf[i] = EMPTYCH;
		}
		rackLen = 0;
		rackPos = 0;
	}
	
	public boolean pushRack(char ch) {
		if (rackLen >= RACKSIZ) {
			return false;
		}
		rackbuf[rackLen++] = ch;
		return true;
	}

	public boolean bagToRack() {
		char ch;
		
		if (rackLen >= RACKSIZ) {
			return false;
		}
		ch = bo.popBag();
		if (ch == (char) 0) {
			return false;
		}
		rackbuf[rackLen++] = ch;
		return true;
	}
	
	public String getRackStr() {
		// return contents of rack as a string
		// padded on right by blanks
		String s = "";
		char ch;
		int i, j;
		
		for (i=0; i < rackLen; i++) {
			ch = rackbuf[i];
			if (bo.isBlankBagLtr(ch)) {
				ch = OUTBLANKCH;
			}
			s += ch;
		}
		for (j = rackLen; j < RACKSIZ; j++) {
			s += EMPTYCH;
		}
		return s;
	}

	public int getRackPos() {
		return rackPos;
	}
	
	public void setRackPos(int pos) {
		rackPos = pos;
	}

	public int getRackLen() {
		return rackLen;
	}
	
	public char getRackChar(int pos) {
		return rackbuf[pos];
	}
	
	public void setRackChar(int pos, char ch) {
		rackbuf[pos] = ch;
	}
	
	public void swapAdjChars(int pos) {
		char ch = rackbuf[pos];
		rackbuf[pos] = rackbuf[pos + 1];
		rackbuf[pos + 1] = ch;
	}
	
	public char removeRackChar() {
		char ch;
		if (rackLen == 0) {
			return (char) 0;
		}
		ch = rackbuf[rackPos];
		for (int i = rackPos; i < rackLen - 1; i++) {
			rackbuf[i] = rackbuf[i + 1];
		}
		rackLen--;
		if (rackPos >= rackLen && rackPos > 0) {
			rackPos--;
		}
		return ch;
	}
	
	public void insertRackChar(char ch) {
		if (rackLen >= RACKSIZ) {
			return;
		}
		for (int i = rackLen; i > rackPos; i--) {
			rackbuf[i] = rackbuf[i - 1];
		}
		rackbuf[rackPos] = ch;
		rackLen++;
	}

	public void appendRackChar(char ch) {
		if (rackLen >= RACKSIZ) {
			return;
		}
		rackbuf[rackLen] = ch;
		rackLen++;
		rackPos = rackLen - 1;
	}
	
	public int getCharPos(char ch) {
		// find pos. of first ch in rack
		for (int i=0; i < rackLen; i++) {
			if (rackbuf[i] == ch) {
				return i;
			}
		}
		return -1;
	}
	
	public int getCharPosNext(char ch, int pos) {
		// find pos. of next ch in rack (start after n chars.)
		// where n = pos
		for (int i = pos; i < rackLen; i++) {
			if (rackbuf[i] == ch) {
				return i;
			}
		}
		return -1;
	}
	
	public void shuffleRack(String word) {
		// reorder tiles on rack so it starts with string = word
		// blank tiles in word parameter are non-alphabetic
		// called by wordOnRack user cmd.
		char ch;
		for (int i=0; i < rackLen; i++) {
			if (i < word.length()) {
				ch = word.charAt(i);
				if (Character.isAlphabetic(ch)) {
					rackbuf[i] = ch;
				}
				else {
					rackbuf[i] = EMPTYCH;
				}
			}
		}
	}
	
	public void setBlanks(int blankCount) {
		// set first n chars (up to n=blankCount) to blanks
		// subtract count of existing blanks from n
		// used for debugging only
		int count = 0;
		for (int i=0; i < rackLen; i++) {
			if (rackbuf[i] == EMPTYCH) {
				count++;
			}
		}
		for (int i=0; i < blankCount - count; i++) {
			if (i >= rackLen) {
				break;
			}
			rackbuf[i] = EMPTYCH;
		}
	}

}
