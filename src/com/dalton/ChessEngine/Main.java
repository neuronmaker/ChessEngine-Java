package com.dalton.ChessEngine;
/**
 * The central boot loading class for this project
 * @author Dalton Herrewynen
 * @version 1
 */
public class Main{
	public static void main(String[] args){
		GameController game=new GameController();
		game.startPrimaryLoop();
		/*game.makeAiMove();
		game.flipPlayer();
		game.makeAiMove();
		game.flipPlayer();
		game.makeAiMove();
		game.flipPlayer();
		game.makeAiMove();
		game.flipPlayer();*/
		game.printHistory();
	}
}