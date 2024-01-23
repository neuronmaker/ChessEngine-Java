package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

/**
 * Class for the Knight piece
 * @author Dalton Herrewynen
 * @version 3
 * @see Piece
 */
public class Knight extends Piece{
	/** All the offsets that the Knight can move too */
	private static final int[][] offset={//0 is x, 1 is y
			{-2,1},{-1,2},{-2,-1},{-1,-2},//left half
			{2,1},{1,2},{2,-1},{1,-2}};   //right half
	/** Attacking square mask for Knight */
	private static final long[] attackMask={
			0b0000000000000000000000000000000000000000000000100000010000000000L,
			0b0000000000000000000000000000000000000000000001010000100000000000L,
			0b0000000000000000000000000000000000000000000010100001000100000000L,
			0b0000000000000000000000000000000000000000000101000010001000000000L,
			0b0000000000000000000000000000000000000000001010000100010000000000L,
			0b0000000000000000000000000000000000000000010100001000100000000000L,
			0b0000000000000000000000000000000000000000101000000001000000000000L,
			0b0000000000000000000000000000000000000000010000000010000000000000L,
			0b0000000000000000000000000000000000000010000001000000000000000100L,
			0b0000000000000000000000000000000000000101000010000000000000001000L,
			0b0000000000000000000000000000000000001010000100010000000000010001L,
			0b0000000000000000000000000000000000010100001000100000000000100010L,
			0b0000000000000000000000000000000000101000010001000000000001000100L,
			0b0000000000000000000000000000000001010000100010000000000010001000L,
			0b0000000000000000000000000000000010100000000100000000000000010000L,
			0b0000000000000000000000000000000001000000001000000000000000100000L,
			0b0000000000000000000000000000001000000100000000000000010000000010L,
			0b0000000000000000000000000000010100001000000000000000100000000101L,
			0b0000000000000000000000000000101000010001000000000001000100001010L,
			0b0000000000000000000000000001010000100010000000000010001000010100L,
			0b0000000000000000000000000010100001000100000000000100010000101000L,
			0b0000000000000000000000000101000010001000000000001000100001010000L,
			0b0000000000000000000000001010000000010000000000000001000010100000L,
			0b0000000000000000000000000100000000100000000000000010000001000000L,
			0b0000000000000000000000100000010000000000000001000000001000000000L,
			0b0000000000000000000001010000100000000000000010000000010100000000L,
			0b0000000000000000000010100001000100000000000100010000101000000000L,
			0b0000000000000000000101000010001000000000001000100001010000000000L,
			0b0000000000000000001010000100010000000000010001000010100000000000L,
			0b0000000000000000010100001000100000000000100010000101000000000000L,
			0b0000000000000000101000000001000000000000000100001010000000000000L,
			0b0000000000000000010000000010000000000000001000000100000000000000L,
			0b0000000000000010000001000000000000000100000000100000000000000000L,
			0b0000000000000101000010000000000000001000000001010000000000000000L,
			0b0000000000001010000100010000000000010001000010100000000000000000L,
			0b0000000000010100001000100000000000100010000101000000000000000000L,
			0b0000000000101000010001000000000001000100001010000000000000000000L,
			0b0000000001010000100010000000000010001000010100000000000000000000L,
			0b0000000010100000000100000000000000010000101000000000000000000000L,
			0b0000000001000000001000000000000000100000010000000000000000000000L,
			0b0000001000000100000000000000010000000010000000000000000000000000L,
			0b0000010100001000000000000000100000000101000000000000000000000000L,
			0b0000101000010001000000000001000100001010000000000000000000000000L,
			0b0001010000100010000000000010001000010100000000000000000000000000L,
			0b0010100001000100000000000100010000101000000000000000000000000000L,
			0b0101000010001000000000001000100001010000000000000000000000000000L,
			0b1010000000010000000000000001000010100000000000000000000000000000L,
			0b0100000000100000000000000010000001000000000000000000000000000000L,
			0b0000010000000000000001000000001000000000000000000000000000000000L,
			0b0000100000000000000010000000010100000000000000000000000000000000L,
			0b0001000100000000000100010000101000000000000000000000000000000000L,
			0b0010001000000000001000100001010000000000000000000000000000000000L,
			0b0100010000000000010001000010100000000000000000000000000000000000L,
			0b1000100000000000100010000101000000000000000000000000000000000000L,
			0b0001000000000000000100001010000000000000000000000000000000000000L,
			0b0010000000000000001000000100000000000000000000000000000000000000L,
			0b0000000000000100000000100000000000000000000000000000000000000000L,
			0b0000000000001000000001010000000000000000000000000000000000000000L,
			0b0000000000010001000010100000000000000000000000000000000000000000L,
			0b0000000000100010000101000000000000000000000000000000000000000000L,
			0b0000000001000100001010000000000000000000000000000000000000000000L,
			0b0000000010001000010100000000000000000000000000000000000000000000L,
			0b0000000000010000101000000000000000000000000000000000000000000000L,
			0b0000000000100000010000000000000000000000000000000000000000000000L,};

	/**
	 * Only constructor requires the setting of team (color)
	 * @param team which side is this piece going to be on
	 */
	public Knight(boolean team){
		super((team==WHITE)? PieceCode.KnightW : PieceCode.KnightB);
	}

	/**
	 * Converts the Knight to a human-readable String containing its internal state
	 * @return String representation of internal state
	 */
	@Override
	public String toString(){
		return PieceCode.decodeTeamString(pieceCode)+" Knight";
	}

	/**
	 * Calculates the Knight's value to the AI player
	 * @param board    The current Board object
	 * @param position Where this Piece is located on the board
	 * @return relative value to the AI
	 */
	@Override
	public int pieceValue(Board board,int position){
		int score=300, xPos=Coord.indexToX(position), yPos=Coord.indexToY(position);

		if(xPos==0 || xPos==7){
			if(yPos==0 || yPos==7) score-=75;//Steep penalty for knights in the corners
			else score-=50;//Smaller penalty for knights at the edge
		}
		return score;
	}

	/**
	 * The move generator method for Knight
	 * @param board    The current state of the board
	 * @param position The position index to check from
	 * @return an ArrayList of integers which encode all the relevant move data for each move
	 */
	@Override
	public ArrayList<Integer> getMoves(Board board,final int position){
		ArrayList<Integer> moves=new ArrayList<>();
		long enemies=board.alliedPieceMask(!team);
		long blanks=~(enemies | board.alliedPieceMask(team));
		//Check each direction
		for(int[] dir: offset){
			int destIndex=Coord.shiftIndex(position,dir[0],dir[1]);
			if(!Coord.isShiftValid(position,dir[0],dir[1])) continue;//We can only move the knight to squares that exist
			if(0!=(blanks & (1L << destIndex))) moves.add(Move.encodeNormal(this.pieceCode,position,destIndex));//if blank, then just move
			else if(0!=(enemies & (1L << destIndex))) moves.add(Move.encode(Move.capture,this.pieceCode,position,destIndex));//Only capture the other team
		}
		return moves;
	}
}