package com.dalton.ChessEngine;

import static com.dalton.ChessEngine.Types.*;

/**
 * All move generation logic
 * @author Dalton Herrewynen
 * @version 0
 */
public class MoveGenerator{
	long pawnMoves=0;
	long pawnLeftAttacks=0,pawnRightAttacks=0;
	int[] castling={0,0};
	long[] attackMask={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//16 masks for 16 pieces
	int[] pieceIndicies={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//16 locations for 16 pieces

	public MoveGenerator(){
	}

	/**
	 * Fills bitboard lists with move data
	 * @param board Current state of the board
	 * @param team  WHITE or BLACK
	 */
	public void generateMoves(Board board,boolean team){
		int index,pieceSerial=0,i=(team==WHITE)?0:1;
		long positions;
		long enemies=board.alliedPieceMask(!team),
				blanks=~(enemies | board.alliedPieceMask(team));

		//generate all pawn Moves in one shot
		if(team==WHITE){
			pawnMoves=blanks&(board.searchPiece(0)<<8);//shift all pawns up 1 (over 8) and only take blank spaces
			pawnMoves|=blanks&((board.searchPiece(0)&Pawn.BLACK_Promotion_mask)<<16);//double move on starting rank (opposite team's promotion mask)
			pawnLeftAttacks=enemies&(board.searchPiece(0)<<7);//one less than a full rank
			pawnRightAttacks=enemies&(board.searchPiece(0)<<9);//one more than a full rank
		}else{
			pawnMoves=blanks&(board.searchPiece(1)>>>8);//shift all pawns down 1 (over 8) and only take blank spaces
			pawnMoves|=blanks&((board.searchPiece(1)&Pawn.WHITE_Promotion_mask)>>>16);//double move on starting rank (opposite team's promotion mask)
			pawnLeftAttacks=enemies&(board.searchPiece(1)>>>9);//exactly backwards from the WHITE pawns
			pawnRightAttacks=enemies&(board.searchPiece(1)>>>7);
		}
		//todo handle promotions here

		for(i+=2; i<PieceCode.PIECE_TYPES; i+=2){
			positions=board.searchPiece(i);//for each piece code
			index=Coord.maskToIndex(positions);
			while(index!=Coord.ERROR_INDEX){//search all positions that piece is found at
				pieceIndicies[pieceSerial]=index;
				attackMask[pieceSerial]=PieceCode.pieceObj(i).attackMask(enemies,blanks,index);
				index=Coord.maskToNextIndex(positions,index);//find next location
				++pieceSerial;//move to next piece's bitboard
			}
		}
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
