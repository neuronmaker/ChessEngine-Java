package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

/**
 * Holds the chess engine code
 * @author Dalton Herrewynen
 * @version 0.4
 */
public class Engine{
	private MoveGenerator moveGen;
	private int maxDepth;
	private int maxThreads=1;
	/** Pre-Allocated boards for re-use in MiniMax, Arranged[depth] */
	private Board[] boardArr;
	/** Pre-Allocated board for re-use in checkmate checking */
	private Board checkMateBoard;

	/**
	 * Checks if the player is in check
	 * @param board Current board state
	 * @param team  WHITE or BLACK
	 * @return True if selected player is in check, False if not
	 */
	public static boolean inCheck(Board board,boolean team){
		long kingPos=board.searchPiece((team==WHITE)? PieceCode.KingW : PieceCode.KingB);
		long attackMask=MoveGenerator.getTeamAttackMask(board,!team);//get capture mask for other team
		return 0!=(kingPos&attackMask);
	}

	/**
	 * Checks if the player is checkmated (no way to save King from capture)
	 * @param board Current board state
	 * @param team  WHITE or BLACK
	 * @return True if checkmated, False if not
	 */
	public boolean isCheckmate(Board board,boolean team){
		if(!inCheck(board,team)) return false;//not in check means not possible to check mate
		ArrayList<Integer> moves=getMoves(board,team);//get moves that this team can make
		for(int i=0; i<moves.size(); ++i){//search for a move which would get out of check
			checkMateBoard.loadState(board);
			checkMateBoard.makeMove(moves.get(i));//simulate the moves
			if(!inCheck(checkMateBoard,team)) return false;//if there is a move which gets out of check, then not a mate
		}
		return true;//if no saving moves found, then it's checkmate
	}

	/**
	 * Checks if the player is in check (requires externally computed moves)
	 * @param moves   The list of other team's moves to check against
	 * @param kingPos Where is this team's King?
	 * @return True if selected player is in check, False if not
	 */
	public static boolean inCheckFast(ArrayList<Integer> moves,final int kingPos){
		int i=0;
		while(i<moves.size() && Move.getEndIndex(moves.get(i))!=kingPos){//search for a move which would capture the king
			++i;
		}
		return i>=moves.size();//if we ended early, it means that there was a capture on the king
	}
	/**
	 * Checks if the player is checkmated (no way to save King from capture), requires a pre-computed moves list
	 * @param board Current board state
	 * @param team  WHITE or BLACK
	 * @param moves Pre-calculated list of moves
	 * @return True if checkmated, False if not
	 */
	private boolean isCheckmateFast(Board board,final boolean team,ArrayList<Integer> moves){
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
		//if(isCheckmate(board,WHITE)) return Integer.MIN_VALUE;//if WHITE is checkmated, Min score favors BLACK
		//if(isCheckmate(board,BLACK)) return Integer.MAX_VALUE;//if BLACK is checkmated, Max score favors WHITE
		long white=board.alliedPieceMask(WHITE),black=board.alliedPieceMask(BLACK),
				blank=~(white|black);//add all occupied squares, take any that are not occupied and consider them blank
		int score=0;
		for(int i=0; i<PieceCode.PIECE_TYPES; ++i){
			long positions=board.searchPiece(i);//Search WHITE first
			int index=Coord.maskToIndex(positions);
			while(index!=Coord.ERROR_INDEX){//search all positions that WHITE piece is found at
				score+=PieceCode.pieceObj(i).pieceValue(black,blank,index);//todo replace this with a function here that can be smarter
				index=Coord.maskToNextIndex(positions,index);//find next location
			}
			++i;//flip to BLACK
			positions=board.searchPiece(i);//Same Piece but now BLACK
			index=Coord.maskToIndex(positions);
			while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
				score-=PieceCode.pieceObj(i).pieceValue(white,blank,index);
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
	public static ArrayList<Integer> getMoves(Board board,boolean team){
		ArrayList<Integer> moves=new ArrayList<>();//Pass this single list around by reference, fewer memory allocations
		int i=(team==WHITE)? PieceCode.WHITE_OFFSET : PieceCode.BLACK_OFFSET,pawn,king,index;
		long positions,enemies=board.alliedPieceMask(!team),
				blanks=~(enemies | board.alliedPieceMask(team));//add the enemies and friends together, invert to get blanks
		for(; i<PieceCode.PIECE_TYPES; i+=2){
			positions=board.searchPiece(i);//for each piece code
			index=Coord.maskToIndex(positions);
			while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
				PieceCode.pieceObj(i).getMoves(moves,enemies,blanks,index);//get all basic moves for every allied piece
				index=Coord.maskToNextIndex(positions,index);//find next location
			}
		}
		//Check for EnPassant
		if(team==WHITE){//todo, use batch processing masking techniques
			positions=board.searchPiece(PieceCode.PawnW)&Pawn.WHITE_EnPassant_mask;//get all pawns that are at EnPassant capture rank
			pawn=PieceCode.PawnW;
			king=PieceCode.KingW;
		}else{
			positions=board.searchPiece(PieceCode.PawnB)&Pawn.BLACK_EnPassant_mask;
			pawn=PieceCode.PawnB;
			king=PieceCode.KingB;
		}
		index=Coord.maskToIndex(positions);
		while(index!=Coord.ERROR_INDEX){
			int move=((Pawn) PieceCode.pieceObj(pawn)).EnPassant(board.getEnPassant(),enemies,index);
			if(!Move.isBlank(move)) moves.add(move);
			index=Coord.maskToNextIndex(positions,index);
		}
		//Castling
		((King) PieceCode.pieceObj(king)).getCastles(board,moves);//get castling moves
		return moves;
	}

	/**
	 * Generates all moves that a given piece type (all pawns, rooks, or knights, etc.) can make
	 * Does not filter out moves leading to a check
	 * @param board     The current board state
	 * @param pieceCode Which piece (and who's team) to check
	 * @return a list of moves encoded as integers
	 * @see PieceCode
	 */
	public static ArrayList<Integer> getMoves(Board board,int pieceCode){
		ArrayList<Integer> moves=new ArrayList<>();//Pass this single list around by reference, fewer memory allocations
		long positions=board.searchPiece(pieceCode);//for each piece code
		long enemies=board.alliedPieceMask(!PieceCode.decodeTeam(pieceCode)),
				blanks=~(enemies | board.alliedPieceMask(PieceCode.decodeTeam(pieceCode)));
		long filteredPawns=0;
		int index=Coord.maskToIndex(positions);//get initial position
		switch(pieceCode){//special moves
			case PieceCode.KingW://Kings can castle
			case PieceCode.KingB://There is only one King on the board
				PieceCode.pieceObj(pieceCode).getMoves(moves,enemies,blanks,index);//get all standard moves
				((King) PieceCode.pieceObj(pieceCode)).getCastles(board,moves);//get castling moves
				return moves;//Only one king so we are done
			case PieceCode.PawnW://select the EnPassant filter for either pawn
				filteredPawns=Pawn.WHITE_EnPassant_mask&positions;//filter out only pawns that can EnPassant
				break;
			case PieceCode.PawnB:
				filteredPawns=Pawn.BLACK_EnPassant_mask&positions;
				break;
		}
		if(board.getEnPassant()!=0 && filteredPawns!=0){//if there is an EnPassant vulnerability
			int filteredIndex=Coord.maskToIndex(filteredPawns),move;
			while(filteredIndex!=Coord.ERROR_INDEX){//search all positions that piece is found at
				move=((Pawn) PieceCode.pieceObj(pieceCode)).EnPassant(board.getEnPassant(),enemies,filteredIndex);
				if(!Move.isBlank(move)) moves.add(move);//if a move was found, add it
				filteredIndex=Coord.maskToNextIndex(filteredPawns,filteredIndex);//find next location
			}
		}
		while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
			PieceCode.pieceObj(pieceCode).getMoves(moves,enemies,blanks,index);//get all normal moves for every one of these pieces
			index=Coord.maskToNextIndex(positions,index);//find next location
		}
		return moves;
	}

	/**
	 * Generates only moves that are legal for a given piece type (all pawns, rooks, or knights, etc.)
	 * Only checks the pieces that belong to the same team as the indicated Piece Code
	 * @param board     The current board state
	 * @param pieceCode Which piece (and who's team) to check
	 * @return a list of moves encoded as integers
	 * @see PieceCode
	 */
	public static ArrayList<Integer> getLegalMoves(Board board,int pieceCode){
		ArrayList<Integer> moves=getMoves(board,pieceCode),legal=new ArrayList<>(moves.size());
		Board movedBoard=new Board(Board.CLEAR);
		for(int i=0; i<moves.size(); ++i){
			movedBoard.loadState(board);
			movedBoard.makeMove(moves.get(i));//simulate the move
			if(!inCheck(movedBoard,PieceCode.decodeTeam(pieceCode)))//if not in check
				legal.add(moves.get(i));//add move
		}
		return legal;
	}
	private int nodes=0;
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
		 */
		nodes=0;
		depth=Math.min(depth,maxDepth);
		ArrayList<Integer> legalMoves=getMoves(board,player);
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
		System.out.println("Nodes: "+nodes);
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
	 * @param alpha Best score for WHITE
	 * @param beta  Best score for BLACK
	 * @return integer score (higher score favors WHITE)
	 */
	public int minimax(Board board,boolean team,int depth,int alpha,int beta){
		/*
		Get all moves after this move, store them
		Score them
		sort them by score
		search best move first (recall from storage, don't recompute)
		 */
		++nodes;
		if(depth<=0) return score(board);//if at end of search, then return the score here
		ArrayList<Integer> moves=getMoves(board,team);//call the move generator
		if(moves.isEmpty()) return score(board);//if no moves present, return this board position score
		Board movedBoard=boardArr[depth];//get reference to the pre-allocated board array
		int bestScore;
		if(team==WHITE){//WHITE is maximizing player
			if(isCheckmateFast(board,WHITE,moves)) return Integer.MIN_VALUE;//if WHITE is checkmated, Min score favors BLACK
			bestScore=Integer.MIN_VALUE;//have not found a good move yet, pick the worst possible case for now
			for(int i=0; i<moves.size() && alpha<beta; ++i){
				movedBoard.loadState(board);
				movedBoard.makeMove(moves.get(i));//load and move to avoid creating new boards all the time
				bestScore=Math.max(bestScore,minimax(movedBoard,BLACK,depth-1,alpha,beta));
				alpha=Math.max(alpha,bestScore);//store the maximal found score
			}
		}else{//BLACK is minimizing player
			if(isCheckmateFast(board,BLACK,moves)) return Integer.MAX_VALUE;//if BLACK is checkmated, Max score favors WHITE
			bestScore=Integer.MAX_VALUE;//have not found a good move yet, go with worst option for now
			for(int i=0; i<moves.size() && alpha<beta; ++i){
				movedBoard.loadState(board);
				movedBoard.makeMove(moves.get(i));//load and move to avoid creating new boards all the time
				bestScore=Math.min(bestScore,minimax(movedBoard,WHITE,depth-1,alpha,beta));
				beta=Math.min(beta,bestScore);//best minimal found score
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
		moveGen=new MoveGenerator();
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
