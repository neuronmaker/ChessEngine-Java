/*
Project: Chess Engine
File: MoveGenerator.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;

import static com.dalton.ChessEngine.Types.*;

/**
 * All move generation logic
 * @author Dalton Herrewynen
 * @version 0
 */
public class MoveGenerator{
	long[] attackMask={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//16 masks for 16 pieces
	int[] pieceIndicies={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//16 locations for 16 pieces

	public MoveGenerator(){
	}

	public void generateMoves(Board board,boolean team){
	}

	/**
	 * Gets the total squares that a team can attack
	 * @param board Current board state
	 * @param team  WHITE or BLACK
	 * @return Bitmask of all squares the team can attack
	 */
	public static long getTeamAttackMask(Board board, boolean team){
		int index;
		long mask=0,positions;
		long enemies=board.alliedPieceMask(!team),
				blanks=~(enemies | board.alliedPieceMask(team));
		for(int i=(team==WHITE)? 0 : 1; i<PieceCode.PIECE_TYPES; i+=2){
			positions=board.searchPiece(i);//for each piece code
			index=Coord.maskToIndex(positions);
			while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
				mask|=PieceCode.pieceObj(i).attackMask(enemies,blanks,index);
				index=Coord.maskToNextIndex(positions,index);//find next location
			}
		}
		return mask;
	}
}
