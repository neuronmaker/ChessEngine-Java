package com.dalton.ChessEngine;

import java.util.Stack;

/**
 * The Game controller
 * @author Dalton Herrewynen
 * @version 0
 */
public class GameController{
	Stack<BoardState> States;
	Stack<BoardState> UndoneStates;
	Board board;
	boolean IsWhiteTurn=true;

}
