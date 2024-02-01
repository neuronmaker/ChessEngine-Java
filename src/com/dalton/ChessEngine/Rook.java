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
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position Where the Rook is on the board
	 * @return Relative value for AI
	 */
	//TODO finish this
	@Override
	public int pieceValue(final long enemies,final long blanks,final int position){
		int score=500;
		ArrayList<Integer> moves=new ArrayList<>();
		getMoves(moves,enemies,blanks,position);
		score+=moves.size()*10;
		return score;
	}

	/**
	 * The move generator method for Rook
	 * @param moves    Reference to the Move list
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @return an ArrayList of integers which encode all the relevant move data for each move
	 */
	@Override
	public void getMoves(ArrayList<Integer> moves,final long enemies,final long blanks,int position){
		HVLineCheck(moves,enemies,blanks,position);
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