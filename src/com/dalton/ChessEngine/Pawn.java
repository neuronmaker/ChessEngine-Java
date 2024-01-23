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
	/** Bit masks used for fast detection of pawns that can be promoted */
	public static final long
			WHITE_Promotion_mask=0b0000000011111111000000000000000000000000000000000000000000000000L,
			BLACK_Promotion_mask=0b0000000000000000000000000000000000000000000000001111111100000000L;

	/**
	 * Constructs the Pawn and sets the color
	 * @param team WHITE or BLACK
	 */
	public Pawn(boolean team){
		super((team==WHITE)? PieceCode.PawnW : PieceCode.PawnB);
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
	 * @param board    The current Board object
	 * @param position Where this Piece is located on the board
	 * @return relative value to the AI
	 */
	@Override
	public int pieceValue(Board board,int position){//TODO speed test against a matrix
		// after 5th file pawn values should increment by 1.5 imo. double pawns in same col should worth less, especially the blocked pawn 
		int score=100, xPos=Coord.indexToX(position), yPos=Coord.indexToY(position);

		if(board.getSquare(Coord.XYToIndex(Coord.indexToX(position),Coord.indexToY(position)+moveYDirection(team)))!=Move.blank()){
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
	 * Returns 1 if WHITE and -1 if BLACK
	 * @param team The team of the pawn
	 * @return -1 or +1
	 */
	private int moveYDirection(boolean team){
		return (team==WHITE)? 1 : -1;//move forward if WHITE, backwards if BLACK
	}

	/**
	 * The move generator method for Pawns
	 * @param board    The current state of the board
	 * @param position The position index to check from
	 * @return an ArrayList of integers which encode all the relevant move data for each move
	 */
	@Override
	public ArrayList<Integer> getMoves(Board board,int position){
		ArrayList<Integer> moves=new ArrayList<>();
		int dy=(team==WHITE)? 1 : -1;//move forward if WHITE, backwards if BLACK
		long diagLeftMask=(1L << Coord.shiftIndex(position,-1,dy)),//diagonally to the left, purely for readability, generates the bitmask for the square
				diagRightMask=(1L << Coord.shiftIndex(position,1,dy));//diagonally to the right, purely for readability, generates the bitmask for the square
		long enemies=board.alliedPieceMask(!team);//get all squares controlled by the enemy

		//normal moves
		if(board.getSquare(Coord.shiftIndex(position,0,dy))==PieceCode.Blank){//single move if not blocked todo speed test against use blank square checks
			moves.add(Move.encodeNormal(pieceCode,position,Coord.shiftIndex(position,0,dy)));//single move in direction of travel for this pawn
			if(board.getSquare(Coord.shiftIndex(position,0,2*dy))==PieceCode.Blank && board.hasNotMoved(position)){//if first move and not blocked
				moves.add(Move.encode(Move.pawnDoubleMove,pieceCode,position,Coord.shiftIndex(position,0,2*dy)));//double move on first move
			}
		}

		boolean edgeLeft=Coord.indexToX(position)==0;
		boolean edgeRight=Coord.indexToX(position)==7;

		//captures (with Yoda code)
		if(0!=(enemies & diagLeftMask) && !edgeLeft) moves.add(Move.encode(Move.capture,pieceCode,position,Coord.shiftIndex(position,-1,dy)));//capture if enemy piece diagonally to the left
		if(0!=(enemies & diagRightMask) && !edgeRight) moves.add(Move.encode(Move.capture,pieceCode,position,Coord.shiftIndex(position,1,dy)));//capture if enemy piece diagonally to the right
		//EnPassant - no need to check for blank squares as they will be blank if EnPassant was tripped anyhow
		if(0!=(enemies & board.getEnPassant() & (1L << Coord.shiftIndex(position,-1,0))) && !edgeLeft){//Can I EnPassant to the left? (bitmask madness to quick search EnPassant against enemies left of pawn)
			moves.add(Move.encode(Move.EnPassantCapture,pieceCode,position,Coord.shiftIndex(position,-1,dy)));
		}else if(0!=(enemies & board.getEnPassant() & (1L << Coord.shiftIndex(position,1,0))) && !edgeRight){//Can I EnPassant to the right? (bitmask madness to quick search EnPassant against enemies right of pawn)
			moves.add(Move.encode(Move.EnPassantCapture,pieceCode,position,Coord.shiftIndex(position,1,dy)));
		}//else if is because enpassant only happens to one pawn at a time, save a jump sometimes

		//promotion
		if(team==WHITE && 0!=(WHITE_Promotion_mask & (1L << position)) ||//Mask WHITE for promotion eligibility
				team==BLACK && 0!=(BLACK_Promotion_mask & (1L << position))){//Mask BLACK for promotion eligibility, OR saves one jump more often than putting two ifs and selecting different promotion methods
			return generatePromotions(moves);//if we can promote, force promotion
		}

		return moves;//if no promotion, then return the normal moves list
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
}