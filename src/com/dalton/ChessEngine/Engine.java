package com.dalton.ChessEngine;

import static com.dalton.ChessEngine.Types.*;
/**
 * Holds the chess engine code
 * @author Dalton Herrewynen
 * @version 0
 */
public class Engine{
	private int depth;
	private static int maxThreads=1;

	/**
	 * Loads default values and does the pre-computations for scoring and move generation
	 * @param threads The initial number of threads to aim for
	 * @param depth   The default maximum depth
	 */
	public Engine(int threads,int depth){
		this.depth=depth;
		maxThreads=threads;
		/*
		load/calculate score table
		pre-calculate attack squares
		create threads
		pause threads
		 */
	}

	/**
	 * Generates all possible moves, split them up between all threads.
	 * Then returns the move with the best score
	 * @param board
	 * @param player
	 * @return
	 */
	public int getBestMove(Board board,boolean player){
		/*
		Generate all moves we can make
		score the move as is?
		Split them between the threads
		Run minimax on the moves
		Try to store all enemy moves a level or 2 deep, then recall the score from the move the enemy makes so save on computing time
		 */
		return 0;
	}

	/**
	 * Gets the score for one move encoded as an integer
	 * @param move  The move to score
	 * @param board The board
	 * @param depth How many more levels to search
	 * @param alpha Highest score found
	 * @param beta  Lowest score found
	 * @return integer score (higher score favors WHITE)
	 */
	public int minimax(int move,Board board, int depth,int alpha,int beta){
		/*
		Get all moves after this move, store them
		Score them
		alpha beta prune
		sort them by score
		search best move first (recall from storage, don't recompute)
		 */
		return 0;
	}
}
