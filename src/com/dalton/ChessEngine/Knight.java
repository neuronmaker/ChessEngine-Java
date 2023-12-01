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

	/**
	 * Only constructor requires the setting of team (color)
	 * @param team which side is this piece going to be on
	 */
	public Knight(boolean team){
		super((team==WHITE) ? PieceCode.KnightW : PieceCode.KnightB);
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