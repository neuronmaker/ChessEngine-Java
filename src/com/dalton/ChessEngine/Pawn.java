/*
Project: Chess Engine
File: Pawn.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

/**
 * Class for the Pawn piece
 * @author Dalton Herrewynen
 * @version 4
 */
public class Pawn extends Piece{
	/** Coefficient, used to mathematically compensate for Pawn direction, saves lots of conditional jumps */
	private final int deltaY;
	/** Used for this Pawn to know where it starts off */
	private final long startingRank;
	/** Bit masks used for fast detection of pawns that can be promoted */
	public static final long
			WHITE_Promotion_mask=0b0000000011111111000000000000000000000000000000000000000000000000L,
			BLACK_Promotion_mask=0b0000000000000000000000000000000000000000000000001111111100000000L;
	/** Bit masks used to detect if a pawn is in the position to EnPassant Capture */
	public static final long
			WHITE_EnPassant_mask=0b0000000000000000000000001111111100000000000000000000000000000000L,
			BLACK_EnPassant_mask=0b0000000000000000000000000000000011111111000000000000000000000000L;
	/** The scoring Look Up Table */
	private final int[] scoreLUT={
			0,10,20,50,50,20,10,0,//score table is subject to change and tweaks
			0,10,20,50,50,20,10,0,
			0,10,20,50,50,20,10,0,
			0,10,20,50,50,20,10,0,
			0,10,20,50,50,20,10,0,
			0,10,20,50,50,20,10,0,
			0,10,20,50,50,20,10,0,
			0,10,20,50,50,20,10,0,
	};
	/** For calculating the value of an advancing pawn, add this to the table rows */
	private static final int[] pawnAdvanceScore={0,0,10,20,50,70,90,200};//arbitrary score values, subject to change

	/**
	 * Constructs the Pawn and sets the color
	 * @param team WHITE or BLACK
	 */
	public Pawn(boolean team){
		super((team==WHITE)? PieceCode.PawnW : PieceCode.PawnB);
		deltaY=(team==WHITE)? 1 : -1;//WHITE pawns move up, BLACK pawns move down
		startingRank=(team==WHITE)? BLACK_Promotion_mask : WHITE_Promotion_mask;//starting masks are opposite of promotion masks
		//add some value to each row so that advancing pawns get more valuable
		for(int y=0; y<BOARD_SIZE; ++y)
			for(int x=0; x<BOARD_SIZE; ++x){//add the pawn advancing scores to the Look-Up Table, row by row
				if(team==WHITE) scoreLUT[Coord.XYToIndex(x,y)]+=pawnAdvanceScore[y];
				else scoreLUT[Coord.XYToIndex(x,y)]+=pawnAdvanceScore[(BOARD_SIZE-1)-y];//if BLACK, apply score bonus in reverse because they move opposite to WHITE
			}
	}

	/**
	 * Converts the Pawn to a human-readable String containing its internal state
	 * @return String representation of internal state
	 */
	@Override
	public String toString(){
		return PieceCode.decodeTeamString(pieceCode)+" Pawn";
	}

	/**
	 * Calculates the Pawn's value to the AI player
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position Where this Piece is located on the board
	 * @return relative value to the AI
	 */
	@Override
	public int pieceValue(final long enemies,final long blanks,final int position){//TODO speed test against a matrix
		// after 5th file pawn values should increment by 1.5 imo. double pawns in same col should worth less, especially the blocked pawn 
		int score=100;

		if(0!=(blanks & (1L << position))) score-=65;//penalty for blocked pawns
		if(0!=(enemies & attackMask(enemies,blanks,position))) score+=50;//reward for potential captures

		return score+scoreLUT[position];
	}

	/**
	 * The move generator method for Pawns
	 * @param moves    Reference to the Move list
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position The position index to check from
	 */
	@Override
	public void getMoves(ArrayList<Integer> moves,final long enemies,final long blanks,final int position){
		ArrayList<Integer> tempMoves=new ArrayList<>();
		long diagLeftMask=(1L << Coord.shiftIndex(position,-1,deltaY)),//diagonally to the left, purely for readability, generates the bitmask for the square
				diagRightMask=(1L << Coord.shiftIndex(position,1,deltaY));//diagonally to the right,todo make these generated only if not on edges... saves cycles later
		//normal moves
		if(0!=(blanks & (1L << Coord.shiftIndex(position,0,deltaY)))){//single move if not blocked
			tempMoves.add(Move.encodeNormal(pieceCode,position,Coord.shiftIndex(position,0,deltaY)));//single move in direction of travel for this pawn
			if(0!=(blanks & (1L << Coord.shiftIndex(position,0,2*deltaY))) && 0!=(startingRank & (1L << position))){//if not blocked and on starting rank
				tempMoves.add(Move.encode(Move.pawnDoubleMove,pieceCode,position,Coord.shiftIndex(position,0,2*deltaY)));//double move on first move
			}
		}

		boolean edgeLeft=Coord.indexToX(position)==0;
		boolean edgeRight=Coord.indexToX(position)==7;

		//captures (with Yoda code)
		if(0!=(enemies & diagLeftMask) && !edgeLeft)
			tempMoves.add(Move.encode(Move.capture,pieceCode,position,Coord.shiftIndex(position,-1,deltaY)));//capture if enemy piece diagonally to the left
		if(0!=(enemies & diagRightMask) && !edgeRight)
			tempMoves.add(Move.encode(Move.capture,pieceCode,position,Coord.shiftIndex(position,1,deltaY)));//capture if enemy piece diagonally to the right

		//promotion
		if(team==WHITE && 0!=(WHITE_Promotion_mask & (1L << position)) ||//Mask WHITE for promotion eligibility
				team==BLACK && 0!=(BLACK_Promotion_mask & (1L << position))){//Mask BLACK for promotion eligibility, OR saves one jump more often than putting two ifs and selecting different promotion methods
			moves.addAll(generatePromotions(tempMoves));//if we can promote, force promotion
		}else{
			moves.addAll(tempMoves);//if no promotion, then return the normal moves list
		}
	}

	/**
	 * Separate check for EnPassant moves to save some cycles elsewhere
	 * @param EnPassant The mask holding the only piece that can be captured by EnPassant
	 * @param enemies   The mask of all enemies
	 * @param position  The position of this pawn
	 * @return Single integer encoded move or a blank move
	 */
	public int EnPassant(final long EnPassant,final long enemies,final int position){
		boolean edgeLeft=Coord.indexToX(position)==0;
		boolean edgeRight=Coord.indexToX(position)==7;
		if(0!=(enemies & EnPassant & (1L << Coord.shiftIndex(position,-1,0))) && !edgeLeft){//Can I EnPassant to the left? (bitmask madness to quick search EnPassant against enemies left of pawn)
			return Move.encode(Move.EnPassantCapture,pieceCode,position,Coord.shiftIndex(position,-1,deltaY));
		}else if(0!=(enemies & EnPassant & (1L << Coord.shiftIndex(position,1,0))) && !edgeRight){//Can I EnPassant to the right? (bitmask madness to quick search EnPassant against enemies right of pawn)
			return Move.encode(Move.EnPassantCapture,pieceCode,position,Coord.shiftIndex(position,1,deltaY));
		}//else if is because enpassant only happens to one pawn at a time, save a jump sometimes
		return Move.blank();
	}

	/**
	 * Converts all moves into promotions for each piece type on this Pawn's team
	 * @param moves The list of integer moves to convert
	 * @return The same moves but each one is now every possible promotion
	 */
	private ArrayList<Integer> generatePromotions(ArrayList<Integer> moves){
		ArrayList<Integer> promotions=new ArrayList<>();
		int start, end, specialCode;
		for(int i=0; i<moves.size(); ++i){//search all moves, exactly copy them with each eligible piece code for promotion
			start=Move.getStartIndex(moves.get(i));//TODO: replace this with more masking madness
			end=Move.getEndIndex(moves.get(i));
			specialCode=Move.getSpecialCode(moves.get(i));
			for(int j=pieceCode+2; j<PieceCode.KingW; j+=2){//this is a weird one, I chose to make BLACK and WHITE piece codes odd and even respectively, this works for the same reason a bit mask works, the 1's bit acts like a team toggle and by offsetting we can select the team for (almost) free. We also can't promote to kings
				promotions.add(Move.encode(specialCode | Move.pawnPromote,j,start,end));//copy everything but iterate all eligible new piece codes and toggle the promotion bit
			}
		}
		return promotions;
	}

	/**
	 * Get the mask of squares this Pawn can attack
	 * @param enemies Mask of enemies to capture
	 * @param blanks  Mask of blank squares
	 * @param pos     The integer position index
	 * @return a 64-bit integer bit mask
	 */
	@Override
	public long attackMask(final long enemies,final long blanks,final int pos){
		long mask;
		switch(Coord.indexToX(pos)){
			case 0://left side
				mask=0b00000010L << ((Coord.indexToY(pos)+deltaY)*BOARD_SIZE);
				break;
			case 7://right side
				mask=0b01000000L << ((Coord.indexToY(pos)+deltaY)*BOARD_SIZE);
				break;
			default:
				mask=0b00000101L << (pos-1+deltaY*BOARD_SIZE);//mask is 1 space too far over because it won't fit otherwise
		}
		return mask & (enemies | blanks);
	}
}