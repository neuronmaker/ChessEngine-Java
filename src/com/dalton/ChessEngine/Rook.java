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
	 * @param enemies Mask of enemy squares
	 * @param blanks  Mask of blank squares
	 * @param pos     Where the Rook is on the board
	 * @return Relative value for AI
	 */
	//TODO finish this
	@Override
	public int pieceValue(final long enemies,final long blanks,final int pos){
		int score=500;
		int moveCount=Coord.maskCount(attackMask(enemies,blanks,pos));
		score+=moveCount*10;
		return score;
	}

	/**
	 * The move generator method for Rook
	 * @param moves    Reference to the Move list
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position The position index to check from
	 */
	@Override
	public void getMoves(ArrayList<Integer> moves,final long enemies,final long blanks,int position){
		HVLineCheck(moves,enemies,blanks,position);
	}

	/**
	 * Get the mask of squares the rook can attack
	 * @param enemies Mask of enemies to capture
	 * @param blanks  Mask of blank squares
	 * @param pos     The integer position index
	 * @return a 64-bit integer bit mask
	 */
	@Override
	public long attackMask(final long enemies,final long blanks,final int pos){
		long mask=0;
		int x=Coord.indexToX(pos), y=Coord.indexToY(pos);
		//horizontal moves
		int i=pos-1;//start behind the position,
		for(; i>=Coord.XYToIndex(0,y) && (0!=(blanks & (1L << i))); --i){//and then move backwards until end of row or a non-blank square
			mask|=1L<<i;
		}
		if(i>=Coord.XYToIndex(0,y) && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			mask|=1L<<i;
		}
		i=pos+1;//now check from 1 in front of the position
		for(; i<Coord.XYToIndex(BOARD_SIZE,y) && (0!=(blanks & (1L << i))); ++i){
			mask|=1L<<i;
		}
		if(i<Coord.XYToIndex(BOARD_SIZE,y) && (0!=(enemies & (1L << i)))){
			mask|=1L<<i;
		}

		//vertical moves
		i=pos-BOARD_SIZE;
		for(; i>=Coord.XYToIndex(x,0) && (0!=(blanks & (1L << i))); i-=BOARD_SIZE){
			mask|=1L<<i;
		}
		if(i>=Coord.XYToIndex(x,0) && (0!=(enemies & (1L << i)))){
			mask|=1L<<i;
		}
		i=pos+BOARD_SIZE;//now check from 1 above of the position
		for(; i<Coord.XYToIndex(x,BOARD_SIZE) && (0!=(blanks & (1L << i))); i+=BOARD_SIZE){
			mask|=1L<<i;
		}
		if(i<Coord.XYToIndex(x,BOARD_SIZE) && (0!=(enemies & (1L << i)))){
			mask|=1L<<i;
		}
		return mask;
	}
}