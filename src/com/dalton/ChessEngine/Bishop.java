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
		ArrayList<Integer> moves=getMoves(enemies,blanks,position);
		score+=moves.size()*10;
		return score;
	}

	/**
	 * Generates moves for the Bishop Piece
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position The position index to check from
	 * @return an ArrayList of integers which encode all the relevant move data for each move
	 */
	@Override
	public ArrayList<Integer> getMoves(final long enemies,final long blanks,final int position){
		return diagLineCheck(enemies,blanks,position);
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