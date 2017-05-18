package robot;

import iconst.IConst;
import dict.Dict;
import board.Board;
import player.Player;

public class RobotPlayer implements IConst {

	private static final int ANCHORSIZ = 500;
	//private static final int BLPENALTY = 10;
	private static final int ERRSCORE = -100000;
	private Board bo;
	private Dict dict;
	private Anchor[] anchors;
	private int ancidx;
	private int enderCount;
	private int innerCount;
	private int[] alphaCounters;
	private int blankCount;
	
	public RobotPlayer(Board bo, Dict dict) {
		this.bo = bo;
		this.dict = dict;
		anchors = new Anchor[ANCHORSIZ];
		alphaCounters = new int[ALPHASIZ];
	}
	
	private Anchor getAnchorAt(int idx) {
		Anchor anchor = anchors[idx]; 
		if (anchor == null) {
			out("Null anchor idx = "+idx);
		}
		return anchor;
	}
	
	private void appendAnchor(Anchor anchor) {
		anchors[ancidx++] = anchor;
	}
	
	public int doRobotsTurn(int playerNo) {
		int count = 0;
		int j, k, n;
		int amountShort;
		char ch;
		boolean errFlag = false;
		boolean skipWord;
		boolean suppressInners = false;  // =true, debug only!
		String word;
		Player player = bo.getPlayer(playerNo);
		String hiWord = "";
		int wordScore;
		int hiScore = NEGSCORE;
		int hiPos = -1;
		int hiAnchorIdx = -1;
		Anchor anchor;
		
		ancidx = 0;
		enderCount = 0;
		innerCount = 0;
		//System.out.println("Robot player name = " + player.getName());
		if (bo.isFirstMove) {
			anchor = new Anchor(EMPTYCH, BOARDHEIGHT / 2, BOARDWIDTH / 2, 
				RACKSIZ, RACKSIZ, true, false);
			appendAnchor(anchor);
			enderCount = 1;
		}
		else {
			// note: no temp. letters on board
			genEnderAnchors();  // anchors at end(s) of an existing word
			genInnerAnchors();  // anchors perpendicular to letter belonging to
								// an existing word
			out("genInnerAnchors: innerCount = "+innerCount);
		}
		if (suppressInners) {
			innerCount = 0;
		}
		dict.resetPosVars();
		for (;;) {   // cycle through all words in dictionary
			word = dict.getWord();
			if (word.equals("")) {
				break;
			}
			word = word.toUpperCase();
			amountShort = getAmountShort(player, word); 
			if (amountShort > 1) {
				continue;  // curr rack is 2 or more letters short,
					// where any blanks, if any, must be used in word
			}
			count++;
			if (amountShort <= 0) {
				for (int i=0; i < enderCount; i++) {  
					// loop thru all "ender" anchors
					anchor = getAnchorAt(i);
					j = anchor.getWordPos(word, 0);
					while (j >= 0) {
						wordScore = getAnchorScore(player, word, i, j, false);
						if (wordScore > hiScore) {
							hiScore = wordScore;
							hiAnchorIdx = i;
							hiPos = j;
							hiWord = word;
						}
						else if (wordScore == ERRSCORE) {
							errFlag = true;
						}
						j = anchor.getWordPos(word, j + 1);
					}
				}
				if (bo.isFirstMove) {
					continue;
				}
			}
			for (int i = enderCount; i < enderCount + innerCount; i++) {
				// loop thru all "inner" anchors
				// use amountShort, blankCount, alphaCounters[k]
				anchor = getAnchorAt(i);
				ch = anchor.getLetter();
				k = (int) ch;
				k -= 'A';
				n = blankCount;
				for (j=0; j < ALPHASIZ; j++) {
					if (alphaCounters[j] >= 0) {
						continue;
					}
					if (j == k) {
						alphaCounters[j]++;
					}
					while ((n > 0) && (alphaCounters[j] < 0)) {
						n--;
						alphaCounters[j]++;
					}
				}
				skipWord = false;
				for (j=0; j < ALPHASIZ; j++) {
					if (alphaCounters[j] < 0) {
						skipWord = true;
						break;
					}
				}
				getAmountShort(player, word);  // restore alphaCounters
				if (skipWord) {
					continue;
				}
				if (isSkipAnchorScore(k, amountShort, blankCount)) {
					continue;
				}
				j = anchor.getWordPos(word, 0);
				while (j >= 0) {
					wordScore = getAnchorScore(player, word, i, j, false);
					if (wordScore > hiScore) {
						hiScore = wordScore;
						hiAnchorIdx = i;
						hiPos = j;
						hiWord = word;
					}
					else if (wordScore == ERRSCORE) {
						errFlag = true;
						System.out.println("[" + word + "], " + j);
					}
					j = anchor.getWordPos(word, j + 1);
				}
			}
		}
		out("Count of possible words = " + count);
		System.out.println("");
		if (errFlag) {
			System.out.println("Internal error encountered: insufficient blank count."); 
		}
		if (hiScore == NEGSCORE) {
			return hiScore;
		}
		wordScore = getAnchorScore(player, hiWord, hiAnchorIdx, hiPos, true);
		if (wordScore == ERRSCORE) {
			System.out.println("Internal error encountered: insufficient blanks in main.");
			return NEGSCORE;
		}
		return hiScore;
	}
	
	private void out(String buf) {
		boolean flag = debug;
		if (flag) {
			System.out.println(buf);
		}
	}
	
	private int getAmountShort(Player player, String word) {
		int n, j;
		char ch;
		
		for (int i=0; i < ALPHASIZ; i++) {
			alphaCounters[i] = 0;
		}
		blankCount = 0;
		n = player.rack.getRackLen();
		for (int i=0; i < n; i++) {
			ch = player.rack.getRackChar(i);
			if (ch == EMPTYCH) {
				blankCount++;
			}
			else {
				j = (int) ch;
				j -= 'A';
				alphaCounters[j]++;
			}
		}
		for (int i=0; i < word.length(); i++) {
			ch = word.charAt(i);
			j = (int) ch;
			j -= 'A';
			alphaCounters[j]--;
		}
		j = 0;
		for (int i=0; i < ALPHASIZ; i++) {
			if (alphaCounters[i] < 0) {
				j -= alphaCounters[i];
			}
		}
		return j - blankCount;
	}

	private boolean isSkipAnchorScore(int k, int amountShort, int blankCount) {
		// usually returns false, probably not really needed
		boolean rtnval = false;
		int alCount = -alphaCounters[k];
		// if (alphaCounters[k] == 0) 
			// eg. ch = E, rack has 2 E's, word has 2 E's
			//     ch = B, rack has 1 B,   word has 1 B
		// if (alphaCounters[k] == -1)
			// eg. ch = E, rack has 2 E's, word has 3 E's
			//     ch = B, rack has 0 B's, word has 1 B
		// if (alphaCounters[k] == -2)
			// eg. ch = E, rack has 1 E, word has 3 E's: blankCount >= 1
		if (amountShort == 1) {
			if (alCount > blankCount + 1) {
				rtnval = true;
			}
		}
		return rtnval;
	}
	
	private int getAnchorScore(Player player, String word, int anchorIdx, int wordPos,
		boolean isFinal) 
	{
		Anchor anchor = anchors[anchorIdx];
		boolean across = anchor.isAcross();
		boolean isInner = !anchor.isEnder();
		int score = 0;
		int row, col;

		row = anchor.getRow();
		col = anchor.getCol();
		if (across) {
			col -= wordPos;
		}
		else {
			row -= wordPos;
		}
		bo.setBrdPos(row, col);
		if (!makeWord(player, word, wordPos, isInner, across)) {
			cancelWord(player);
			return ERRSCORE;
		}
		if (!isFinal) {
			score = bo.calcRobotScore();
			cancelWord(player);
		}
		return score;
	}
	
	private boolean makeWord(Player player, String word, int wordPos,
		boolean isInner, boolean across) 
	{
		char ch;
		int j, k;
		int racklen;
		int row, col;
		boolean isBlank;
		
		k = 0;
		racklen = player.rack.getRackLen();
		for (int i=0; i < word.length(); i++) {
			if (k >= racklen) {
				break;
			}
			if (isInner && (i == wordPos) && !bo.isFirstMove) {
				continue;  // skip over anchor letter
			}
			k++;
			isBlank = false;
			ch = word.charAt(i);
			j = player.rack.getCharPos(ch);
			if (j < 0) {
				isBlank = true;
				j = player.rack.getCharPos(EMPTYCH);
			}
			if (j < 0) {
				return false;  // shouldn't happen
			}
			if (isBlank) {
				row = bo.getBrdRow();
				col = bo.getBrdCol();
				bo.turnBlocs.pushBlank(row, col, ch);
			}
			player.rack.setRackPos(j);
			// move curr. rack letter to board
			rackClick(player, across);
		}
		return true;  // returns false on error
	}
	
	private void rackClick(Player player, boolean across) {
		// place curr letter of rack onto board
		char ch;
		int dy, dx;
		int row, col;

		row = bo.getBrdRow();
		col = bo.getBrdCol();
		if (!bo.isBrdEmpty(row, col) || player.rack.getRackLen() == 0) {
			// curr board letter is not empty, or rack is empty
			return;
		}
		ch = player.rack.removeRackChar();
		// convert letter to lower case
		ch = bo.convertRackToBoard(ch);
		bo.setBoard(ch, row, col);
		if (across) {
			dy = 0;
			dx = 1;
		}
		else {
			dy = 1;
			dx = 0;
		}
		do {
			// skip over non-empty cells (if any) while locating
			//   next empty cell
			row += dy;
			col += dx;
		} while (bo.isValidBoardCell(row, col) && !bo.isBrdEmpty(row, col));
		// note: above loop will only execute once, while stmt. unnecessary
		if (bo.isValidBoardCell(row, col)) {
			// set curr board cell to next empty cell
			bo.setBrdPos(row, col);
		}
	}
	
	private void cancelWord(Player player) {
		int pos;
		char ch;
		
		pos = player.rack.getRackLen();
		if (pos >= RACKSIZ) {
			return;
		}
		player.rack.setRackPos(pos);
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (bo.isTempLtr(i, j)) {
					// move letter placed on board to rack
					ch = bo.getBoard(i, j);
					// convert to upper case
					ch = bo.convertBoardToRack(ch);
					player.rack.appendRackChar(ch);
					bo.setBoard(EMPTYCH, i, j);
				}
			}
		}
		bo.turnBlocs.clrStk();
	}
	
	private void genInnerAnchors() {
		int row, col;
		int head, tail;
		char ch;
		int anchorIdx = 0;
		Anchor anchor;
		
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (bo.isBrdEmpty(i, j)) {
					continue;
				}
				ch = bo.getBrdLtrCh(i, j);
				if (bo.isBrdFreeCell(i - 1, j) && bo.isBrdFreeCell(i + 1, j)) {
					// adj cells above/below are empty or not on board
					head = 0;
					tail = 0;
					row = i - 1;
					// calculate head = headroom
					while (bo.isValidBoardCell(row, j) && bo.isBrdEmpty(row, j)) {
						if (!bo.isBrdFreeCell(row - 1, j) || 
							!bo.isBrdFreeCell(row, j - 1) || !bo.isBrdFreeCell(row, j + 1)
						) {
							break;
						}
						row--;
					}
					if (row < i - 1) {
						head = i - row - 1;
					}
					row = i + 1;
					// calculate tail = tailroom
					while (bo.isValidBoardCell(row, j) && bo.isBrdEmpty(row, j)) {
						if (!bo.isBrdFreeCell(row + 1, j) || 
							!bo.isBrdFreeCell(row, j - 1) || !bo.isBrdFreeCell(row, j + 1)
						) {
							break;
						}
						row++;
					}
					if (row > i + 1) {
						tail = row - i - 1;
					}
					if ((head > 0) || (tail > 0)) {
						anchor = new Anchor(ch, i, j, head, tail, false, false);
						appendAnchor(anchor);
						anchorIdx++;
					}
				}
				else if (bo.isBrdFreeCell(i, j - 1) && bo.isBrdFreeCell(i, j + 1)) {
					// adj cells to left/right are empty or not on board
					head = 0;
					tail = 0;
					col = j - 1;
					// calculate head = headroom
					while (bo.isValidBoardCell(i, col) && bo.isBrdEmpty(i, col)) {
						if (!bo.isBrdFreeCell(i, col - 1) || 
							!bo.isBrdFreeCell(i - 1, col) || !bo.isBrdFreeCell(i + 1, col)
						) {
							break;
						}
						col--;
					}
					if (col < j - 1) {
						head = j - col - 1;
					}
					col = j + 1;
					// calculate tail = tailroom
					while (bo.isValidBoardCell(i, col) && bo.isBrdEmpty(i, col)) {
						if (!bo.isBrdFreeCell(i, col + 1) || 
							!bo.isBrdFreeCell(i - 1, col) || !bo.isBrdFreeCell(i + 1, col)
						) {
							break;
						}
						col++;
					}
					if (col > j + 1) {
						tail = col - j - 1;
					}
					if ((head > 0) || (tail > 0)) {
						anchor = new Anchor(ch, i, j, head, tail, true, false);
						appendAnchor(anchor);
						anchorIdx++;
					}
				}
			}
		}
		innerCount += anchorIdx;
	}
	
	private void genEnderAnchors() {
		genAcrossEnders();
		out("genAcrossEnders: enderCount = "+enderCount);
		genDownEnders();
		out("genDownEnders: enderCount = "+enderCount);
	}
	
	private void genAcrossEnders() {
		char ch;
		String baseWord;
		int k;
		boolean inWord;
		
		for (int i=0; i < BOARDHEIGHT; i++) {
			inWord = false;
			for (int j=0; j < BOARDWIDTH; j++) {  // scan row
				if (bo.isBrdEmpty(i, j)) {
					inWord = false;
					continue;
				}
				if (inWord) {
					continue;
				}
				if (j >= BOARDWIDTH - 1) {
					continue;
				}
				if (bo.isBrdEmpty(i, j + 1)) {
					inWord = false;
					continue;
				}
				inWord = true;
				baseWord = "";
				k = j;
				// build baseWord
				while (!bo.isBrdFreeCell(i, k)) {
					ch = bo.getBrdLtrCh(i, k++);
					baseWord += ch;
				}
				// anchor columns: j - 1, k
				if (bo.isValidBoardCell(i, j - 1) && bo.isBrdEmpty(i, j - 1) &&
					bo.isBrdFreeCell(i, j - 2) && 
					bo.isBrdFreeCell(i - 1, j - 1) && bo.isBrdFreeCell(i + 1, j - 1) 
				) {
					genAcrossEnderAnchors(baseWord, i, j - 1, true);
				}
				if (bo.isValidBoardCell(i, k) && bo.isBrdEmpty(i, k) &&
					bo.isBrdFreeCell(i, k + 1) && 
					bo.isBrdFreeCell(i - 1, k) && bo.isBrdFreeCell(i + 1, k) 
				) {
					genAcrossEnderAnchors(baseWord, i, k, false);
				}
			}
		}
	}
	
	private void genAcrossEnderAnchors(String baseWord, int i, int j, boolean isBegin) {
		String word;
		char ch;
		int head, tail;
		int row;
		int anchorIdx = 0;
		Anchor anchor;
		
		for (ch = 'A'; ch <= 'Z'; ch++) {
			if (isBegin) {
				word = "" + ch + baseWord;
			}
			else {
				word = baseWord + ch;
			}
			if (!dict.lookupWord(word)) {
				continue;
			}
			// word = anchor letter + baseWord (in either order)
			out("["+word+"]");
			head = 0;
			tail = 0;
			row = i - 1;
			// calculate head = headroom
			while (bo.isValidBoardCell(row, j) && bo.isBrdEmpty(row, j)) {
				if (!bo.isBrdFreeCell(row - 1, j) || 
					!bo.isBrdFreeCell(row, j - 1) || !bo.isBrdFreeCell(row, j + 1)
				) {
					break;
				}
				row--;
			}
			if (row < i - 1) {
				head = i - row - 1;
			}
			row = i + 1;
			// calculate tail = tailroom
			while (bo.isValidBoardCell(row, j) && bo.isBrdEmpty(row, j)) {
				if (!bo.isBrdFreeCell(row + 1, j) || 
					!bo.isBrdFreeCell(row, j - 1) || !bo.isBrdFreeCell(row, j + 1)
				) {
					break;
				}
				row++;
			}
			if (row > i + 1) {
				tail = row - i - 1;
			}
			if ((head > 0) || (tail > 0)) {
				anchor = new Anchor(ch, i, j, head, tail, false, true);
				appendAnchor(anchor);
				anchorIdx++;
			}
		}
		enderCount += anchorIdx;
	}
	
	private void genDownEnders() {
		char ch;
		String baseWord;
		int k;
		boolean inWord;
		
		for (int j=0; j < BOARDWIDTH; j++) {
			inWord = false;
			for (int i=0; i < BOARDHEIGHT; i++) {  // scan column
				if (bo.isBrdEmpty(i, j)) {
					inWord = false;
					continue;
				}
				if (inWord) {
					continue;
				}
				if (i >= BOARDHEIGHT - 1) {
					continue;
				}
				if (bo.isBrdEmpty(i + 1, j)) {
					inWord = false;
					continue;
				}
				inWord = true;
				baseWord = "";
				k = i;
				// build baseWord
				while (!bo.isBrdFreeCell(k, j)) {
					ch = bo.getBrdLtrCh(k++, j);
					baseWord += ch;
				}
				// anchor rows: i - 1, k
				if (bo.isValidBoardCell(i - 1, j) && bo.isBrdEmpty(i - 1, j) &&
					bo.isBrdFreeCell(i - 2, j) && 
					bo.isBrdFreeCell(i - 1, j - 1) && bo.isBrdFreeCell(i - 1, j + 1) 
				) {
					genDownEnderAnchors(baseWord, i - 1, j, true);
				}
				if (bo.isValidBoardCell(k, j) && bo.isBrdEmpty(k, j) &&
					bo.isBrdFreeCell(k + 1, j) && 
					bo.isBrdFreeCell(k, j - 1) && bo.isBrdFreeCell(k, j + 1) 
				) {
					genDownEnderAnchors(baseWord, k, j, false);
				}
			}
		}
	}
	
	private void genDownEnderAnchors(String baseWord, int i, int j, boolean isBegin) {
		String word;
		char ch;
		int head, tail;
		int col;
		int anchorIdx = 0;
		Anchor anchor;
		
		for (ch = 'A'; ch <= 'Z'; ch++) {
			if (isBegin) {
				word = "" + ch + baseWord;
			}
			else {
				word = baseWord + ch;
			}
			if (!dict.lookupWord(word)) {
				continue;
			}
			// word = anchor letter + baseWord (in either order)
			out("["+word+"]");
			head = 0;
			tail = 0;
			col = j - 1;
			// calculate head = headroom
			while (bo.isValidBoardCell(i, col) && bo.isBrdEmpty(i, col)) {
				if (!bo.isBrdFreeCell(i, col - 1) || 
					!bo.isBrdFreeCell(i - 1, col) || !bo.isBrdFreeCell(i + 1, col)
				) {
					break;
				}
				col--;
			}
			if (col < j - 1) {
				head = j - col - 1;
			}
			col = j + 1;
			// calculate tail = tailroom
			while (bo.isValidBoardCell(i, col) && bo.isBrdEmpty(i, col)) {
				if (!bo.isBrdFreeCell(i, col + 1) || 
					!bo.isBrdFreeCell(i - 1, col) || !bo.isBrdFreeCell(i + 1, col)
				) {
					break;
				}
				col++;
			}
			if (col > j + 1) {
				tail = col - j - 1;
			}
			if ((head > 0) || (tail > 0)) {
				anchor = new Anchor(ch, i, j, head, tail, true, true);
				appendAnchor(anchor);
				anchorIdx++;
			}
		}
		enderCount += anchorIdx;
	}
}

class Anchor implements IConst {
	
	private byte letter;
	private byte row;
	private byte col;
	private byte headRoom;
	private byte tailRoom;
	private boolean across;
	private boolean ender;
	
	Anchor(char letter, int row, int col, int head, int tail, 
		boolean across, boolean ender) 
	{
		this.letter = (byte) (letter - 'A');
		this.row = (byte) row;
		this.col = (byte) col;
		this.headRoom = (byte) head;
		this.tailRoom = (byte) tail;
		this.across = across;
		this.ender = ender;   // often letter = 'S'
			// or say, letter = 'B' next to 'READ', making BREAD
	}
	
	public char getLetter() {
		return (char) (letter + 'A');
	}
	
	public int getRow() {
		return row;
	}
	
	public int getCol() {
		return col;
	}
	
	public int getHeadRoom() {
		return headRoom;
	}
	
	public int getTailRoom() {
		return tailRoom;
	}
	
	public boolean isAcross() {
		return across;
	}
	
	public boolean isEnder() {
		return ender;
	}
	
	public int getWordPos(String word, int startPos) {
		char ch = getLetter();
		int len = word.length();

		if (ch != EMPTYCH) { }
		else if (startPos < len) {
			return startPos;
		}
		else {
			return -1;
		}
		for (int i=startPos; i < len; i++) {
			if (ch != word.charAt(i)) {
				continue;
			}
			if (headRoom < i) {
				continue;
			}
			// ex: len = 3, i = 2, tail = 0
			if (len - (i + 1) > tailRoom) {
				continue;
			}
			return i;
		}
		return -1;
	}
}
