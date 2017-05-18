package scrab;

import iconst.IConst;
import board.Board;
import player.Player;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class Scrab implements IConst {

	private String[] args;
	
	public Scrab(String[] args) {
		this.args = args;
	}
	
	public void runScrab() {
		try {
			scrabMain(args);
		}
		catch (IOException exc) {
			//
		}
	}
	
	private void scrabMain(String[] args)
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
		
		cmdarg = getCmdChArg(cmdbuf);
		ch = cmdarg.charAt(0);
		if (cmdarg.length() > 1) {
			cmdbuf = cmdarg;
		}
		cmdarg = getCmdArg(cmdbuf);
		switch (ch) {
		case BADCMDCH:
			break;
		case 'r':
			restartGame(cmdarg);
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
		case 'v':
			togglePipeMode();
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
		case 'I':
			goToRow(cmdarg);
			break;
		case 'J':
			goToCol(cmdarg);
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
		System.out.println("v - toggle pipe mode");
		System.out.println("i0 - up arrow/go to row");
		System.out.println("m0 - down arrow/go to row");
		System.out.println("j0 - left arrow/go to column");
		System.out.println("k0 - right arrow/go to column");
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
		System.out.println("v         : turn off for cleaner board display");
	}
	
	public String getCmdChArg(String cmdbuf) {
		// return first char. of command line on success
		// special cases : return value
		//   i0          : I 0
		//   k9          : J 9
		//   wr myword   : W
		//   wr          : W
		//   help        : h
		int n;
		char ch = cmdbuf.charAt(0);
		String buf = cmdbuf.substring(1);
		
		if (cmdbuf.length() == 1) {
			return "" + ch;
		}
		if (cmdbuf.startsWith("i") || cmdbuf.startsWith("m")) {
			return "I " + buf;
		}
		if (cmdbuf.startsWith("j") || cmdbuf.startsWith("k")) {
			return "J " + buf;
		}
		if (cmdbuf.startsWith("wr")) {
			if (cmdbuf.length() > 2 && cmdbuf.charAt(2) != ' ') {
				return "" + BADCMDCH; 
			}
			return "W";
		}
		if (cmdbuf.equals("help")) {
			return "h";
		}
		n = cmdbuf.indexOf(' ');
		if (n < 0 || n > 1) {
			return "" + BADCMDCH;
		}
		return "" + ch;
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
			if (inbuf.length() > 1) { }
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
		
	public void putOkCmdString(String msg) {
		System.out.println(msg);
		System.out.print("OK");
		System.out.print(DEFPROMPT);
		getReadLine();
	}
		
	private void restartGame(String cmdarg) {
		bo.restartGame(cmdarg);
		player = bo.getCurrPlayer();
		if (player.isRobot()) {
			nextPlayer(true);
		}
		else {
			bo.showBoard();
		}
		isNewGame = true;
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
	
	private void togglePipeMode() {
		bo.togglePipeMode();
		bo.showBoard();
	}
	
	private void goToRow(String buf) {
		int col = bo.getBrdCol();
		int destRow = bo.hexDigToInt(buf);

		if (bo.isWordOnBoard()) {
			return;
		}
		if (destRow < 0) {
			return;
		}
		bo.setBrdPos(destRow, col);
		bo.showBoard();
	}
	
	private void goToCol(String buf) {
		int row = bo.getBrdRow();
		int destCol = bo.hexDigToInt(buf);

		if (bo.isWordOnBoard()) {
			return;
		}
		if (destCol < 0) {
			return;
		}
		bo.setBrdPos(row, destCol);
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
		int turnScore;
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
		setScoreWoNext(turnScore);
		nextPlayer(true);
	}

	private void setScoreWoNext(int turnScore) {
		int score;
		char ch;
		int row, col;
		int loc;
		boolean forceBlanks = false;  // just for debug!

		if (turnScore > NEGSCORE) {
			player.setTurnScore(turnScore);
			score = player.getScore() + turnScore;
			player.setScore(score);
		}
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
		bo.out("setSWN: top while");
		while (bo.turnBlocs.getBlocCount() > 0) {
			// for each blank in turn, pop/add to permanent blank list
			loc = bo.turnBlocs.topBlankLoc();
			row = bo.turnBlocs.getBlankRow(loc);
			col = bo.turnBlocs.getBlankCol(loc);
			ch = bo.turnBlocs.topBlankVal();
			bo.out("setSWN: while, ch = ["+ch+"]");
			bo.turnBlocs.popBlank();
			bo.boardBlocs.pushBlank(row, col, ch);
		}
		bo.out("setSWN: btm while");
		bo.isFirstMove = false;
		bo.zeroPassCount();
		player.rack.fillRack();
		if (!player.isRobot() || !forceBlanks) {
			return;
		}  // debug only:
		player.rack.setBlanks(turnScore % 3);
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
		int score;
		
		do {
			playerNo = bo.getCurrPlayerNo();
			if (player.isRobot()) {
				bo.showBoard();
				if (!getYNflag("Robot's turn: Keep playing? ")) {
					if (getYNflag("Quit game: Are you sure? ")) {
						gameOver();
						return;
					}
				}
				score = bo.rp.doRobotsTurn(playerNo);
				setScoreWoNext(score);
				isWordMade = true;
			}
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
		} while (player.isRobot());
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
		if (bo.isWordOnBoard()) {
			boardClick();
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
		bo.showBoard();
		putOkCmdString("Game Over");
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
