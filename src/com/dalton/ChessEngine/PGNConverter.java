package com.dalton.ChessEngine;

import static com.dalton.ChessEngine.Types.*;
/**
 * Decodes PGN notation and generates PGN notation from game history
 * Designed to be small and efficient
 * @author Dalton Herrewynen
 * @version 0
 */
public class PGNConverter{
	/**
	 * Calculates what square a move started from
	 * @param board  The board after the move
	 * @param PGN    The PGN move
	 * @param player WHITE or BLACK
	 * @return The index where the move started on (0-63)
	 */
	public static int getStartSquare(Board board, String PGN, boolean player){
		return -1;
	}

	/**
	 * Converts the coordinate part of the PGN move into the destination square
	 * @param PGN The PGN move
	 * @return The destination index (0-63)
	 */
	public static int getDestSquare(String PGN){
		return -1;
	}
}
