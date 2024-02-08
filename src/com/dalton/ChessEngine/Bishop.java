/*
Project: Chess Engine
File: Bishop.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

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
	 * Calculates the Bishop's value to the AI player
	 * @param enemies Mask of enemy squares
	 * @param blanks  Mask of blank squares
	 * @param pos     Where this Piece is located on the board
	 * @return relative value to the AI
	 */
	@Override
	public int pieceValue(final long enemies,final long blanks,final int pos){
		int score=300;
		int moveCount=Coord.maskCount(attackMask(enemies,blanks,pos));
		score+=moveCount*10;
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
	 * Get the mask of squares the Bishop can attack
	 * @param enemies Mask of enemies to capture
	 * @param blanks  Mask of blank squares
	 * @param pos     The integer position index
	 * @return a 64 bit integer bit mask
	 */
	@Override
	public long attackMask(final long enemies,final long blanks,final int pos){
		long mask=0;
		final int x=Coord.indexToX(pos);
		//North West
		int i=pos+NW;//iterate ahead of position
		for(; Coord.indexToX(i)<BOARD_SIZE && Coord.indexToX(i)>x && i<TOTAL_SQUARES && (0!=(blanks & (1L << i))); i+=NW){
			mask|=1L<<i;//while blank, add to mask
		}
		if(Coord.indexToX(i)<BOARD_SIZE && Coord.indexToX(i)>x && i<TOTAL_SQUARES && (0!=(enemies & (1L << i)))){
			mask|=1L<<i;//then add the capture
		}

		//North East
		i=pos+NE;//iterate ahead of position
		for(; Coord.indexToX(i)>=0 && Coord.indexToX(i)<x && i<TOTAL_SQUARES && (0!=(blanks & (1L << i))); i+=NE){
			mask|=1L<<i;
		}
		if(Coord.indexToX(i)>=0 && Coord.indexToX(i)<x && i<TOTAL_SQUARES && (0!=(enemies & (1L << i)))){
			mask|=1L<<i;
		}

		//South East
		i=pos+SE;//iterate ahead of position
		for(; Coord.indexToX(i)>=0 && Coord.indexToX(i)<x && i>=0 && (0!=(blanks & (1L << i))); i+=SE){
			mask|=1L<<i;
		}
		if(Coord.indexToX(i)>=0 && Coord.indexToX(i)<x && i>=0 && (0!=(enemies & (1L << i)))){
			mask|=1L<<i;
		}

		//South West
		i=pos+SW;//iterate ahead of position
		for(; Coord.indexToX(i)<BOARD_SIZE && Coord.indexToX(i)>x && i>=0 && (0!=(blanks & (1L << i))); i+=SW){
			mask|=1L<<i;
		}
		if(Coord.indexToX(i)<BOARD_SIZE && Coord.indexToX(i)>x && i>=0 && (0!=(enemies & (1L << i)))){
			mask|=1L<<i;
		}
		return mask;
	}
}