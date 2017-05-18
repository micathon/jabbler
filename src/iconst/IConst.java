package iconst;

public interface IConst {
	boolean debug = false;
	int BOARDWIDTH = 15;
	int BOARDHEIGHT = 15;
	int RACKSIZ = 7;
	int BONUS7LTRWRD = 50;
	int NEGSCORE = -99999;
	char EMPTYCH = ' ';
	char BRDSPCH = '.';
	char BRDLWCH = '+';
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
	int BLHARDSIZ = 2;
	//int BLSTKSIZ = BLHARDSIZ; 
	int BLSTKSIZ = 20;
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
