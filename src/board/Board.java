package board;

import iconst.IConst;
import dict.Dict;
import blocs.BlankLocs;
import robot.RobotPlayer;
import player.Player;

public class Board implements IConst {
	
	private char[][] board = new char[BOARDHEIGHT][BOARDWIDTH];
	private int[][] brdfacltr = {
		{1,1,1,2,1,1,1,1,1,1,1,2,1,1,1},
		{1,1,1,1,1,3,1,1,1,3,1,1,1,1,1},
		{1,1,1,1,1,1,2,1,2,1,1,1,1,1,1},
		{2,1,1,1,1,1,1,2,1,1,1,1,1,1,2},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,3,1,1,1,3,1,1,1,3,1,1,1,3,1},
		{1,1,2,1,1,1,2,1,2,1,1,1,2,1,1},
		{1,1,1,2,1,1,1,1,1,1,1,2,1,1,1},
		{1,1,2,1,1,1,2,1,2,1,1,1,2,1,1},
		{1,3,1,1,1,3,1,1,1,3,1,1,1,3,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{2,1,1,1,1,1,1,2,1,1,1,1,1,1,2},
		{1,1,1,1,1,1,2,1,2,1,1,1,1,1,1},
		{1,1,1,1,1,3,1,1,1,3,1,1,1,1,1},
		{1,1,1,2,1,1,1,1,1,1,1,2,1,1,1}
	};
	private int[][] brdfacwrd = {
		{3,1,1,1,1,1,1,3,1,1,1,1,1,1,3},
		{1,2,1,1,1,1,1,1,1,1,1,1,1,2,1},
		{1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
		{1,1,1,2,1,1,1,1,1,1,1,2,1,1,1},
		{1,1,1,1,2,1,1,1,1,1,2,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{3,1,1,1,1,1,1,2,1,1,1,1,1,1,3},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
		{1,1,1,1,2,1,1,1,1,1,2,1,1,1,1},
		{1,1,1,2,1,1,1,1,1,1,1,2,1,1,1},
		{1,1,2,1,1,1,1,1,1,1,1,1,2,1,1},
		{1,2,1,1,1,1,1,1,1,1,1,1,1,2,1},
		{3,1,1,1,1,1,1,3,1,1,1,1,1,1,3}
	};
	private char[] bag = new char[BAGSIZ];
	private int[] bagltrcount = {
	//  A,B,C,D,E, F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z
		9,2,2,4,12,2,3,2,9,1,1,4,2,6,8,2,1,6,4,6,4,2,2,1,2,1
	};
	private int[] bagltrval = {
	//  A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q, R,S,T,U,V,W,X,Y,Z
		1,3,3,2,1,4,2,4,1,8,5,1,3,1,1,3,10,1,1,1,1,4,4,8,4,10
	};
	private int bagSize = BAGSIZ;
	public String errmsg;
	public BlankLocs boardBlocs, turnBlocs, xBlocs;
	public RobotPlayer rp;
	private Dict dict;
	private boolean isUseDict = false;
	public boolean isFirstMove;
	private int playerCount;
	private Player[] playerTab = new Player[MAXPLAYTABSIZ];
	private boolean rackMode;
	private boolean swapMode;
	private boolean pipeMode;
	public boolean isXblanks = false;
	public boolean is3kind = false;
	public boolean is4kind = false;
	private int currPlayerNo;
	private int currBrdRow;
	private int currBrdCol;
	private int currBrdMode;
	private int passCount;
	private int outPlayerNo;
	private boolean isGameOver;
	
	public Board(String[] args) {
		boolean isBagOk;
		int weeBagSiz = TMPBAGSIZ; 
		
		errmsg = "";
		initBoard();
		isBagOk = initBag();
		if (BAGSIZ == weeBagSiz) {}
		else if (!isBagOk) {
			errmsg = "Bad arr inz: bagltrcount";
		}
		else if (!testBagOps(7)) {
			// pass it loop count of 7
			errmsg = "Fail: testBagOps";
		}
		// use default (2 humans) unless args (from main) is not null
		playerCount = getNewPlayerCount(true, args);
		initPlayerNo();
		dict = new Dict();
		rp = new RobotPlayer(this, dict);
	}

	private void initBoard() {
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				board[i][j] = EMPTYCH;
			}
		}
		currBrdRow = BOARDHEIGHT / 2;
		currBrdCol = BOARDWIDTH / 2;
		currBrdMode = BRDACROSS;
		initMisc();
	}
	
	public void initMisc() {
		rackMode = false;
		swapMode = false;
		pipeMode = true;
		isFirstMove = true;
		isGameOver = false;
		boardBlocs = new BlankLocs();  // blank loc(s) on board
		turnBlocs = new BlankLocs();   // blank loc(s) in curr. turn
		xBlocs = new BlankLocs();      // blank loc(s) being interchanged
		bagSize = BAGSIZ;
		outPlayerNo = -1;
		passCount = 0;
	}
	
	public void restartGame(String newNames) {
		String[] args = {""}; 
		int count;
		Player player;

		args[0] = newNames;
		initBoard();
		if (!initBag()) {
			out("initBag failed!");
		}
		count = getNewPlayerCount(false, args);
		if (count >= 0) {
			playerCount = count;
		}
		else {
			for (int i=0; i < playerCount; i++) {
				player = playerTab[i];
				player.restartPlayer();
				player.rack.clearRack();
				player.rack.fillRack();
			}
		}
		initPlayerNo();
	}
	
	public void setGameFlags(String cmdarg) {
		// user is setting up to 3 flags
		cmdarg = cmdarg.toUpperCase();
		// 3kind/4kind mutually exclusive
		is3kind = false;    // 3
		is4kind = false;    // 4
		isXblanks = false;  // I
		isUseDict = false;  // D
		// flags are ordered: if all 3 flags are true, then
		// cmdarg = 3ID or 4ID
		if (cmdarg.equals("")) {
			return;
		}
		else if (cmdarg.length() > 3) {
			return;
		}
		else if (cmdarg.equals("I")) {
			isXblanks = true;
		}
		else if (cmdarg.equals("3")) {
			is3kind = true;
		}
		else if (cmdarg.equals("4")) {
			is4kind = true;
		}
		else if (cmdarg.equals("D")) {
			isUseDict = true;
		}
		else if (cmdarg.length() < 2) {
			return;
		}
		else if (cmdarg.equals("3I")) {
			is3kind = true;
			isXblanks = true;
		}
		else if (cmdarg.equals("4I")) {
			is4kind = true;
			isXblanks = true;
		}
		else if (cmdarg.equals("3D")) {
			is3kind = true;
			isUseDict = true;
		}
		else if (cmdarg.equals("4D")) {
			is4kind = true;
			isUseDict = true;
		}
		else if (cmdarg.equals("ID")) {
			isXblanks = true;
			isUseDict = true;
		}
		else if (cmdarg.length() < 3) {
			return;
		}
		else if (cmdarg.equals("3ID")) {
			is3kind = true;
			isXblanks = true;
			isUseDict = true;
		}
		else if (cmdarg.equals("4ID")) {
			is4kind = true;
			isXblanks = true;
			isUseDict = true;
		}
	}
	
	private boolean initBag() {
		int k = 0;
		int blankCount = 0;
		
		for (int i=0; i < bagltrcount.length; i++) {
			for (int j=0; j < bagltrcount[i]; j++, k++) {
				if (k < BAGSIZ) {
					bag[k] = (char)('A' + i);
				}
			}
		}
		for (int i=k; i < BAGSIZ; i++) {
			bag[i] = EMPTYCH;
			blankCount++;
		}
		// return false on error
		return (blankCount == BLHARDSIZ);
	}
	
	public boolean isBlankBagLtr(char ch) {
		return ch == EMPTYCH;
	}
	
	public char getBoard(int i, int j) {
		return board[i][j];
	}
	
	public void setBoard(char ch, int i, int j) {
		board[i][j] = ch;
	}
	
	public int getFacLtr(int i, int j) {
		// 1 = normal
		// 2 = double letter score
		// 3 = triple letter score
		return brdfacltr[i][j];
	}
	
	public int getFacWrd(int i, int j) {
		// 1 = normal
		// 2 = double word score
		// 3 = triple word score
		return brdfacwrd[i][j];
	}
	
	public int getBrdHeight() {
		return BOARDHEIGHT;
	}
	
	public int getBrdWidth() {
		return BOARDWIDTH;
	}
	
	public char getBrdCellCh(int i, int j) {
		// display char. of cell on board (when board is displayed)
		if (isBrdBlank(i, j)) {
			return BRDBLCH;
		}
		if (board[i][j] != EMPTYCH) {
			return board[i][j];
		}
		if (pipeMode) {}
		else if ((brdfacltr[i][j] > 1) || (brdfacwrd[i][j] > 1)) {
			return BRDLWCH;
		}
		if (brdfacltr[i][j] == 2) {
			return BRD2LCH;
		}
		if (brdfacltr[i][j] == 3) {
			return BRD3LCH;
		}
		if (brdfacwrd[i][j] == 2) {
			return BRD2WCH;
		}
		if (brdfacwrd[i][j] == 3) {
			return BRD3WCH;
		}
		return BRDSPCH;
	}
	
	public boolean isBrdBlank(int i, int j) {
		int k = boardBlocs.getBlocIdx(i, j);
		return k >= 0;
	}
	
	public boolean isValidBoardCell(int i, int j) {
		return (i >= 0) && (i < BOARDHEIGHT) &&
			(j >= 0) && (j < BOARDWIDTH);
	}
	
	public char getBrdLtrCh(int i, int j) {
		// display letter (upper case) of cell on board
		// if blank then return value of blank
		char ch;
		String s;
		
		if (!isValidBoardCell(i, j)) {
			return EMPTYCH;
		}
		ch = getBoard(i, j);
		if (ch == EMPTYCH) {
			return ch;
		}
		if (ch == BRDBLCH) {
			ch = boardBlocs.getBlankVal(i, j);
		}
		else if (ch == BRDTMPCH) {
			ch = turnBlocs.getBlankVal(i, j);
		}
		else if (Character.isLowerCase(ch)) {
			s = "" + ch;
			s = s.toUpperCase();
			ch = s.charAt(0);
		}
		return ch;
	}
	
	public boolean getUseDict() {
		return isUseDict;
	}
	
	public void setUseDict(boolean flag) {
		isUseDict = flag;
	}
	
	public void showBoard() {
		// display board
		// to right of board, display:
		//   racks, player names
		//   caret points to curr. tile of curr. rack
		//   turn scores, total scores, player names
		String boardCell;
		char ch;
		int n;
		Player player = null;
		boolean isShortCell = false;
		char leadCh;
		boolean isAlpha;
		boolean isLeadCh;
		boolean isTrailCh;
		boolean mayTrail;
		char pipeCh = '|';
		String sepBoardRack = " |";
		String sepRackNames = sepBoardRack;
		char rackModeCh = getModeChar(rackMode);
		char swapModeCh = getModeChar(swapMode);
		int playerNo = 0;
		boolean showRackPos = false;
		int turnScore, totScore;
		String turnScoreBuf, totScoreBuf;
		
		System.out.print(" ");
		for (int j=0; j < BOARDWIDTH; j++) {
			boardCell = " " + getBrdHexCh(j);
			System.out.print(boardCell);
		}
		System.out.print(" " + sepBoardRack + "Rack: [" + rackModeCh + ']');
		System.out.print(" Swap: [" + swapModeCh + ']');
		System.out.println(" Bag: " + bagSize);
		for (int i=0; i < BOARDHEIGHT; i++) {
			ch = getBrdHexCh(i);
			System.out.print(ch);
			isShortCell = false;
			for (int j=0; j < BOARDWIDTH; j++) {
				ch = getBrdCellCh(i, j);
				boardCell = "" + ch;
				isAlpha = Character.isLetter(ch) || (ch == BRDBLCH);
				isAlpha = isAlpha && pipeMode;
				isLeadCh = isAlpha && isValidBoardCell(i, j - 1) && isBrdEmpty(i, j - 1);
				isTrailCh = isAlpha && isValidBoardCell(i, j + 1) && isBrdEmpty(i, j + 1) &&
					!(i == currBrdRow && (j + 1) == currBrdCol);
				mayTrail = false;
				if (i == currBrdRow && j == currBrdCol) {
					if (isBrdEmpty(i, j)) {
						switch (currBrdMode) {
						case BRDACROSS:
							boardCell = "" + ACROSSCH;
							break;
						case BRDDOWN:
							boardCell = "" + DOWNCH;
							break;
						}
					}
					else if (ch == BRDBLCH) {
						ch = boardBlocs.getBlankVal(i, j);
						boardCell = "" + ch;
						boardCell = boardCell.toLowerCase();
					}
					boardCell = "[" + boardCell + ']';
					isShortCell = true;  // next cell not preceded by blank
				}
				else if (!isShortCell) {
					// previous cell not succeeded by ']' or '|'
					if (isLeadCh) {
						leadCh = pipeCh;
					}
					else {
						leadCh = ' ';
					}
					boardCell = leadCh + boardCell;
					mayTrail = true;
				}
				else if (j <= BOARDWIDTH - 1) {
					isShortCell = false;
					mayTrail = true;
				}
				if (mayTrail && isTrailCh) {
					boardCell += pipeCh;
					isShortCell = true;
				}
				System.out.print(boardCell);
			}
			if (!isShortCell) {
				sepBoardRack = " " + sepBoardRack;
			}
			if (showRackPos) {
				System.out.print(sepBoardRack);
				n = RACKSIZ;
				for (int j=0; j < n; j++) {
					if (j == player.rack.getRackPos()) {
						// indicator char. at curr. tile position
						System.out.print(PTRCH);
					}
					else {
						System.out.print(' ');
					}
				}
				System.out.print(sepRackNames);
				showRackPos = false;
			}
			else if (playerNo < playerCount) {
				player = playerTab[playerNo++];
				System.out.print(sepBoardRack + player.rack.getRackStr());
				System.out.print(sepRackNames + player.getName());
				if (playerNo == (currPlayerNo + 1)) {
					showRackPos = true;
				}
			}
			else if (i == (playerCount + 1)) {
				System.out.print(sepBoardRack + "________|");
			}
			else if (i == (playerCount + 2)) {
				System.out.print(sepBoardRack + "Wrd Tot" + sepRackNames);
				playerNo = playerCount;
			}
			else if ((i > (playerCount + 2)) && (playerNo < (2 * playerCount))) {
				player = playerTab[playerNo++ - playerCount];
				turnScore = player.getTurnScore();
				totScore = player.getScore();
				turnScoreBuf = "" + turnScore;
				totScoreBuf = "" + totScore;
				turnScoreBuf = padLeft(turnScoreBuf, 3);
				totScoreBuf = padLeft(totScoreBuf, 3);
				System.out.print(sepBoardRack + turnScoreBuf + ' ' + totScoreBuf);
				System.out.print(sepRackNames + player.getName());
			}
			sepBoardRack = sepRackNames;
			System.out.println("");
		}
		System.out.println("");
	}
	
	public char getBrdHexCh(int i) {
		if (i < 0 || i >= 16) {
			return EMPTYCH;
		}
		if (i < 10) {
			return (char)('0' + i);
		}
		return (char)('A' + i - 10);
	}
	
	public int hexDigToInt(String hexbuf) {
		// hexbuf = "" + single hex digit (except 0xF)
		// used to convert user input to board column no.
		// returns -1 on error
		int rtnval;
		char ch;
		
		hexbuf = hexbuf.trim();
		hexbuf = hexbuf.toUpperCase();
		if (hexbuf.length() != 1) {
			return -1;
		}
		ch = hexbuf.charAt(0);
		rtnval = (int)(ch - '0');
		if (rtnval >= 0 && rtnval <= 9) {
			return rtnval;
		}
		rtnval = (int)(ch - 'A' + 10);
		if (rtnval >= 10 && rtnval < 15) {
			return rtnval;
		}
		return -1;
	}
	
	public int getBagSize() {
		return bagSize;
	}
	
	public boolean pushBag(char ltr) {
		if (bagSize >= BAGSIZ) {
			return false;
		}
		bag[bagSize++] = ltr;
		return true;
	}
	
	public char popBag() {
		int bagIdx;
		char rtnval;
		
		if (bagSize <= 0) {
			return (char) 0;
		}
		// pick random tile char. from bag
		bagIdx = rollDice(bagSize);
		rtnval = bag[bagIdx];
		bag[bagIdx] = bag[bagSize - 1];
		bagSize--;
		return rtnval;
	}
	
	public int rollDice(int faceCount) {
		// random no. between 0 and faceCount - 1
		int rtnval;
		rtnval = (int) Math.floor(Math.random() * faceCount);
		return rtnval;
	}
	
	public void out(String buf) {
		boolean flag = debug;
		if (flag) {
			System.out.println(buf);
		}
	}
	
	public boolean testBagOps(int count) {
		// test pop/push bag opers.
		// count between 1 and 99
		// return false on error
		char[] word = new char[count];
		char ch;
		String wordstr = "";
		
		for (int i=0; i < count; i++) {
			ch = popBag();
			word[i] = ch;
			wordstr += ch;
		}
		for (int i=0; i < count; i++) {
			ch = word[i];
			if (!pushBag(ch)) {
				return false;
			}
		}
		if (bagSize != BAGSIZ) {
			return false;
		}
		out("Bag word = [" + wordstr + ']');
		return true;
	}
	
	public int getPlayerCount() {
		return playerCount;
	}
	
	public int getActiveCount() {
		Player player;
		int count = 0;

		for (int i=0; i < playerCount; i++) {
			player = playerTab[i];
			if (player.isActive()) {
				count++;
			}
		}
		return count;
	}
	
	private int getNewPlayerCount(boolean firstTime, String[] args) {
		// user is setting between 1 and 4 player names
		// return no. of players
		Player player;
		String name;
		String oldName;
		String[] newArgs;
		int argCount;
		boolean handled;
		int playerCount = 0;
		
		if (firstTime) {
			for (int i=0; i < MAXPLAYTABSIZ; i++) {
				playerTab[i] = new Player(this);
			}
			if (args.length == 0) {
				return getDefPlayerCount();
			}
			newArgs = args;
			argCount = args.length;
		}
		else {
			newArgs = expandArgs(args[0]);
			if (newArgs[0].equals("")) {
				out("getNewPlayerCount: no args");
				return -1;  // don't change player names
			}
			argCount = getArgCount(newArgs);
			out("getNewPlayerCount: argCount = "+argCount);
			for (int i=0; i < argCount; i++) {
				player = playerTab[i];
				player.restartPlayer();
				player.rack.clearRack();
				player.rack.fillRack();
			}
		}
		for (int i=0; i < argCount; i++) {
			if (i >= MAXPLAYTABSIZ) {
				break;
			}
			playerCount++;
			player = playerTab[i];
			if (firstTime) {
				name = args[i];
			}
			else {
				name = newArgs[i];
			}
			if (name.equals("")) {
				playerCount--;
				break;
			}
			handled = false;
			if (name.length() == 1) {
				// handle special chars.: dot, hyphen, caret
				handled = true;
				oldName = player.getName();
				switch (name.charAt(0)) {
				case DITTOCH:
					if (oldName.equals("")) {
						player.setPlayer("", i + 1, false);
					}
					break;
				case USEIDXCH:
					player.setPlayer("", i + 1, false);
					break;
				case ROBOTCH:
					player.setPlayer("", i + 1, true);
					break;
				default:
					handled = false;
				}
			}
			if (!handled) {
				// player name is non-special char.
				player.setPlayer(name, 0, false);
			}
		}
		return playerCount;
	}
	
	public String[] expandArgs(String argstr) {
		// argstr = list of up to 4 words
		// words separated by spaces
		// put each word in an array of strings
		String[] strTab = new String[MAXPLAYTABSIZ];
		String arg;
		int j = 0;
		int k = -1;
		int n;
		
		argstr = argstr.trim();
		for (int i=0; i < strTab.length; i++) {
			n = argstr.indexOf(' ');
			if (n < 0) {
				arg = argstr;
			}
			else {
				arg = argstr.substring(0, n);
			}
			if (arg.equals("")) {
				k = i;
				break;
			}
			strTab[j++] = arg;
			argstr = argstr.substring(arg.length());
			argstr = argstr.trim();
		}
		if (k >= 0) {
			for (int i = k; i < strTab.length; i++) {
				strTab[i] = "";
			}
		}
		return strTab;
	}

	private int getArgCount(String[] argList) {
		int argLen = argList.length;
		int count = 0;
		for (int i=0; i < argLen; i++) {
			if (argList[i].equals("")) {
				break;
			}
			count++;
		}
		return count;
	}
	
	private int getDefPlayerCount() {
		// set player names to defaults for first 2 players
		// clear playerTab elements for subsequent 2 players
		Player player;

		for (int i=0; i < DEFPLAYTABSIZ; i++) {
			player = playerTab[i];
			player.setPlayer("", i + 1, false);
		}
		for (int i = DEFPLAYTABSIZ; i < MAXPLAYTABSIZ; i++) {
			player = playerTab[i];
			player.clearPlayer();
		}
		return DEFPLAYTABSIZ;
	}
	
	public char getModeChar(boolean flag) {
		return (flag) ? 'X' : '.';
	}
	
	public boolean isRackMode() {
		return rackMode;
	}
	
	public void toggleRackMode() {
		rackMode = !rackMode;
	}
	
	public boolean isSwapMode() {
		return swapMode;
	}
	
	public void toggleSwapMode() {
		swapMode = !swapMode;
	}
	
	public boolean isPipeMode() {
		return pipeMode;
	}
	
	public void togglePipeMode() {
		pipeMode = !pipeMode;
	}
	
	public int getCurrPlayerNo() {
		return currPlayerNo;
	}
	
	public void setCurrPlayerNo(int playerNo) {
		currPlayerNo = playerNo;
	}

	public void initPlayerNo() {
		currPlayerNo = rollDice(playerCount);
	}
	
	public Player getCurrPlayer() {
		return playerTab[currPlayerNo];
	}
	
	public Player getPlayer(int playerNo) {
		return playerTab[playerNo];
	}
	
	public int getOutPlayerNo() {
		return outPlayerNo;
	}
	
	public void setOutPlayerNo(int playerNo) {
		outPlayerNo = playerNo;
	}

	public boolean getGameOver() {
		return isGameOver;
	}
	
	public void setGameOver(boolean flag) {
		isGameOver = flag;
	}

	public int getBrdRow() {
		return currBrdRow;
	}
	
	public int getBrdCol() {
		return currBrdCol;
	}
	
	public void setBrdPos(int row, int col) {
		currBrdRow = row;
		currBrdCol = col;
	}
	
	public int getBrdMode() {
		return currBrdMode;
	}
	
	public void cycleBrdMode() {
		currBrdMode++;
		currBrdMode %= MODECOUNT;
	}
	
	public boolean isBrdEmpty(int row, int col) {
		return (board[row][col] == EMPTYCH);
	}
	
	public boolean isBrdFreeCell(int row, int col) {
		return !isValidBoardCell(row, col) || (board[row][col] == EMPTYCH);
	}
	
	public boolean isWordOnBoard() {
		// true if any temp. tiles on board
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (isTempLtr(i, j)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isWordAcross() {
		return currBrdMode == BRDACROSS;
	}
	
	public char convertRackToBoard(char ch) {
		// temp. letters on board are lower case
		// temp. blank on board differs from perm. blank
		String s = "" + ch;
		if (ch == EMPTYCH) {
			return BRDTMPCH;
		}
		s = s.toLowerCase();
		return s.charAt(0);
	}
	
	public char convertBoardToRack(char ch) {
		// letters on rack are upper case
		String s = "" + ch;
		if (ch == BRDTMPCH) {
			return EMPTYCH;
		}
		s = s.toUpperCase();
		return s.charAt(0);
	}
	
	public boolean isTempLtr(int i, int j) {
		// tile at board cell is temp, not perm.
		char ch = board[i][j];
		if (ch == EMPTYCH || ch == BRDBLCH) {
			return false;
		}
		if (ch == BRDTMPCH) {
			return true;
		}
		return Character.isLowerCase(ch);
	}
	
	public boolean isPermLtr(int i, int j) {
		// tile at board cell is perm, not temp.
		char ch = board[i][j];
		if (ch == EMPTYCH || ch == BRDTMPCH) {
			return false;
		}
		if (ch == BRDBLCH) {
			return true;
		}
		return Character.isUpperCase(ch);
	}
	
	public char convertTempToPerm(char ch) {
		String s;
		if (ch == EMPTYCH) {
			return EMPTYCH;
		}
		if (ch == BRDTMPCH) {
			return BRDBLCH;
		}
		if (Character.isLowerCase(ch)) {
			s = "" + ch;
			s = s.toUpperCase();
			return s.charAt(0);
		}
		return ch;
	}
	
	public boolean isValidPerm(int i, int j) {
		// tile at board cell is perm.
		// (i, j) need not be a valid cell loc.
		return isValidBoardCell(i, j) && isPermLtr(i, j);
	}
	
	public int getTempLtrCount() {
		// count no. of temp. tiles on board
		int count = 0;
		
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (isTempLtr(i, j)) {
					count++;
				}
			}
		}
		return count;
	}
	
	// calculate score of word intersecting with (row, col)
	// negate and subtract 1 if word contains less than 2 temp letters
	// board(row, col) assumed to be temp letter
	
	public int calcTurnScore(StringBuilder outBadWrdLst, boolean isUseDict) {
		int row = -1;
		int col = -1;
		int score = 0;
		int ascore, dscore;
		boolean isAcross;
		String aword, dword;
		String badWrdLst;
		
		// find first temp. tile:
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (isTempLtr(i, j)) {
					row = i;
					col = j;
					break;
				}
			}
			if (row >= 0) {
				break;
			}
		}
		if (row < 0) {
			return 0;
		}
		badWrdLst = "";
		aword = getWordThru(row, col, true);  // across
		dword = getWordThru(row, col, false); // down
		if (isUseDict) {
			// append aword/dword if not in dict.
			badWrdLst = appendBadWord(badWrdLst, aword);
			badWrdLst = appendBadWord(badWrdLst, dword);
		}
		// calc. scores of words going thru curr. loc
		// across, then down
		ascore = getVecScore(row, col, true);
		dscore = getVecScore(row, col, false);
		isAcross = (ascore >= 0) || (dscore >= 0);
		if (!isAcross) {
			// only one temp. tile on board
			outBadWrdLst.append(badWrdLst);
			score = -(2 + ascore + dscore);
			return score;
		}
		isAcross = (ascore >= 0);
		if (isAcross) {
			score = ascore - dscore - 1;
		}
		else {
			score = dscore - ascore - 1;
		}
		// for all temp. tiles in word:
		for (;;) {
			if (isAcross) {
				col++;
			}
			else {
				row++;
			}
			if (!isValidBoardCell(row, col) || isBrdEmpty(row, col)) {
				break;
			}
			if (!isTempLtr(row, col)) {
				continue;
			}
			// subtract -ve score(s) of perpendicular word(s) from score of main word
			score -= 1 + getVecScore(row, col, !isAcross);
			if (isUseDict) {
				aword = getWordThru(row, col, !isAcross);
				// append perpendicular word if not in dict.
				badWrdLst = appendBadWord(badWrdLst, aword);
			}
		}
		outBadWrdLst.append(badWrdLst);
		return score;
	}

	public int calcRobotScore() {
		int row = -1;
		int col = -1;
		int score = 0;
		int ascore, dscore;
		boolean isAcross;
		
		// find first temp. tile:
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (isTempLtr(i, j)) {
					row = i;
					col = j;
					break;
				}
			}
			if (row >= 0) {
				break;
			}
		}
		if (row < 0) {
			return 0;
		}
		// calc. scores of words going thru curr. loc
		// across, then down
		ascore = getVecScore(row, col, true);
		dscore = getVecScore(row, col, false);
		isAcross = (ascore >= 0) || (dscore >= 0);
		if (!isAcross) {
			// only one temp. tile on board
			score = -(2 + ascore + dscore);
			return score;
		}
		isAcross = (ascore >= 0);
		if (isAcross) {
			score = ascore - dscore - 1;
		}
		else {
			score = dscore - ascore - 1;
		}
		// for all temp. tiles in word:
		for (;;) {
			if (isAcross) {
				col++;
			}
			else {
				row++;
			}
			if (!isValidBoardCell(row, col) || isBrdEmpty(row, col)) {
				break;
			}
			if (!isTempLtr(row, col)) {
				continue;
			}
			// subtract -ve score(s) of perpendicular word(s) from score of main word
			score -= 1 + getVecScore(row, col, !isAcross);
		}
		return score;
	}

	private int getVecScore(int row, int col, boolean isAcross) {
		// calc. score of word going thru (row, col)
		// score is -ve if only one temp. letter found:
		//   real score = -1 - score
		int n;
		int score = 0;
		int product = 1;
		int currLtrScore;
		int tempCount = 0;
		int permCount = 0;
		
		if (!isTempLtr(row, col)) {
			return 0;
		}
		n = getLeadCount(row, col, isAcross);
		if (isAcross) {
			col -= n;
		}
		else {
			row -= n;
		}
		// we are now at beginning of word
		do {
			currLtrScore = getCellVal(row, col);
			if (isTempLtr(row, col)) {
				currLtrScore *= getFacLtr(row, col);
				product *= getFacWrd(row, col);
				tempCount++;
			}
			else {
				permCount++;
			}
			score += currLtrScore;
			if (isAcross) {
				col++;
			}
			else {
				row++;
			}
		} while (isValidBoardCell(row, col) && !isBrdEmpty(row, col));
		if (tempCount < 2 && permCount == 0) {
			// no word found to calc. score
			// real score = -1 - (-1) = 0
			return -1;
		}
		score *= product;
		if (tempCount < 2) {
			score = -1 - score;
		}
		else if (tempCount >= RACKSIZ) {
			score += BONUS7LTRWRD;
		}
		return score;
	}
	
	public int getLeadCount(int row, int col, boolean isAcross) {
		// no. of leading tiles in word thru board cell
		int i = row;
		int j = col;
		
		for (;;) {
			if (isAcross) {
				col--;
				if (!isValidBoardCell(row, col)) {
					col++;
					break;
				}
			}
			else {
				row--;
				if (!isValidBoardCell(row , col)) {
					row++;
					break;
				}
			}
			if (!isBrdEmpty(row, col)) {
				continue;
			}
			if (isAcross) {
				col++;
				break;
			}
			row++;
			break;
		}
		if (isAcross) {
			return j - col;
		}
		return i - row;
	}

	public int getTrailCount(int row, int col, boolean isAcross) {
		// no. of trailing tiles in word thru board cell
		int i = row;
		int j = col;
		
		for (;;) {
			if (isAcross) {
				col++;
				if (!isValidBoardCell(row, col)) {
					col--;
					break;
				}
			}
			else {
				row++;
				if (!isValidBoardCell(row , col)) {
					row--;
					break;
				}
			}
			if (!isBrdEmpty(row, col)) {
				continue;
			}
			if (isAcross) {
				col--;
				break;
			}
			row--;
			break;
		}
		if (isAcross) {
			return col - j;
		}
		return row - i;
	}
	
	public String getWordThru(int row, int col, boolean isAcross) {
		// return word thru board cell
		// any blanks replaced with letter values
		String word = "";
		int m = getLeadCount(row, col, isAcross);
		int n = getTrailCount(row, col, isAcross);
		
		if (isAcross) {
			col -= m;
		}
		else {
			row -= m;
		}
		for (int i = 0; i <= m + n; i++) {
			word += getBrdLtrCh(row, col);
			if (isAcross) {
				col++;
			}
			else {
				row++;
			}
		}
		return word;
	}
	
	public String appendBadWord(String outbuf, String word) {
		if (word.length() <= 1 || dict.lookupWord(word)) {
			return outbuf;
		}
		if (outbuf.equals("")) {
			return word;
		}
		return outbuf + ' ' + word;
	}
	
	public boolean lookupWord(String word) {
		return dict.lookupWord(word);
	}

	public int getCellVal(int row, int col) {
		// return value of letter at board cell
		char ch;
		String s;
		int val;
		
		ch = getBoard(row, col);
		if ((ch == BRDTMPCH) || isBrdBlank(row, col) || isBrdEmpty(row, col)) {
			return 0;
		}
		s = "" + ch;
		s = s.toUpperCase();
		ch = s.charAt(0);
		val = ch - 'A';
		return getLtrVal(val);
	}

	public int getLtrVal(int i) {
		return bagltrval[i];
	}
	
	public int getChrVal(char ch) {
		// return value of letter
		if (!Character.isAlphabetic(ch)) {
			return 0;
		}
		if (Character.isUpperCase(ch)) {
			return getLtrVal(ch - 'A');
		}
		return getLtrVal(ch - 'a');
	}
	
	public int getPassCount() {
		return passCount;
	}
	
	public void incPassCount() {
		passCount++;
	}
	
	public void zeroPassCount() {
		passCount = 0;
	}
	
	public String getSpaces(int n) {
		String s = "";
		
		for (int i=0; i < n; i++) {
			s += ' ';
		}
		return s;
	}
	
	public String padLeft(String buf, int width) {
		String s;
		
		s = getSpaces(width - buf.length()) + buf;
		return s;
	}
}
