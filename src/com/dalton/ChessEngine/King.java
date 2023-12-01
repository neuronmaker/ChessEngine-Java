package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.PieceCode.*;
import static com.dalton.ChessEngine.Types.*;

/**
 * Class for the King piece
 * @author Dalton Herrewynen
 * @version 3
 * @see Piece
 */
public class King extends Piece{
	/** Generate all the possible directions a king can move */
	private static final int[][] offset={{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,-1},{1,-1},{-1,1}};
	/** Masks for checking Queen and King side castling */
	private static final long
			qSideCastleMask=0b0000000000000000000000000000000000000000000000000000000000001110L,
			kSideCastleMask=0b0000000000000000000000000000000000000000000000000000000001100000L;

	/**
	 * Only constructor requires the setting of team (color)
	 * @param team which side is this piece going to be on
	 */
	public King(boolean team){
		super((team==WHITE)? KingW : KingB);
	}

	/**
	 * Converts the King to a human-readable String containing its internal state
	 * @return String representation of internal state
	 */
	@Override
	public String toString(){
		return decodeTeamString(pieceCode)+" King";
	}

	/**
	 * Calculates the King's value to the AI player
	 * @param board    The current Board object
	 * @param position Where this Piece is located on the board
	 * @return relative value to the AI
	 */
	@Override
	public int pieceValue(final Board board,final int position){
		return Integer.MAX_VALUE;
	}

	/**
	 * The move generator method for King
	 * @param board    The current state of the board
	 * @param position The position index to check from
	 * @return an ArrayList of integers which encode all the relevant move data for each move
	 */
	@Override
	public ArrayList<Integer> getMoves(Board board,int position){
		ArrayList<Integer> moves=new ArrayList<>();
		long enemies=board.alliedPieceMask(!team);
		long blanks=~(enemies | board.alliedPieceMask(team));

		//Check each direction
		for(int[] dir: offset){
			int destIndex=Coord.shiftIndex(position,dir[0],dir[1]);
			if(!Coord.isShiftValid(position,dir[0],dir[1]))
				continue;//We can only move the King to squares that exist and won't cause a check
			if(0!=(blanks & (1L << destIndex))) moves.add(Move.encodeNormal(this.pieceCode,position,destIndex));//if blank, then just move
			else if(0!=(enemies & (1L << destIndex))) moves.add(Move.encode(Move.capture,this.pieceCode,position,destIndex));//Only capture the other team
		}
		//Castling moves
		if(board.hasNotMoved(position) && !isInCheck(board,position)){//no castling if moved or in check
			//Checking the queenside
			int rookPos=Coord.XYToIndex(0,Coord.indexToY(position));//get the rook for this side
			if(PieceCode.Blank==board.getSquare(Coord.shiftMask(qSideCastleMask,0,Coord.indexToY(position)))//use shifting of the mask to make it work for WHITE and BLACK
					&& board.hasNotMoved(rookPos)){//Must have blank line between the King and Rook and both must be unmoved
				moves.add(Move.encodeCastle(Move.qSideCastle, team));//We can castle Queen side
			}
			//Checking the kingside
			rookPos=Coord.XYToIndex(BOARD_SIZE-1,Coord.indexToY(position));//get the other rook for this side
			if(PieceCode.Blank==board.getSquare(Coord.shiftMask(kSideCastleMask,0,Coord.indexToY(position)))
					&& board.hasNotMoved(rookPos)){//Must have blank line between the King and Rook and both must be unmoved
				moves.add(Move.encodeCastle(Move.kSideCastle, team));//We can castle King side
			}
		}
		return moves;
	}

	/**
	 * Checks if King is in check
	 * @param board    The current Board object
	 * @param position Where this King is located on the board
	 * @return True if the King is in check, False otherwise
	 */
	public boolean isInCheck(Board board,int position){
		//Check if any enemy pieces can can attack the King's position
		for(int piece=(!team) ? 0 : 1; piece<PieceCode.KingW; piece+=2){//get all pieces for other team by integer code
			int pieceIndex=Coord.maskToIndex(board.searchPiece(piece));//locate the piece
			while(pieceIndex!=Coord.ERROR_INDEX){//while there is a piece to find
				ArrayList<Integer> enemyMoves=PieceCode.pieceObj(piece).getMoves(board,pieceIndex);//Get the legal moves from the enemy piece
				//todo Filter out pieces via bit masking
				for(int enemyMove: enemyMoves){//Check if any enemy move ends at the King's position
					if(Move.getEndIndex(enemyMove)==position) return true; //King is in check
				}
				pieceIndex=Coord.maskToNextIndex(board.searchPiece(piece),pieceIndex);//iterator
			}
		}
		return false;//King is not in check
	}
}
