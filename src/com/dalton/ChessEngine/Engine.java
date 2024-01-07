package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;
/**
 * Holds the chess engine code
 * @author Dalton Herrewynen
 * @version 0.1
 */
public class Engine{
	private int maxDepth;
	private static int maxThreads=1;

	/**
	 * Loads default values and does the pre-computations for scoring and move generation
	 * @param threads The initial number of threads to aim for
	 * @param depth   The default maximum depth
	 */
	public Engine(int threads,int depth){
		maxDepth=depth;
		maxThreads=threads;
		/*
		load/calculate score table
		pre-calculate attack squares
		create threads
		pause threads
		 */
	}

	/**
	 * Scores the entire board
	 * @param board The current board state
	 * @return A score from WHITE player's perspective
	 */
	public int score(Board board){
		int score=0;
		for(int i=0; i<PieceCode.PIECE_TYPES; ++i){
			long positions=board.searchPiece(i);//for each piece code
			int index=Coord.maskToIndex(positions);
			int teamCoeff=(i%2==0)?1:-1;//if even, then make positive scores for WHITE, otherwise flip to negative for BLACK
			while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
				score+=teamCoeff*PieceCode.pieceObj(i).pieceValue(board,index);//todo replace this with a function here that can be smarter
				index=Coord.maskToNextIndex(positions,index);//find next location
			}
		}
		return score;
	}

	/**
	 * Gets all legal moves for a given player team as encoded integers
	 * @param board The current state of the game
	 * @param team  WHITE or BLACK
	 * @return ArrayList of moves encoded into integers
	 */
	public ArrayList<Integer> getLegalMoves(Board board,boolean team){
		ArrayList<Integer> moves=new ArrayList<>();
		int i;
		if(team==WHITE) i=0;//WHITE starts at 0
		else i=1;//advance by 1 if BLACK
		for(; i<PieceCode.PIECE_TYPES; i+=2){
			long positions=board.searchPiece(i);//for each piece code
			int index=Coord.maskToIndex(positions);
			while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
				moves.addAll(PieceCode.pieceObj(i).getMoves(board,index));//get all moves for every allied piece
				index=Coord.maskToNextIndex(positions,index);//find next location
			}
		}
		return moves;
	}

	/**
	 * Generates all possible moves, split them up between all threads.
	 * Then returns the move with the best score after scoring to the desired depth.
	 * @param board  Current state of the board for the search
	 * @param player Pick best move for whom? (WHITE or BLACK)
	 * @param depth  The desired maximum depth
	 * @return Encoded move integer
	 */
	public int getBestMove(Board board, boolean player, int depth){
		/*
		Generate all moves we can make
		score the move as is?
		Split them between the threads
		Run minimax on the moves
		Try to store all enemy moves a level or 2 deep, then recall the score from the move the enemy makes so save on computing time
		 */
		ArrayList<Integer> legalMoves=getLegalMoves(board,player);
		ArrayList<Integer> scores=new ArrayList<>();
		int bestMove,bestScore;
		if(legalMoves.isEmpty()) return Move.blank();//signal there are no moves if there are no moves found
		for(int i=0; i<legalMoves.size(); ++i){
			scores.add(minimax(legalMoves.get(i),new Board(board),player,depth,Integer.MIN_VALUE,Integer.MAX_VALUE));
		}
		bestMove=legalMoves.get(0);//there is at least one move if we get here
		bestScore=scores.get(0);
		if(player==WHITE){//WHITE is maximizing player
			for(int i=1; i<scores.size(); ++i){
				if(scores.get(i)>bestScore){//find the maximal score and select its move
					bestScore=scores.get(i);
					bestMove=legalMoves.get(i);
				}
			}
		}else{//BLACK is the minimizing player
			for(int i=1; i<scores.size(); ++i){
				if(scores.get(i)<bestScore){//find the minimal score and select its move
					bestScore=scores.get(i);
					bestMove=legalMoves.get(i);
				}
			}
		}
		return bestMove;
	}

	/**
	 * Generates all possible moves, split them up between all threads.
	 * Then returns the move with the best score after scoring to the default maximum depth.
	 * @param board  Current state of the board for the search
	 * @param player Pick best move for whom? (WHITE or BLACK)
	 * @return Encoded move integer
	 */
	public int getBestMove(Board board, boolean player){
		return getBestMove(board,player,maxDepth);
	}

	/**
	 * Gets the score for one move encoded as an integer
	 * Initial call should set alpha to a very low value and beta to a very high value
	 * @param move  The move to score
	 * @param board The current board
	 * @param team  Who's turn? WHITE or BLACK
	 * @param depth How many more levels to search
	 * @param alpha Highest score found
	 * @param beta  Lowest score found
	 * @return integer score (higher score favors WHITE)
	 */
	public int minimax(int move, Board board, boolean team, int depth, int alpha, int beta){
		/*
		Get all moves after this move, store them
		Score them
		alpha beta prune
		sort them by score
		search best move first (recall from storage, don't recompute)
		 */
		board.makeMove(move);//make the move to score
		if(depth<=0) return score(board);//if at end of search, then return the score here
		ArrayList<Integer> moves=getLegalMoves(board,team);//call the move generator
		if(moves.isEmpty()) return score(board);//if no moves present, return this board position score
		int bestScore;
		if(team==WHITE){//WHITE is maximizing player
			bestScore=Integer.MIN_VALUE;//have not found a good move yet, pick the worst possible case for now
			for(int i=0; i<moves.size() && bestScore>=alpha; ++i){
				bestScore=Math.max(bestScore,minimax(moves.get(i),new Board(board),BLACK,depth-1,alpha,beta));
				alpha=Math.max(alpha,bestScore);//store the best found score
				if(bestScore>beta) break;//break on beta cut off (pruning)
			}
		}else{//BLACK is minimizing player
			bestScore=Integer.MAX_VALUE;//have not found a good move yet, go with worst option for now
			for(int i=0; i<moves.size(); ++i){
				bestScore=Math.min(bestScore,minimax(moves.get(i),new Board(board),WHITE,depth-1,alpha,beta));
				beta=Math.min(beta,bestScore);//best minimal score found
				if(bestScore<alpha) break;//break on alpha cut off (pruning)
			}
		}
		return bestScore;
	}
}
