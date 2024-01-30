package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

/**
 * Holds the chess engine code
 * @author Dalton Herrewynen
 * @version 0.3
 */
public class Engine{
	private int maxDepth;
	private int maxThreads=1;
	private Board[] boardArr;
	private Board checkMateBoard;

	/**
	 * Checks if the player is in check
	 * @param board Current board state
	 * @param team  WHITE or BLACK
	 * @return True if selected player is in check, False if not
	 */
	public static boolean inCheck(Board board,boolean team){
		int kingpos=Coord.maskToIndex(board.searchPiece((team==WHITE)? PieceCode.KingW : PieceCode.KingB));
		ArrayList<Integer> moves=getLegalMoves(board,!team);//get moves that the other guy can make
		for(int i=0; i<moves.size(); ++i){//search for a move which would capture the king
			if(Move.isCapture(moves.get(i)) && Move.getEndIndex(moves.get(i))==kingpos)
				return true;//if they can capture, then we are in check
		}
		return false;//if no moves that capture the king, then not in check
	}

	/**
	 * Checks if the player is checkmated (no way to save King from capture)
	 * @param board Current board state
	 * @param team  WHITE or BLACK
	 * @return True if checkmated, False if not
	 */
	public boolean isCheckmate(Board board,boolean team){
		if(!inCheck(board,team)) return false;//not in check means not possible to check mate
		ArrayList<Integer> moves=getLegalMoves(board,team);//get moves that this team can make
		for(int i=0; i<moves.size(); ++i){//search for a move which would get out of check
			checkMateBoard.loadState(board);
			checkMateBoard.makeMove(moves.get(i));//simulate the moves
			if(!inCheck(checkMateBoard,team)) return false;//if there is a move which gets out of check, then not a mate
		}
		return true;//if no saving moves found, then it's checkmate
	}
	/**
	 * Checks if the player is checkmated (no way to save King from capture), requires a pre-computed moves list
	 * @param board Current board state
	 * @param team  WHITE or BLACK
	 * @return True if checkmated, False if not
	 */
	public boolean isCheckmateFast(Board board,boolean team,ArrayList<Integer> moves){
		if(!inCheck(board,team)) return false;//not in check means not possible to check mate
		for(int i=0; i<moves.size(); ++i){//search for a move which would get out of check
			checkMateBoard.loadState(board);
			checkMateBoard.makeMove(moves.get(i));//simulate the moves
			if(!inCheck(checkMateBoard,team)) return false;//if there is a move which gets out of check, then not a mate
		}
		return true;//if no saving moves found, then it's checkmate
	}

	/**
	 * Scores the entire board
	 * @param board The current board state
	 * @return A score from WHITE player's perspective
	 */
	public int score(Board board){
		//todo find a way to only do legal moves since it gets moves after checks or checkmates
		if(isCheckmate(board,WHITE)) return Integer.MIN_VALUE;//if WHITE is checkmated, Min score favors BLACK
		if(isCheckmate(board,BLACK)) return Integer.MAX_VALUE;//if BLACK is checkmated, Max score favors WHITE
		int score=0;
		for(int i=0; i<PieceCode.PIECE_TYPES; ++i){
			long positions=board.searchPiece(i);//Search WHITE first
			int index=Coord.maskToIndex(positions);
			while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
				score+=PieceCode.pieceObj(i).pieceValue(board,index);//todo replace this with a function here that can be smarter
				index=Coord.maskToNextIndex(positions,index);//find next location
			}
			++i;//flip to BLACK
			positions=board.searchPiece(i);//Same Piece but now BLACK
			index=Coord.maskToIndex(positions);
			while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
				score-=PieceCode.pieceObj(i).pieceValue(board,index);
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
	public static ArrayList<Integer> getLegalMoves(Board board,boolean team){
		ArrayList<Integer> moves=new ArrayList<>();
		int i=(team==WHITE)? PieceCode.WHITE_OFFSET : PieceCode.BLACK_OFFSET;
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
	 * Generates all moves that are legal for a given piece type (all pawns, rooks, or knights, etc.)
	 * Only checks the pieces that belong to the same team as the indicated Piece Code
	 * @param board     The current board state
	 * @param pieceCode Which piece (and who's team) to check
	 * @return a list of moves encoded as integers
	 * @see PieceCode
	 */
	public static ArrayList<Integer> getLegalMoves(Board board,int pieceCode){
		ArrayList<Integer> moves=new ArrayList<>();
		long positions=board.searchPiece(pieceCode);//for each piece code
		int index=Coord.maskToIndex(positions);
		while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
			moves.addAll(PieceCode.pieceObj(pieceCode).getMoves(board,index));//get all moves for every one of these pieces
			index=Coord.maskToNextIndex(positions,index);//find next location
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
	public int getBestMove(Board board,boolean player,int depth){
		/*
		Generate all moves we can make
		score the move as is?
		Split them between the threads
		Run minimax on the moves
		Try to store all enemy moves a level or 2 deep, then recall the score from the move the enemy makes so save on computing time
		consider pre-allocating the boards to save on allocation time
		 */
		depth=Math.min(depth,maxDepth);
		ArrayList<Integer> legalMoves=getLegalMoves(board,player);
		ArrayList<Integer> scores=new ArrayList<>();
		int bestMove, bestScore;
		Board movedBoard=new Board(Board.CLEAR);
		if(legalMoves.isEmpty()) return Move.blank();//signal there are no moves if there are no moves found
		for(int i=0; i<legalMoves.size(); ++i){
			movedBoard.loadState(board);
			movedBoard.makeMove(legalMoves.get(i));
			scores.add(minimax(movedBoard,player,depth,Integer.MIN_VALUE,Integer.MAX_VALUE));
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
	public int getBestMove(Board board,boolean player){
		return getBestMove(board,player,maxDepth);
	}

	/**
	 * Gets the score of the board by searching possible moves, usually called after a move
	 * Initial call should set alpha to a very low value and beta to a very high value
	 * @param board The current board
	 * @param team  Who's turn? WHITE or BLACK
	 * @param depth How many more levels to search
	 * @param alpha Highest score found
	 * @param beta  Lowest score found
	 * @return integer score (higher score favors WHITE)
	 */
	public int minimax(Board board,boolean team,int depth,int alpha,int beta){
		/*
		Get all moves after this move, store them
		Score them
		alpha beta prune
		sort them by score
		search best move first (recall from storage, don't recompute)
		 */
		if(depth<=0) return score(board);//if at end of search, then return the score here
		ArrayList<Integer> moves=getLegalMoves(board,team);//call the move generator
		if(moves.isEmpty()) return score(board);//if no moves present, return this board position score
		Board movedBoard=boardArr[depth];//get reference to the pre-allocated board array
		int bestScore;
		if(team==WHITE){//WHITE is maximizing player
			//if(isCheckmateFast(board,WHITE,moves)) return Integer.MIN_VALUE;//if WHITE is checkmated, Min score favors BLACK
			bestScore=Integer.MIN_VALUE;//have not found a good move yet, pick the worst possible case for now
			for(int i=0; i<moves.size() && bestScore>=alpha; ++i){
				movedBoard.loadState(board);
				movedBoard.makeMove(moves.get(i));//load and move to avoid creating new boards all the time
				bestScore=Math.max(bestScore,minimax(movedBoard,BLACK,depth-1,alpha,beta));
				alpha=Math.max(alpha,bestScore);//store the maximal found score
				if(bestScore>=beta){
					//System.out.println("Alpha break Depth: "+depth+" "+ Move.describe(moves.get(i)));
					break;//break on beta cut off (pruning)
				}
			}
		}else{//BLACK is minimizing player
			//if(isCheckmateFast(board,BLACK,moves)) return Integer.MAX_VALUE;//if BLACK is checkmated, Max score favors WHITE
			bestScore=Integer.MAX_VALUE;//have not found a good move yet, go with worst option for now
			for(int i=0; i<moves.size(); ++i){
				movedBoard.loadState(board);
				movedBoard.makeMove(moves.get(i));//load and move to avoid creating new boards all the time
				bestScore=Math.min(bestScore,minimax(movedBoard,WHITE,depth-1,alpha,beta));
				beta=Math.min(beta,bestScore);//best minimal found score
				if(bestScore<=alpha){
					//System.out.println("Beta break Depth: "+depth+" "+ Move.describe(moves.get(i)));
					break;//break on alpha cut off (pruning)
				}
			}
		}
		return bestScore;
	}

	/**
	 * Loads default values and does the pre-computations for scoring and move generation
	 * @param threads The initial number of threads to aim for
	 * @param depth   The default maximum depth
	 */
	public Engine(int threads,int depth){
		maxDepth=depth;
		maxThreads=threads;
		boardArr=new Board[maxDepth];
		checkMateBoard=new Board(Board.CLEAR);
		for(int i=0; i<maxDepth; ++i){//pre-allocate the space for minimax boards
			boardArr[i]=new Board(Board.CLEAR);
		}
		/*
		load/calculate score table
		pre-calculate attack squares
		create threads
		pause threads
		 */
	}
}
