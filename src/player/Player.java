package player;

import iconst.IConst;
import board.Board;
import rack.Rack;

public class Player implements IConst {

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
	
	public Player(Board bo) {
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
