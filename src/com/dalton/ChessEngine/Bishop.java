/*
Project: Chess Engine
File: Bishop.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;

import java.util.ArrayList;


/**
 * Class for the Bishop piece
 * @author Dalton Herrewynen
 * @version 3
 */
public class Bishop extends Piece{
	/**
	 * Sets the Bishop alliance
	 * @param team WHITE or BLACK
	 */
	public Bishop(boolean team){
		super((team==Types.WHITE)? PieceCode.BishopW : PieceCode.BishopB);
	}

	/**
	 * Converts the Bishop to a human-readable String containing its internal state
	 * @return String representation of internal state
	 */
	@Override
	public String toString(){
		return PieceCode.decodeTeamString(pieceCode)+" Bishop";
	}

	/**
	 * Calculates the Piece's value to the AI player
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position Where this Piece is located on the board
	 * @return relative value to the AI
	 */
	@Override
	public int pieceValue(final long enemies,final long blanks,final int position){
		int score=300;
		ArrayList<Integer> moves=new ArrayList<>();
		getMoves(moves,enemies,blanks,position);
		score+=moves.size()*10;
		return score;
	}

	/**
	 * Generates moves for the Bishop Piece
	 * @param moves    Reference to the Move list
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position The position index to check from
	 */
	@Override
	public void getMoves(ArrayList<Integer> moves,final long enemies,final long blanks,final int position){
		diagLineCheck(moves,enemies,blanks,position);
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