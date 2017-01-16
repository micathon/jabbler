package scrab;
import dict.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Scrab implements IConst {

	public static void main(String[] args) 
		throws IOException
	{
		BufferedReader br = new BufferedReader(new
			InputStreamReader(System.in));
		String inbuf;
		Board bo = new Board(args);
		String errmsg = bo.errmsg;

		if (errmsg.length() > 0) {
			System.out.println("Error initializing game:");
			System.out.println(errmsg);
			System.exit(1);
		}
		CmdProc cmdProc = new CmdProc(bo, br);
		
		System.out.println("Enter Scrabble commands (h for help).\n");
		for (;;) {
			System.out.print(DEFPROMPT);
			inbuf = br.readLine();
			inbuf = inbuf.trim();
			if (inbuf.length() == 0) {
				continue;
			}
			if (!cmdProc.doCmd(inbuf)) {
				break;
			}
		}
		System.out.println("\nbye");
	}

}

class CmdProc implements IConst {
	
	private static final char BADCMDCH = ' ';
	private Board bo;
	private Player player;
	private BufferedReader br;
	private boolean isNewGame;
	private char lastcmd = BADCMDCH;
	private boolean isBrief;
	
	CmdProc(Board bo, BufferedReader br) {
		this.bo = bo;
		this.br = br;
		isNewGame = true;
		isBrief = true;
	}
	
	public boolean doCmd(String cmdbuf) {
		// keep processing (single) current user command
		// return false on user quit
		char ch;
		String cmdarg;
		boolean rtnval = true;
		
		ch = getCmdChar(cmdbuf);
		cmdarg = getCmdArg(cmdbuf);
		switch (ch) {
		case BADCMDCH:
			break;
		case 'r':
			restartGame(cmdarg);
			isNewGame = true;
			break;
		case 'z':
			setGameFlags(cmdarg, isNewGame);
			break;
		case 'q':
			rtnval = quitGame();
			break;
		case 's':
			bo.showBoard();
			break;
		case 't':
			toggleRackMode();
			break;
		case 'u':
			toggleSwapMode();
			break;
		case 'i':
			upArrow();
			break;
		case 'm':
			downArrow();
			break;
		case 'j':
			leftArrow();
			break;
		case 'k':
			rightArrow();
			break;
		case 'x':
			boardRackClick(cmdarg);
			break;
		case 'n':
			nextPress();
			break;
		case 'p':
			passOrMulti();
			break;
		case 'c':
			cancelWord();
			break;
		case 'b':
			backSpace();
			break;
		case 'l':
			lookupWord(cmdarg);
			break;
		case 'w':
			makeWord(cmdarg);
			break;
		case 'W':
			wordOnRack(cmdarg);
			break;
		case 'h':
			helpScreen(lastcmd);
			break;
		}
		if (ch != 'r') {
			isNewGame = false;
		}
		if (!rtnval || bo.getGameOver()) {
			if (getYNflag("Start new game? ")) {
				restartGame("");
				rtnval = true;
			}
			else {
				rtnval = false;
			}
		}
		lastcmd = ch;
		return rtnval;
	}
	
	private void helpScreen(char lastcmd) {
		// display brief help screen
		// display opposite (brief/verbose) help screen
		//   if previous command repeated
		if (lastcmd != 'h') {
			isBrief = true;
		}
		else {
			isBrief = !isBrief;
		}
		if (isBrief) {
			briefHelp();
		}
		else {
			verboseHelp();
		}
	}
	
	private void briefHelp() {
		System.out.println("r - restart game");
		System.out.println("z - set game flags");
		System.out.println("q - quit");
		System.out.println("s - show board");
		System.out.println("t - toggle rack mode");
		System.out.println("u - toggle swap mode");
		System.out.println("i - up arrow");
		System.out.println("m - down arrow");
		System.out.println("j - left arrow");
		System.out.println("k - right arrow");
		System.out.println("x - click on rack/board");
		System.out.println("n - next player");
		System.out.println("p - pass/scrabble");
		System.out.println("c - cancel word");
		System.out.println("b - backspace");
		System.out.println("l - lookup word");
		System.out.println("w - make word");
		System.out.println("wr - reorder letters on rack");
		System.out.println("h - toggle brief/verbose help");
		System.out.println("help - same as h");
	}
	
	private void verboseHelp() {
		System.out.println("r . . .   : 3 players");
		System.out.println("r ^ ^     : 2 robot players");
		System.out.println("r Jim Bob : Jim vs. Bob");
		System.out.println("r - - . ^ : player1, player2, ditto, robot4");
		System.out.println("r         : use same player names");
		System.out.println("          : player1 - human player");
		System.out.println("          : robot1 - robot player");
		System.out.println("z [flags] : use house rules");
		System.out.println("z 3id     : 3-of-a-kind, interchangeable blanks, can use dictionary");
		System.out.println("z 4d      : 4-of-a-kind, can use dictionary (i.e. no challenging)");
		System.out.println("p         : letters on board = AAA & 3-of-a-kind: put one back");
		System.out.println("p         : letters on board = IIII & 4-of-a-kind: put one back");
		System.out.println("p abc     : scrabble: put back \"abc\"");
		System.out.println("w cat     : make word: \"cat\"");
		System.out.println("w cat. s  : make word: \"cats\" (blank = s)");
		System.out.println("wr cat    : shuffle rack: starts with \"cat\"");
		System.out.println("wr rob.n  : shuffle rack: starts with \"robin\" (blank = i)");
		System.out.println("wr mo se  : shuffle rack: starts with \"mouse\" (blank = u)");
	}
	
	public char getCmdChar(String cmdbuf) {
		// return first char. of command line on success
		// special cases : return value
		//   wr myword   : W
		//   wr          : W
		//   help        : h
		int n;
		char ch;
		
		if (cmdbuf.length() == 1) {
			return cmdbuf.charAt(0);
		}
		if (cmdbuf.startsWith("wr")) {
			if (cmdbuf.length() > 2 && cmdbuf.charAt(2) != ' ') {
				return BADCMDCH; 
			}
			return 'W';
		}
		if (cmdbuf.equals("help")) {
			return 'h';
		}
		n = cmdbuf.indexOf(' ');
		if (n < 0 || n > 1) {
			return BADCMDCH;
		}
		ch = cmdbuf.charAt(0);
		return ch;
	}
	
	public String getCmdArg(String cmdbuf) {
		// return arg of command line (everything after blank)
		// null string if no blank found
		int n;
		String s;
		
		n = cmdbuf.indexOf(' ');
		if (n < 0) {
			return "";
		}
		s = cmdbuf.substring(n);
		s = s.trim();
		return s;
	}
	
	private char getCmdArgBlank(String cmdarg) {
		// return upper case of single-letter cmdarg
		// return dummy char. otherwise
		char ch;
		String s;
		if (cmdarg.length() != 1) {
			return EMPTYCH;
		}
		ch = cmdarg.charAt(0);
		if (!Character.isLetter(ch)) {
			return EMPTYCH;
		}
		s = "" + ch;
		s = s.toUpperCase();
		ch = s.charAt(0);
		return ch;
	}
	
	public String getReadLine() {
		String inbuf;
		try {
			inbuf = br.readLine();
		}
		catch (IOException exc) {
			inbuf = "";
		}
		return inbuf;
	}
	
	public boolean getYNflag(String msg) {
		// display msg
		// if msg ends with blank, append standard y/n suffix
		// force user to enter Y or N
		// keep looping on invalid input
		String inbuf;
		boolean flag = false;
		
		if (msg.length() == 0) {
			return false;
		}
		if (msg.charAt(msg.length() - 1) == ' ') {
			msg += "[Y/N]";
		}
		System.out.println(msg);
		for (;;) {
			System.out.print(DEFPROMPT);
			inbuf = getReadLine();
			inbuf = inbuf.trim();
			inbuf = inbuf.toUpperCase();
			if (inbuf.length() == 0) {
				continue;
			}
			if (inbuf.length() > 1) {
				//
			}
			else if (inbuf.equals("Y")) {
				flag = true;
				break;
			}
			else if (inbuf.equals("N")) {
				flag = false;
				break;
			}
			System.out.println(msg);
		}
		return flag;
	}
	
	public char getCmdSwitch(String msg, String cmds, int defCmdNo) {
		// display msg
		// force user to enter char. found in cmds
		// defCmdNo:
		//   idx of default char. in cmds (starts at 1)
		//   0 if none
		// defch = default char. (blank if none)
		// bare CR entered: use default char. (if any)
		// loop on invalid input
		// return valid char (upper-cased) entered by user
		// never returns blank
		char ch = ' ';
		char defch = ' ';
		String inbuf;
		
		cmds = cmds.toUpperCase();
		if (defCmdNo <= 0 || defCmdNo > cmds.length()) {
			defCmdNo = 0;
		}
		else {
			defch = cmds.charAt(defCmdNo - 1);
		}
		do {
			System.out.println(msg);
			System.out.print(DEFPROMPT);
			inbuf = getReadLine();
			inbuf = inbuf.trim();
			inbuf = inbuf.toUpperCase();
			if (inbuf.length() == 0 && defCmdNo == 0) {
				continue;
			}
			if (inbuf.length() == 0) {
				ch = defch;
			}
			else if (inbuf.length() > 1) {
				continue;
			}
			else {
				ch = inbuf.charAt(0);
				if (cmds.indexOf(ch) < 0) {
					ch = ' ';
				}
			}
		} while (ch == ' ');
		return ch;
	}

	public String getCmdString(String msg) {
		String inbuf;

		System.out.println(msg);
		System.out.print(DEFPROMPT);
		inbuf = getReadLine();
		inbuf = inbuf.trim();
		return inbuf;
	}
		
	private void restartGame(String cmdarg) {
		bo.restartGame(cmdarg);
		bo.showBoard();
	}
	
	private void setGameFlags(String cmdarg, boolean isNewGame) {
		// user inputs optional rule variations
		// only at beginning of game
		if (!isNewGame) {
			System.out.println("Error: game already in progress.");
			return;
		}
		bo.setGameFlags(cmdarg);
		bo.showBoard();
	}
	
	private boolean quitGame() {
		// return false if current game is over
		char ch;
		if (getYNflag("Terminate current game? ")) {
			return false;
		}
		if (!getYNflag("Does current player wish to drop out? ")) {
			return true;
		}
		// else current player is quitting
		if (bo.getActiveCount() <= 1) {
			return false;
		}
		player = bo.getCurrPlayer();
		while (player.rack.getRackLen() > 0) {
			ch = player.rack.removeRackChar();
			bo.pushBag(ch);
		}
		player.setActive(false);
		nextPlayer(false);
		return true;
	}
	
	private void toggleRackMode() {
		bo.toggleRackMode();
		bo.showBoard();
	}
	
	private void toggleSwapMode() {
		bo.toggleSwapMode();
		bo.showBoard();
	}
	
	private void upArrow() {
		boolean isRefresh = false;
		int row = bo.getBrdRow();
		int col = bo.getBrdCol();

		if (bo.isRackMode() || bo.isWordOnBoard()) {
			return;
		}
		if (row > 0) {
			bo.setBrdPos(row - 1, col);
			isRefresh = true;
		}
		// else already at top
		if (isRefresh) {
			bo.showBoard();
		}
	}
	
	private void downArrow() {
		boolean isRefresh = false;
		int row = bo.getBrdRow();
		int col = bo.getBrdCol();

		if (bo.isRackMode() || bo.isWordOnBoard()) {
			return;
		}
		if (row < (bo.getBrdHeight() - 1)) {
			bo.setBrdPos(row + 1, col);
			isRefresh = true;
		}
		// else already at bottom
		if (isRefresh) {
			bo.showBoard();
		}
	}
	
	private void leftArrow() {
		boolean isRefresh = false;
		
		if (bo.isRackMode()) {
			isRefresh = leftOnRack();
		}
		else if (!bo.isWordOnBoard()) {
			isRefresh = leftOnBoard();
		}
		if (isRefresh) {
			bo.showBoard();
		}
	}
	
	private void rightArrow() {
		boolean isRefresh = false;
		
		if (bo.isRackMode()) {
			isRefresh = rightOnRack();
		}
		else if (!bo.isWordOnBoard()) {
			isRefresh = rightOnBoard();
		}
		if (isRefresh) {
			bo.showBoard();
		}
	}
	
	private boolean leftOnBoard() {
		boolean isRefresh = false;
		int row = bo.getBrdRow();
		int col = bo.getBrdCol();

		if (col > 0) {
			bo.setBrdPos(row, col - 1);
			isRefresh = true;
		}
		// else already at leftmost col.
		return isRefresh;
	}
	
	private boolean rightOnBoard() {
		boolean isRefresh = false;
		int row = bo.getBrdRow();
		int col = bo.getBrdCol();

		if (col < (bo.getBrdWidth() - 1)) {
			bo.setBrdPos(row, col + 1);
			isRefresh = true;
		}
		// else already at rightmost col.
		return isRefresh;
	}
	
	private boolean leftOnRack() {
		int i;
		
		player = bo.getCurrPlayer();
		i = player.rack.getRackPos();
		if (i <= 0) {
			// already at leftmost letter on rack
			return false;
		}
		if (bo.isSwapMode()) {
			player.rack.swapAdjChars(i - 1);
		}
		player.rack.setRackPos(i - 1);
		return true;
	}
	
	private boolean rightOnRack() {
		int i, j;
		
		player = bo.getCurrPlayer();
		i = player.rack.getRackPos();
		j = player.rack.getRackLen();
		if (i >= j - 1) {
			// already at rightmost letter on rack
			return false;
		}
		if (bo.isSwapMode()) {
			player.rack.swapAdjChars(i);
		}
		player.rack.setRackPos(i + 1);
		return true;
	}
	
	private void boardRackClick(String cmdarg) {
		int i, j, row, col;
		char ch;
		if (bo.isRackMode()) {
			// letter rack to board
			rackClick(cmdarg);
			return;
		}
		if (bo.isWordOnBoard()) {
			// letter board to rack
			boardClick();
			return;
		}
		row = bo.getBrdRow();
		col = bo.getBrdCol();
		player = bo.getCurrPlayer();
		if (bo.isXblanks && bo.isBrdBlank(row, col)) {
			ch = bo.boardBlocs.getBlankVal(row, col);
			i = player.rack.getCharPos(ch);
			if (i >= 0) {
				// un-interchange blank clicked on:
				player.rack.setRackChar(i, EMPTYCH);
				j = bo.boardBlocs.getBlocIdx(row, col);
				bo.boardBlocs.delBloc(j);
				bo.setBoard(ch, row, col);
				bo.xBlocs.pushBlank(row, col, ch);
			}
		}
		else {
			// toggle across/down
			bo.cycleBrdMode();
		}
		bo.showBoard();
	}
	
	private void rackClick(String cmdarg) {
		rackClickRtn(cmdarg);
		bo.showBoard();
	}
		
	private void rackClickRtn(String cmdarg) {
		// place curr letter of rack onto board
		char ch, ch1;
		char blval;
		int row, col;
		int dy, dx;
		player = bo.getCurrPlayer();
		row = bo.getBrdRow();
		col = bo.getBrdCol();
		if (!bo.isBrdEmpty(row, col) || player.rack.getRackLen() == 0) {
			// curr board letter is not empty, or rack is empty
			return;
		}
		ch1 = player.rack.removeRackChar();
		// convert letter to lower case
		ch = bo.convertRackToBoard(ch1);
		if (ch == BRDTMPCH) {
			// place blank onto board
			blval = getCmdArgBlank(cmdarg);
			if (blval == EMPTYCH) {
				// user forgot to type space and then blank value
				System.out.println("Enter: [x v], where v is blank value.");
				player.rack.insertRackChar(ch1);
				return;
			}
			bo.turnBlocs.pushBlank(row, col, blval);
		}
		bo.setBoard(ch, row, col);
		if (bo.isWordAcross()) {
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
		if (bo.isValidBoardCell(row, col)) {
			// set curr board cell to next empty cell
			bo.setBrdPos(row, col);
		}
	}
	
	private void boardClick() {
		// move previously placed letter on board to rack
		// letter(s) already placed on board
		char ch;
		int row, col;
		int dy, dx;
		player = bo.getCurrPlayer();
		if (player.rack.getRackLen() >= RACKSIZ) {
			return;
		}
		row = bo.getBrdRow();
		col = bo.getBrdCol();
		if (bo.isWordAcross()) {
			dy = 0;
			dx = -1;
		}
		else {
			dy = -1;
			dx = 0;
		}
		if (bo.isBrdEmpty(row, col)) {
			do {  // skip over previous permanent board letters
				row += dy;
				col += dx;
			} while (bo.isPermLtr(row, col));
		}
		bo.setBrdPos(row, col);
		ch = bo.getBrdCellCh(row, col);
		if (ch == BRDTMPCH) {
			bo.turnBlocs.popBlank();
		}
		// convert letter to upper case
		ch = bo.convertBoardToRack(ch);
		player.rack.insertRackChar(ch);
		bo.setBoard(EMPTYCH, row, col);
		bo.showBoard();
	}

	private void nextPress() {
		// player indicated: go to next player
		char ch;
		int row, col;
		int loc;
		int score, turnScore;
		StringBuilder badWrdLst = new StringBuilder("");
		boolean isUseDict = bo.getUseDict();
		int playerNo;
		Player loser;
		boolean validTurn = true;
		
		player = bo.getCurrPlayer();
		if (bo.isFirstMove) {
			// first turn's word must cover center square
			row = BOARDHEIGHT / 2;
			col = BOARDWIDTH / 2;
			ch = bo.getBoard(row, col);
			if (ch == EMPTYCH) {
				validTurn = false;
			}
			else if (RACKSIZ - player.rack.getRackLen() < 2) {
				// first turn's word must have 2 or more letters
				validTurn = false;
			}
		}
		else {
			validTurn = false;
			for (int i=0; i < BOARDHEIGHT; i++) {
				for (int j=0; j < BOARDWIDTH; j++) {
					if (bo.isTempLtr(i, j)) {
						// if any letter placed on board adj. to
						//   permanent letter, then turn is OK
						if (bo.isValidPerm(i, j + 1) ||
							bo.isValidPerm(i, j - 1) ||
							bo.isValidPerm(i + 1, j) ||
							bo.isValidPerm(i - 1, j)) {
							validTurn = true;
						}
						// if none are adj. to perm. letter, then
						//   turn is not OK
					}
				}
			}
		}
		if (!validTurn) {
			System.out.println("Error: word on board disconnected.");
			return;
		}
		if (bo.xBlocs.getBlocCount() > bo.turnBlocs.getBlocCount()) {
			// user did not use interchanged blank(s) in her turn
			System.out.println("Error: you must use blank(s) this turn.");
			return;
		}
		// bad word list will contain list of newly formed invalid words
		turnScore = bo.calcTurnScore(badWrdLst, isUseDict);  // calc. score
		if (badWrdLst.length() > 0) {
			System.out.println("Error - you've made invalid words: [" + badWrdLst + "]");
			return;
		}
		player.setTurnScore(turnScore);
		bo.showBoard();
		if (isUseDict) {
			ch = getCmdSwitch("Please enter: (n)ext, [B]ack", "NB", 2);
		}
		else {
			ch = getCmdSwitch("Please enter: (n)ext, (c)hallenge, [B]ack", "NCB", 3);
		}
		switch (ch) {
		case 'N':
			break;
		case 'C':
			// other player has challenged
			playerNo = getChallengeLoser();
			if (playerNo < 0) {
				bo.showBoard();
				return;
			}
			if (playerNo == bo.getCurrPlayerNo()) {
				// challengee unsuccessful, word cancelled, misses turn
				cancelWordRtn();
				passPlayer();
				return;
			}
			// set turn-missing player (unsuccessful challenger):
			loser = bo.getPlayer(playerNo);
			loser.setSkip(true);
			break;
		default:
			bo.showBoard();
			return;
		}
		score = player.getScore() + turnScore;
		player.setScore(score);
		// make all temp. letter permanent
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (bo.isTempLtr(i, j)) {
					ch = bo.getBoard(i, j);
					ch = bo.convertTempToPerm(ch);
					bo.setBoard(ch, i, j);
				}
			}
		}
		while (bo.turnBlocs.getBlocCount() > 0) {
			// for each blank in turn, pop/add to permanent blank list
			loc = bo.turnBlocs.topBlankLoc();
			row = bo.turnBlocs.getBlankRow(loc);
			col = bo.turnBlocs.getBlankCol(loc);
			ch = bo.turnBlocs.topBlankVal();
			bo.turnBlocs.popBlank();
			bo.boardBlocs.pushBlank(row, col, ch);
		}
		bo.isFirstMove = false;
		bo.zeroPassCount();
		player.rack.fillRack();
		nextPlayer(true);
	}
	
	private void passOrMulti() {
		// user is passing/scrabbling or 
		//   putting back one letter (3 or 4 of kind)
		int kindCount, rptCount;
		if (bo.xBlocs.getBlocCount() > 0) {
			System.out.println("Error: you cannot interchange blank(s).");
			return;
		}
		if (bo.is3kind) {
			kindCount = 3;
		}
		else if (bo.is4kind) {
			kindCount = 4;
		}
		else {
			// force next if-stmt to fail,
			// user not prompted
			kindCount = BAGSIZ + 1;
		}
		rptCount = getRptCount();
		if (rptCount >= kindCount && getYNflag("Repeats detected: too many of those? ")) {
			handleRepeats(rptCount, kindCount);
			return;
		}
		// player is passing/scrabbling:
		passPlayer();
	}
	
	private void passPlayer() {	
		// player is passing/scrabbling:
		int inCount, outCount;
		char ch;

		player = bo.getCurrPlayer();
		// fill rack, get no. of new letters on rack
		inCount = player.rack.fillRack();  
		outCount = bo.getTempLtrCount();  // no. of temp. letters placed on board
		if (inCount == 0 && outCount == 0 && bo.getBagSize() == 0) {
			// user passes at end of game, no letters in play
			bo.incPassCount();
		}
		if (inCount >= outCount) {
			for (int i=0; i < BOARDHEIGHT; i++) {
				for (int j=0; j < BOARDWIDTH; j++) {
					if (bo.isTempLtr(i, j)) {
						// move letter placed on board to bag
						ch = bo.getBoard(i, j);
						ch = bo.convertBoardToRack(ch);
						bo.pushBag(ch);
						bo.setBoard(EMPTYCH, i, j);
					}
				}
			}
		}
		else {
			// due to lack of letters in bag, 
			// not all letters placed on board can be returned to bag,
			// instead they must be returned to rack
			for (int i=0; i < BOARDHEIGHT; i++) {
				for (int j=0; j < BOARDWIDTH; j++) {
					if (bo.isTempLtr(i, j)) {
						ch = bo.getBoard(i, j);
						ch = bo.convertBoardToRack(ch);
						if (--inCount >= 0) {
							bo.pushBag(ch);
						}
						else {
							player.rack.appendRackChar(ch);
						}
						bo.setBoard(EMPTYCH, i, j);
					}
				}
			}
		}
		nextPlayer(false);
	}
	
	private void nextPlayer(boolean isWordMade) {
		int playerNo;
		
		playerNo = bo.getCurrPlayerNo();
		if (!isWordMade) {
			player.setTurnScore(0);
		}
		// clear 2 lists of blanks: interchanged and placed on board
		bo.turnBlocs.clrStk();
		bo.xBlocs.clrStk();
		if (bo.getBagSize() > 0) {}
		else if (player.rack.getRackLen() == 0) {
			// player has gone out
			bo.setOutPlayerNo(playerNo);
			gameOver();
			return;
		}
		else if (bo.getPassCount() >= bo.getPlayerCount()) {
			// bag empty, all players have passed
			gameOver();
			return;
		}
		for (;;) {
			playerNo = (playerNo + 1) % bo.getPlayerCount();
			bo.setCurrPlayerNo(playerNo);
			player = bo.getCurrPlayer();
			// skip inactive players
			if (!player.isActive()) {
				continue;
			}
			// skip players who unsuccessfully challenged
			if (!player.isSkip()) {
				break;
			}
			player.setSkip(false);
		}
		bo.showBoard();
	}
	
	private int getChallengeLoser() {
		// evaluate status of challenge made
		// return playerNo of losing challenger
		// return curr. playerNo if challenger wins
		// return -1 on error
		String s;
		int loserNo;
		int playerNo = bo.getCurrPlayerNo();
		StringBuilder badWrdLst = new StringBuilder("");
		
		bo.calcTurnScore(badWrdLst, true);  // calc. score, get invalid words
		if (badWrdLst.length() > 0) {
			System.out.println("Invalid word(s) found: [" + badWrdLst + "]");
			System.out.println("");
			return playerNo;
		}
		s = getCmdString("Please enter player no. of challenger:");
		try {
			// no. entered is one-based, playerNo var. is zero-based
			loserNo = Integer.parseInt(s);
		}
		catch (NumberFormatException exc) {
			loserNo = 0;
		}
		if (loserNo <= 0 || loserNo > bo.getPlayerCount() || loserNo == (playerNo + 1)) {
			return -1;
		}
		return loserNo - 1;
	}
	
	private void cancelWord() {
		cancelWordRtn();
		bo.showBoard();
	}
	
	private void cancelWordRtn() {
		int pos, loc;
		int row, col;
		char ch;
		boolean isFirst = true;
		
		player = bo.getCurrPlayer();
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
					if (isFirst) {
						// set curr. board pos. to first letter moved
						bo.setBrdPos(i, j);
					}
					isFirst = false;
				}
			}
		}
		while (bo.xBlocs.getBlocCount() > 0) {
			// un-swap blank:
			ch = bo.xBlocs.topBlankVal();
			loc = bo.xBlocs.topBlankLoc();
			row = bo.xBlocs.getBlankRow(loc);
			col = bo.xBlocs.getBlankCol(loc);
			bo.setBoard(ch, row, col);
			bo.boardBlocs.pushBlank(row, col, ch);
			pos = player.rack.getCharPos(EMPTYCH);
			if (pos >= 0) {
				player.rack.setRackChar(pos, ch);
			}
			bo.xBlocs.popBlank();
		}
		bo.turnBlocs.clrStk();
	}
	
	private void backSpace() {
		// move letter most recently placed on board to rack
		char ch;
		player = bo.getCurrPlayer();
		for (int i = BOARDHEIGHT - 1; i >= 0; i--) {
			for (int j = BOARDWIDTH - 1; j >= 0; j--) {
				if (bo.isTempLtr(i, j)) {
					ch = bo.getBoard(i, j);
					ch = bo.convertBoardToRack(ch);
					player.rack.appendRackChar(ch);
					bo.setBoard(EMPTYCH, i, j);
					bo.setBrdPos(i, j);
					bo.showBoard();
					return;
				}
			}
		}
	}
	
	private void gameOver() {
		int valueSum = 0;
		int valueRack;
		char ch;
		int score;
		int outPlayerNo = bo.getOutPlayerNo();
		int hiScore = -9999;
		String name;
		
		bo.setGameOver(true);
		for (int i=0; i < bo.getPlayerCount(); i++) {
			// for all players who didn't go out:
			if (i == outPlayerNo) {
				continue;
			}
			player = bo.getPlayer(i);
			valueRack = 0;
			// add up values of letters on rack
			for (int j=0; j < player.rack.getRackLen(); j++) {
				ch = player.rack.getRackChar(j);
				valueRack += bo.getChrVal(ch);
			}
			// subtract sum from score
			score = player.getScore() - valueRack;
			player.setScore(score);
			// calc. grand total
			valueSum += valueRack;
		}
		if (outPlayerNo >= 0) {
			player = bo.getPlayer(outPlayerNo);
			// add grand total to score of player who went out
			score = player.getScore() + valueSum;
			player.setScore(score);
		}
		// find score of winning player(s)
		for (int i=0; i < bo.getPlayerCount(); i++) {
			player = bo.getPlayer(i);
			if (player.getScore() > hiScore) {
				hiScore = player.getScore(); 
			}
		}
		// display names/scores of winning player(s)
		for (int i=0; i < bo.getPlayerCount(); i++) {
			player = bo.getPlayer(i);
			if (player.getScore() == hiScore) {
				name = player.getName();
				System.out.println("Winner: " + name + " = " + hiScore);
			}
		}
		// display names/scores of losing player(s)
		for (int i=0; i < bo.getPlayerCount(); i++) {
			player = bo.getPlayer(i);
			if (player.getScore() < hiScore) {
				name = player.getName();
				System.out.println("Player: " + name + " = " + player.getScore());
			}
		}
	}
	
	private void handleRepeats(int rptCount, int kindCount) {
		// user has 3 or 4 of kind (or more)
		int bagCount = bo.getBagSize();
		char ch;
		
		if (bagCount <= 0) {
			System.out.println("Bag is empty: cannot put letter(s) back.");
			cancelWordRtn();
			return;
		}
		if (rptCount < kindCount) {
			System.out.println("You don't have enough of those.");
			return;
		}
		rptCount -= kindCount - 1;
		if (rptCount > bagCount) {
			rptCount = bagCount;
		}
		player = bo.getCurrPlayer();
		for (int i=0; i < rptCount; i++) {
			player.rack.bagToRack();
		}
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (bo.isTempLtr(i, j)) {
					// move letter placed on board to bag
					ch = bo.getBoard(i, j);
					// convert to upper case
					ch = bo.convertBoardToRack(ch);
					bo.pushBag(ch);
					bo.setBoard(EMPTYCH, i, j);
					rptCount--;
					if (rptCount <= 0) {
						cancelWord();
						return;
					}
				}
			}
		}
		cancelWord();
	}
	
	private int getRptCount() {
		// if all letters place on board alike, return count
		// else return 1
		int count = 1;
		char ch = ' ';
		
		for (int i=0; i < BOARDHEIGHT; i++) {
			for (int j=0; j < BOARDWIDTH; j++) {
				if (!bo.isTempLtr(i, j)) { }
				else if (ch == ' ') {
					ch = bo.getBoard(i, j);
				}
				else if (ch == bo.getBoard(i, j)) {
					count++;
				}
				else {
					return 1;
				}
			}
		}
		return count;
	}

	private void lookupWord(String word) {
		String msg;
		if (!bo.getUseDict()) {
			System.out.println("Looking up words not allowed.");
			return;
		}
		if (bo.lookupWord(word)) {
			msg = "is in dictionary.";
		}
		else {
			msg = "not found.";
		}
		System.out.println("'" + word + "' " + msg);
	}
	
	private void makeWord(String word) {
		// user enters:
		//   w cat (word equals "cat")
		//   w cat. s (where . is a blank equal to s, any punct. mark can take place of .)
		// place cat or cats on board
		
		char ch;
		String cmdarg = "";  // only handles zero/one blank(s)
		int j;
		int racklen;
		
		player = bo.getCurrPlayer();
		if (word.equals("")) {
			word = getCmdString("Enter word:");
		}
		word = word.trim();
		word = word.toUpperCase();
		j = word.indexOf(' ');
		if (j >= 0) {
			ch = word.charAt(j + 1);
			if (Character.isAlphabetic(ch)) {
				cmdarg = "" + ch;  // value of blank
			}
		}
		racklen = player.rack.getRackLen();
		for (int i=0; i < word.length(); i++) {
			if (i >= racklen) {
				break;
			}
			ch = word.charAt(i);
			if (!Character.isAlphabetic(ch)) {
				if (ch == ' ') {
					break;
				}
				if (cmdarg.length() == 0) {
					continue;
				}
				ch = EMPTYCH;
			}
			// ch is a letter (or blank if ch was not a letter)
			j = player.rack.getCharPos(ch);
			if (j < 0) {
				break;
			}
			player.rack.setRackPos(j);
			// move curr. rack letter to board
			rackClickRtn(cmdarg);
		}
		bo.showBoard();
	}
	
	private void wordOnRack(String word) {
		// user enters:
		//   wr cat (word equals "cat")
		//   wr rob.n (word equals "robin", where . is a blank equal to i)
		//   wr mo se (word equals "mouse", where 3rd letter is a blank equal to u)
		// shuffle letters on rack, begins with cat, robin, or mouse
		char ch;
		String buf = "";
		int bitmask = 0;
		int bitcurr;
		int pos, i, k;
		int racklen;
		
		player = bo.getCurrPlayer();
		if (word.equals("")) {
			word = getCmdString("Enter word:");
		}
		word = word.trim();
		word = word.toUpperCase();
		racklen = player.rack.getRackLen();
		for (i=0; i < word.length(); i++) {
			if (i >= racklen) {
				break;
			}
			ch = word.charAt(i);
			if (!Character.isAlphabetic(ch)) {
				ch = EMPTYCH;
			}
			pos = 0;
			k = 0;
			do {
				pos = player.rack.getCharPosNext(ch, k);
				if (pos < 0) {
					break;
				}
				bitcurr = 1 << pos;
				if ((bitcurr & bitmask) == 0) {
					bitmask |= bitcurr;
					break;
				}
				k = pos + 1;
			} while (k < racklen);
			if (pos < 0 || k >= racklen) {
				continue;
			}
			buf += ch;
		}
		i = 0;
		k = 0;
		while (bitmask != 0) {
			bitcurr = bitmask % 2;
			if (bitcurr == 1) {
				ch = player.rack.getRackChar(k);
				player.rack.setRackChar(k, player.rack.getRackChar(i));
				player.rack.setRackChar(i, ch);
				i++;
			}
			bitmask >>>= 1;
			k++;
		}
		player.rack.shuffleRack(buf);
		bo.showBoard();
	}
	
}

interface IConst {
	int BOARDWIDTH = 15;
	int BOARDHEIGHT = 15;
	int RACKSIZ = 7;
	int BONUS7LTRWRD = 50;
	char EMPTYCH = ' ';
	char BRDSPCH = '.';
	char BRDBLCH = '*';
	char BRDTMPCH = '$';
	char BRD2LCH = '2';
	char BRD3LCH = '3';
	char BRD2WCH = '4';
	char BRD3WCH = '9';
	//int TMPBAGSIZ = 29;
	int TMPBAGSIZ = 40;
	int BAGSIZ = 100;
	//int BAGSIZ = TMPBAGSIZ;
	int ALPHASIZ = 26;
	int BLSTKSIZ = 2;
	int DEFPLAYTABSIZ = 2;
	int MAXPLAYTABSIZ = 4;  // max. no. of players
	char DITTOCH = '.';
	char USEIDXCH = '-';
	char ROBOTCH = '^';
	char PTRCH = '^';
	int BRDACROSS = 0;
	int BRDDOWN = 1;
	//int BRDNULL = 2;
	//int BRDXMODE = 3;
	int MODECOUNT = 2;  // used to be 4
	char ACROSSCH = '>';
	char DOWNCH = '^';
	char XMODECH = 'X';
	String DEFPROMPT = "> ";
}

class Board implements IConst {
		
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
	private Dict dict;
	private boolean isUseDict = false;
	public boolean isFirstMove;
	private int playerCount;
	private Player[] playerTab = new Player[MAXPLAYTABSIZ];
	private boolean rackMode;
	private boolean swapMode;
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
	
	Board(String[] args) {
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
			errmsg = "Fail: testBagOps";
		}
		playerCount = getNewPlayerCount(true, args);
		initPlayerNo();
		dict = new Dict();
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
		rackMode = true;
		swapMode = false;
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
		initBag();
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
			bag[k] = EMPTYCH;
			blankCount++;
		}
		// return false on error
		return (blankCount == BLSTKSIZ);
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
		// display char. of cell on board
		if (isBrdBlank(i, j)) {
			return BRDBLCH;
		}
		if (board[i][j] != EMPTYCH) {
			return board[i][j];
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
					// previous cell not succeeded by ']'
					boardCell = " " + boardCell;
				}
				else if (j <= BOARDWIDTH - 1) {
					isShortCell = false;
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
			if (i == (playerCount + 1)) {
				System.out.print(sepBoardRack + "________|");
			}
			if (i == (playerCount + 2)) {
				System.out.print(sepBoardRack + "Wrd Tot" + sepRackNames);
				playerNo = playerCount;
			}
			if ((i > (playerCount + 2)) && (playerNo < (2 * playerCount))) {
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
		if (i < 0 || i > 16) {
			return EMPTYCH;
		}
		if (i < 10) {
			return (char)('0' + i);
		}
		return (char)('A' + i - 10);
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
		System.out.println("Bag word = [" + wordstr + ']');
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
				return -1;  // don't change player names
			}
			argCount = newArgs.length;
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

class Player implements IConst {

	private String name;
	private int score;
	private int turnScore;
	private boolean robot;
	private boolean active;
	private boolean skip;
	private static String noNamePlayer = "player";
	private static String robotPlayer = "robot";
	public Rack rack;
	private Board bo;
	
	Player(Board bo) {
		this.bo = bo;
		clearPlayer();
	}

	public void clearPlayer() {
		name = "";
		score = 0;
		turnScore = 0;
		robot = false;
		active = false;
		skip = false;
		rack = new Rack(bo);
	}

	public void restartPlayer() {
		score = 0;
		turnScore = 0;
		active = true;
		skip = false;
	}

	public void setPlayer(String name, int idx, boolean robot) {
		if (idx == 0) {
			this.name = name;
		}
		else if (robot) {
			this.name = robotPlayer + idx;
		}
		else {
			this.name = noNamePlayer + idx;
		}
		this.robot = robot;
		active = true;
		skip = false;
		rack.fillRack();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isRobot() {
		return robot;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean flag) {
		active = flag;
	}
	
	public boolean isSkip() {
		return skip;
	}
	
	public void setSkip(boolean flag) {
		skip = flag;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public int getTurnScore() {
		return turnScore;
	}
	
	public void setTurnScore(int score) {
		turnScore = score;
	}
	
}

class Rack implements IConst {
	
	private static final char EMPTYCH = ' ';
	private static final char OUTBLANKCH = '*';
	private Board bo;
	private char[] rackbuf;
	private int rackLen;
	private int rackPos;

	Rack(Board bo) {
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

}

class BlankLocs {
	
	private static final int BLOCSIZ = 2;
	private static final char EMPTYCH = ' ';
	private int[] blocs;      // location(s) of blank(s)
	private int bloccount;    // no. of blanks
	private char[] blankStr;  // value(s) of blank(s)
	
	BlankLocs() {
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
