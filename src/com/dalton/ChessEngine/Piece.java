package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

/**
 * My own version of the Piece super class
 * @author Dalton Herrewynen
 * @version 1
 */
public abstract class Piece{
	/** Directions for line diagonal line checks */
	public static final int NE=Coord.NE, NW=Coord.NW, SE=Coord.SE, SW=Coord.SW;
	public final boolean team;
	public final int pieceCode;

	public Piece(int givenCode){
		team=PieceCode.decodeTeam(givenCode);
		pieceCode=givenCode;
	}

	public abstract int pieceValue(final long enemies,final long blanks,final int position);
	public abstract ArrayList<Integer> getMoves(final long enemies,final long blanks,final int position);

	/**
	 * Checks a Horizontal and a Vertical line for sliding pieces
	 * @param enemies  A bitmask of the enemies on the board
	 * @param blanks   A bitmask of the blank squares
	 * @param position The current position of this piece
	 * @return A list of encoded Move integers
	 */
	protected ArrayList<Integer> HVLineCheck(final long enemies,final long blanks,final int position){
		ArrayList<Integer> moves=new ArrayList<>();
		int x=Coord.indexToX(position), y=Coord.indexToY(position);
		//horizontal moves
		int i=position-1;//start behind the position,
		for(; i>=Coord.XYToIndex(0,y) && (0!=(blanks & (1L << i))); --i){//and then move backwards until end of row or a non-blank square
			moves.add(Move.encodeNormal(pieceCode,position,i));//while blank, encode normal moves
		}
		if(i>=Coord.XYToIndex(0,y) && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			moves.add(Move.encode(Move.capture,pieceCode,position,i));//then make a capture
		}
		i=position+1;//now check from 1 in front of the position
		for(; i<Coord.XYToIndex(BOARD_SIZE,y) && (0!=(blanks & (1L << i))); ++i){//and then move forwards until end of row or a non-blank square
			moves.add(Move.encodeNormal(pieceCode,position,i));//while blank, encode normal moves
		}
		if(i<Coord.XYToIndex(BOARD_SIZE,y) && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			moves.add(Move.encode(Move.capture,pieceCode,position,i));//then make a capture
		}

		//vertical moves
		i=position-BOARD_SIZE;//start below the position (remember that to move index up 1 need to move over by 8)
		for(; i>=Coord.XYToIndex(x,0) && (0!=(blanks & (1L << i))); i-=BOARD_SIZE){//and then move downwards until end of row or a non-blank square
			moves.add(Move.encodeNormal(pieceCode,position,i));//while blank, encode normal moves
		}
		if(i>=Coord.XYToIndex(x,0) && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			moves.add(Move.encode(Move.capture,pieceCode,position,i));//if we found an enemy then make a capture
		}
		i=position+BOARD_SIZE;//now check from 1 above of the position
		for(; i<Coord.XYToIndex(x,BOARD_SIZE) && (0!=(blanks & (1L << i))); i+=BOARD_SIZE){//and then move upwards until end of row or a non-blank square
			moves.add(Move.encodeNormal(pieceCode,position,i));//while blank, encode normal moves
		}
		if(i<Coord.XYToIndex(x,BOARD_SIZE) && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			moves.add(Move.encode(Move.capture,pieceCode,position,i));//then make a capture
		}
		return moves;
	}

	/**
	 * Checks both diagonal lines for sliding pieces
	 * @param enemies  A bitmask of the enemies on the board
	 * @param blanks   A bitmask of the blank squares
	 * @param position The current position of this piece
	 * @return A list of encoded Move integers
	 */
	protected ArrayList<Integer> diagLineCheck(final long enemies,final long blanks,final int position){
		ArrayList<Integer> moves=new ArrayList<>();
		final int x=Coord.indexToX(position), y=Coord.indexToY(position);
		//North West
		int i=position+NW;//iterate ahead of position
		for(; Coord.indexToX(i)<BOARD_SIZE && Coord.indexToX(i)>x && i<TOTAL_SQUARES && (0!=(blanks & (1L << i))); i+=NW){//Move NW until off side edge (Coord.indexToX()) or off end (>TOTAL_SQUARES) or find a non-blank square
			moves.add(Move.encodeNormal(pieceCode,position,i));//while blank, encode normal moves
		}
		if(Coord.indexToX(i)<BOARD_SIZE && Coord.indexToX(i)>x && i<TOTAL_SQUARES && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			moves.add(Move.encode(Move.capture,pieceCode,position,i));//then make a capture
		}

		//North East
		i=position+NE;//iterate ahead of position
		for(; Coord.indexToX(i)>=0 && Coord.indexToX(i)<x && i<TOTAL_SQUARES && (0!=(blanks & (1L << i))); i+=NE){//Move NE until off side edge (Coord.indexToX()) or off end (>TOTAL_SQUARES) or find a non-blank square
			moves.add(Move.encodeNormal(pieceCode,position,i));//while blank, encode normal moves
		}
		if(Coord.indexToX(i)>=0 && Coord.indexToX(i)<x && i<TOTAL_SQUARES && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			moves.add(Move.encode(Move.capture,pieceCode,position,i));//then make a capture
		}

		//South East
		i=position+SE;//iterate ahead of position
		for(; Coord.indexToX(i)>=0 && Coord.indexToX(i)<x && i>=0 && (0!=(blanks & (1L << i))); i+=SE){//Move SE until off side edge (Coord.indexToX()) or off end (>TOTAL_SQUARES) or find a non-blank square
			moves.add(Move.encodeNormal(pieceCode,position,i));//while blank, encode normal moves
		}
		if(Coord.indexToX(i)>=0 && Coord.indexToX(i)<x && i>=0 && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			moves.add(Move.encode(Move.capture,pieceCode,position,i));//then make a capture
		}

		//South West
		i=position+SW;//iterate ahead of position
		for(; Coord.indexToX(i)<BOARD_SIZE && Coord.indexToX(i)>x && i>=0 && (0!=(blanks & (1L << i))); i+=SW){//Move SW until off side edge (Coord.indexToX()) or off end (>TOTAL_SQUARES) or find a non-blank square
			moves.add(Move.encodeNormal(pieceCode,position,i));//while blank, encode normal moves
		}
		if(Coord.indexToX(i)<BOARD_SIZE && Coord.indexToX(i)>x && i>=0 && (0!=(enemies & (1L << i)))){//if we found an enemy and the loop exited early
			moves.add(Move.encode(Move.capture,pieceCode,position,i));//then make a capture
		}

		return moves;
	}

	/**
	 * Get the mask of squares this piece can attack
	 * @param friends Mask of friendly units to mask out
	 * @param pos     The integer position index
	 * @return a 64 bit integer bit mask
	 */
	public abstract long attackMask(final long friends,final int pos);
}
