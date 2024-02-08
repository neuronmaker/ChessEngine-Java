/*
Project: Chess Engine
File: Queen.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.PieceCode.*;
import static com.dalton.ChessEngine.Types.*;

/**
 * Class for the Queen piece
 * @author Dalton Herrewynen
 * @version 3
 */
public class Queen extends Piece{
	/**
	 * Only constructor requires the setting of team (color)
	 * @param team WHITE or BLACK
	 */
	public Queen(boolean team){
		super((team==WHITE)? QueenW : QueenB);
	}

	/**
	 * Converts the Queen to a human-readable String containing its internal state
	 * @return String representation of internal state
	 */
	@Override
	public String toString(){
		return decodeTeamString(pieceCode)+" Queen";
	}

	/**
	 * Placeholder for the incomplete method
	 * @param enemies Mask of enemy squares
	 * @param blanks  Mask of blank squares
	 * @param pos     Where the piece is on the board
	 * @return The relative score for the AI
	 */
	@Override
	public int pieceValue(final long enemies,final long blanks,final int pos){
		int score=2000;
		int moveCount=Coord.maskCount(attackMask(enemies,blanks,pos));
		score+=moveCount*15;
		return score;
	}

	/**
	 * The move generator method for Queen
	 * @param moves    Reference to the Move list
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position The position index to check from
	 */
	@Override
	public void getMoves(ArrayList<Integer> moves,final long enemies,final long blanks,final int position){//todo idea: split into table of rays and generate all rays with all blocked lengths
		//Bishop moves
		diagLineCheck(moves,enemies,blanks,position);
		//Rook moves
		HVLineCheck(moves,enemies,blanks,position);
	}

	/**
	 * Get the mask of squares the Queen can attack
	 * @param enemies Mask of enemies to capture
	 * @param blanks  Mask of blank squares
	 * @param pos     The integer position index
	 * @return a 64 bit integer bit mask
	 */
	@Override
	public long attackMask(final long enemies,final long blanks,final int pos){
		long mask=PieceCode.pieceObj(PieceCode.encodeChar('b',team)).attackMask(enemies,blanks,pos);//get Bishop mask
		mask|=PieceCode.pieceObj(PieceCode.encodeChar('r',team)).attackMask(enemies,blanks,pos);//get Rook mask
		return mask;
	}
}
