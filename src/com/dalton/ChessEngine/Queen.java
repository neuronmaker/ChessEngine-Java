/*
Project: Chess Engine
File: Queen.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.PieceCode.*;
import static com.dalton.ChessEngine.Types.*;

/**
 * Class for the Queen piece
 * @author Dalton Herrewynen
 * @version 3
 */
public class Queen extends Piece{
	/**
	 * Only constructor requires the setting of team (color)
	 * @param team WHITE or BLACK
	 */
	public Queen(boolean team){
		super((team==WHITE)? QueenW : QueenB);
	}

	/**
	 * Converts the Queen to a human-readable String containing its internal state
	 * @return String representation of internal state
	 */
	@Override
	public String toString(){
		return decodeTeamString(pieceCode)+" Queen";
	}

	/**
	 * Placeholder for the incomplete method
	 * @param board    The current Board state
	 * @param position Where the piece is on the board
	 * @return The relative score for the AI
	 */
	//TODO Complete pieceValue()
	@Override
	public int pieceValue(Board board,int position){
		int score=2000;
		ArrayList<Integer> moves=getMoves(board,position);
		score+=moves.size()*15;
		return score;
	}

	/**
	 * The move generator method for Queen
	 * @param board    The current state of the board
	 * @param position The position index to check from
	 * @return an ArrayList of integers which encode all the relevant move data for each move
	 */
	@Override
	public ArrayList<Integer> getMoves(Board board,int position){//todo idea: split into table of rays and generate all rays with all blocked lengths
		ArrayList<Integer> moves=new ArrayList<>();
		long enemies=board.alliedPieceMask(!team);
		long blanks=~(enemies | board.alliedPieceMask(team));//add the enemies and friends together, invert to get blanks

		//Bishop moves
		moves.addAll(diagLineCheck(enemies,blanks,position));

		//Rook moves
		moves.addAll(HVLineCheck(enemies,blanks,position));

		return moves;
	}

	/**
	 * Get the mask of squares the Queen can attack
	 * @param friends Mask of friendly units to mask out
	 * @param pos     The integer position index
	 * @return a 64 bit integer bit mask
	 */
	@Override
	public long attackMask(long friends,int pos){
		return 0;
	}
}
