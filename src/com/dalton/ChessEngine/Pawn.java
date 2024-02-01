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
 * @version 3
 */
public class Pawn extends Piece{
	private final int dy;
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

	/**
	 * Constructs the Pawn and sets the color
	 * @param team WHITE or BLACK
	 */
	public Pawn(boolean team){
		super((team==WHITE)? PieceCode.PawnW : PieceCode.PawnB);
		dy=(team==WHITE)? 1 : -1;//WHITE pawns move up, BLACK pawns move down
		startingRank=(team==WHITE)?BLACK_Promotion_mask:WHITE_Promotion_mask;//starting masks are opposite of promotion masks
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
		int score=100, xPos=Coord.indexToX(position), yPos=Coord.indexToY(position);

		if(0!=(blanks&(1L<<position))){
			//This is triggered when there's a piece ahead of us. We're much less valuable when we're being blocked
			score-=65;
		}

		if(xPos==3 || xPos==4) score+=50;//Center pawns are worth more than side pawns

		if(team==WHITE && yPos>=4){//save 1 conditional
			int advancePawnScore=yPos-3;
			score+=advancePawnScore*60;
		}else if(yPos<=3){
			int advancePawnScore=4-yPos;
			score+=advancePawnScore*60;
		}
		return score;
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
		long diagLeftMask=(1L << Coord.shiftIndex(position,-1,dy)),//diagonally to the left, purely for readability, generates the bitmask for the square
				diagRightMask=(1L << Coord.shiftIndex(position,1,dy));//diagonally to the right,todo make these generated only if not on edges... saves cycles later
		//normal moves
		if(0!=(blanks & (1L << Coord.shiftIndex(position,0,dy)))){//single move if not blocked
			tempMoves.add(Move.encodeNormal(pieceCode,position,Coord.shiftIndex(position,0,dy)));//single move in direction of travel for this pawn
			if(0!=(blanks & (1L << Coord.shiftIndex(position,0,2*dy))) && 0!=(startingRank &(1L<<position))){//if not blocked and on starting rank
				tempMoves.add(Move.encode(Move.pawnDoubleMove,pieceCode,position,Coord.shiftIndex(position,0,2*dy)));//double move on first move
			}
		}

		boolean edgeLeft=Coord.indexToX(position)==0;
		boolean edgeRight=Coord.indexToX(position)==7;

		//captures (with Yoda code)
		if(0!=(enemies & diagLeftMask) && !edgeLeft) tempMoves.add(Move.encode(Move.capture,pieceCode,position,Coord.shiftIndex(position,-1,dy)));//capture if enemy piece diagonally to the left
		if(0!=(enemies & diagRightMask) && !edgeRight) tempMoves.add(Move.encode(Move.capture,pieceCode,position,Coord.shiftIndex(position,1,dy)));//capture if enemy piece diagonally to the right

		//promotion
		if(team==WHITE && 0!=(WHITE_Promotion_mask & (1L << position)) ||//Mask WHITE for promotion eligibility
				team==BLACK && 0!=(BLACK_Promotion_mask & (1L << position))){//Mask BLACK for promotion eligibility, OR saves one jump more often than putting two ifs and selecting different promotion methods
			moves.addAll(generatePromotions(tempMoves));//if we can promote, force promotion
		}
		moves.addAll(tempMoves);//if no promotion, then return the normal moves list
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
			return Move.encode(Move.EnPassantCapture,pieceCode,position,Coord.shiftIndex(position,-1,dy));
		}else if(0!=(enemies & EnPassant & (1L << Coord.shiftIndex(position,1,0))) && !edgeRight){//Can I EnPassant to the right? (bitmask madness to quick search EnPassant against enemies right of pawn)
			return Move.encode(Move.EnPassantCapture,pieceCode,position,Coord.shiftIndex(position,1,dy));
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
			for(int j=pieceCode+2; j<PieceCode.PIECE_TYPES; j+=2){//this is a weird one, I chose to make BLACK and WHITE piece codes odd and even respectively, this works for the same reason a bit mask works, the 1's bit acts like a team toggle and by offsetting we can select the team for (almost) free. We also can't promote to kings
				promotions.add(Move.encode(specialCode | Move.pawnPromote,j,start,end));//copy everything but iterate all eligible new piece codes and toggle the promotion bit
			}
		}
		return promotions;
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