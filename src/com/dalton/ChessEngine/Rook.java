/*
Project: Chess Engine
File: Rook.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

/**
 * Class for the Rook piece
 * @author Dalton Herrewynen
 * @version 3
 */
public class Rook extends Piece{
	/**
	 * Constructs the Rook and sets the team (color, team, etc.)
	 * @param team WHITE or BLACK
	 */
	public Rook(boolean team){
		super((team==WHITE)? PieceCode.RookW : PieceCode.RookB);
	}

	/**
	 * Converts the Rook to a human-readable String containing its internal state
	 * @return String representation of internal state
	 */
	@Override
	public String toString(){
		return PieceCode.decodeTeamString(pieceCode)+" Rook";
	}

	/**
	 * Calculates the relative value of this piece, used for AI
	 * @param board    The board this Rook is on
	 * @param position Where the Rook is on the board
	 * @return Relative value for AI
	 */
	//TODO finish this
	@Override
	public int pieceValue(final Board board,final int position){
		ArrayList<Integer> moves=getMoves(board,position);
		int score=500;
		score+=moves.size()*10;
		return score;
	}

	/**
	 * The move generator method for Rook
	 * @param board    The current state of the board
	 * @param position The position index to check from
	 * @return an ArrayList of integers which encode all the relevant move data for each move
	 */
	@Override
	public ArrayList<Integer> getMoves(Board board,int position){
		long enemies=board.alliedPieceMask(!team);
		long blanks=~(enemies | board.alliedPieceMask(team));//add the enemies and friends together, invert to get blanks
		return HVLineCheck(enemies,blanks,position);
	}

	/**
	 * Get the mask of squares this piece can attack
	 * @param friends Mask of friendly units to mask out
	 * @param pos     The integer position index
	 * @return a 64 bit integer bit mask
	 */
	@Override
	public long attackMask(long friends,int pos){
		return 0;
	}
}