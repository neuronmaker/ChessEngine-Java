package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.PieceCode.*;
import static com.dalton.ChessEngine.Types.*;

/**
 * Class for the King piece
 * @author Dalton Herrewynen
 * @version 4
 * @see Piece
 */
public class King extends Piece{
	private final int startingIndex;//There is only ever 1 King on the board
	/** Generate all the possible directions a king can move */
	private static final int[][] offset={{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,-1},{1,-1},{-1,1}};
	/** Masks for checking Queen and King side castling */
	private static final long
			qSideCastleMask=0b0000000000000000000000000000000000000000000000000000000000001110L,
			kSideCastleMask=0b0000000000000000000000000000000000000000000000000000000001100000L;
	/** The attacking mask */
	private static final long[] attackMask={//todo find edges, make a mask for each edge and combine them for the middle, using masking to chop off the edges when touching an edge
			0b0000000000000000000000000000000000000000000000000000001100000010L,
			0b0000000000000000000000000000000000000000000000000000011100000101L,
			0b0000000000000000000000000000000000000000000000000000111000001010L,
			0b0000000000000000000000000000000000000000000000000001110000010100L,
			0b0000000000000000000000000000000000000000000000000011100000101000L,
			0b0000000000000000000000000000000000000000000000000111000001010000L,
			0b0000000000000000000000000000000000000000000000001110000010100000L,
			0b0000000000000000000000000000000000000000000000001100000001000000L,
			0b0000000000000000000000000000000000000000000000110000001000000011L,
			0b0000000000000000000000000000000000000000000001110000010100000111L,
			0b0000000000000000000000000000000000000000000011100000101000001110L,
			0b0000000000000000000000000000000000000000000111000001010000011100L,
			0b0000000000000000000000000000000000000000001110000010100000111000L,
			0b0000000000000000000000000000000000000000011100000101000001110000L,
			0b0000000000000000000000000000000000000000111000001010000011100000L,
			0b0000000000000000000000000000000000000000110000000100000011000000L,
			0b0000000000000000000000000000000000000011000000100000001100000000L,
			0b0000000000000000000000000000000000000111000001010000011100000000L,
			0b0000000000000000000000000000000000001110000010100000111000000000L,
			0b0000000000000000000000000000000000011100000101000001110000000000L,
			0b0000000000000000000000000000000000111000001010000011100000000000L,
			0b0000000000000000000000000000000001110000010100000111000000000000L,
			0b0000000000000000000000000000000011100000101000001110000000000000L,
			0b0000000000000000000000000000000011000000010000001100000000000000L,
			0b0000000000000000000000000000001100000010000000110000000000000000L,
			0b0000000000000000000000000000011100000101000001110000000000000000L,
			0b0000000000000000000000000000111000001010000011100000000000000000L,
			0b0000000000000000000000000001110000010100000111000000000000000000L,
			0b0000000000000000000000000011100000101000001110000000000000000000L,
			0b0000000000000000000000000111000001010000011100000000000000000000L,
			0b0000000000000000000000001110000010100000111000000000000000000000L,
			0b0000000000000000000000001100000001000000110000000000000000000000L,
			0b0000000000000000000000110000001000000011000000000000000000000000L,
			0b0000000000000000000001110000010100000111000000000000000000000000L,
			0b0000000000000000000011100000101000001110000000000000000000000000L,
			0b0000000000000000000111000001010000011100000000000000000000000000L,
			0b0000000000000000001110000010100000111000000000000000000000000000L,
			0b0000000000000000011100000101000001110000000000000000000000000000L,
			0b0000000000000000111000001010000011100000000000000000000000000000L,
			0b0000000000000000110000000100000011000000000000000000000000000000L,
			0b0000000000000011000000100000001100000000000000000000000000000000L,
			0b0000000000000111000001010000011100000000000000000000000000000000L,
			0b0000000000001110000010100000111000000000000000000000000000000000L,
			0b0000000000011100000101000001110000000000000000000000000000000000L,
			0b0000000000111000001010000011100000000000000000000000000000000000L,
			0b0000000001110000010100000111000000000000000000000000000000000000L,
			0b0000000011100000101000001110000000000000000000000000000000000000L,
			0b0000000011000000010000001100000000000000000000000000000000000000L,
			0b0000001100000010000000110000000000000000000000000000000000000000L,
			0b0000011100000101000001110000000000000000000000000000000000000000L,
			0b0000111000001010000011100000000000000000000000000000000000000000L,
			0b0001110000010100000111000000000000000000000000000000000000000000L,
			0b0011100000101000001110000000000000000000000000000000000000000000L,
			0b0111000001010000011100000000000000000000000000000000000000000000L,
			0b1110000010100000111000000000000000000000000000000000000000000000L,
			0b1100000001000000110000000000000000000000000000000000000000000000L,
			0b0000001000000011000000000000000000000000000000000000000000000000L,
			0b0000010100000111000000000000000000000000000000000000000000000000L,
			0b0000101000001110000000000000000000000000000000000000000000000000L,
			0b0001010000011100000000000000000000000000000000000000000000000000L,
			0b0010100000111000000000000000000000000000000000000000000000000000L,
			0b0101000001110000000000000000000000000000000000000000000000000000L,
			0b1010000011100000000000000000000000000000000000000000000000000000L,
			0b0100000011000000000000000000000000000000000000000000000000000000L,};

	/**
	 * Only constructor requires the setting of team (color)
	 * @param team which side is this piece going to be on
	 */
	public King(boolean team){
		super((team==WHITE)? KingW : KingB);
		startingIndex=Coord.XYToIndex(Board.KingX,(team==WHITE)?XYMIN:XYMAX);//For WHITE shift up 0, for BLACK, shift to the top
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
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position Where this Piece is located on the board
	 * @return relative value to the AI
	 */
	@Override
	public int pieceValue(final long enemies,final long blanks,final int position){
		return Integer.MAX_VALUE;
	}

	/**
	 * Move generator for the King
	 * @param moves    Reference to the Move list
	 * @param enemies  Mask of enemy squares
	 * @param blanks   Mask of blank squares
	 * @param position The position index to check from
	 */
	@Override
	public void getMoves(ArrayList<Integer> moves,final long enemies,final long blanks,final int position){
		//Check each direction
		for(int[] dir: offset){
			int destIndex=Coord.shiftIndex(position,dir[0],dir[1]);
			if(!Coord.isShiftValid(position,dir[0],dir[1]))
				continue;//We can only move the King to squares that exist and won't cause a check
			if(0!=(blanks & (1L << destIndex)))
				moves.add(Move.encodeNormal(this.pieceCode,position,destIndex));//if blank, then just move
			else if(0!=(enemies & (1L << destIndex)))
				moves.add(Move.encode(Move.capture,this.pieceCode,position,destIndex));//Only capture the other team
		}
	}

	/**
	 * Checks for castling moves
	 * @param board The current state of the board
	 * @param moves The list of moves to add any castling moves onto
	 */
	public void getCastles(Board board,ArrayList<Integer> moves){
		if(board.hasNotMoved(startingIndex) && !isInCheck(board,startingIndex)){//no castling if moved or in check
			//Checking the queenside
			int rookPos=Coord.XYToIndex(0,Coord.indexToY(startingIndex));//get the rook for this side
			if(PieceCode.Blank==board.getSquare(Coord.shiftMask(qSideCastleMask,0,Coord.indexToY(startingIndex)))//use shifting of the mask to make it work for WHITE and BLACK
					&& board.hasNotMoved(rookPos)){//Must have blank line between the King and Rook and both must be unmoved
				moves.add(Move.encodeCastle(Move.qSideCastle,team));//We can castle Queen side
			}
			//Checking the kingside
			rookPos=Coord.XYToIndex(XYMAX,Coord.indexToY(startingIndex));//get the other rook for this side
			if(PieceCode.Blank==board.getSquare(Coord.shiftMask(kSideCastleMask,0,Coord.indexToY(startingIndex)))//as above, if anything falls into the mask, do not castle
					&& board.hasNotMoved(rookPos)){//Must have blank line between the King and Rook and both must be unmoved
				moves.add(Move.encodeCastle(Move.kSideCastle,team));//We can castle King side
			}
		}
	}

	/**
	 * Checks if King is in check
	 * @param board    The current Board object
	 * @param position Where this King is located on the board
	 * @return True if the King is in check, False otherwise
	 */
	public boolean isInCheck(Board board,int position){//todo change this to use an attacking mask for speed, ray cast from king instead of checking all enemy moves
		//Check if any enemy pieces can can attack the King's position
		for(int piece=(!team)? 0 : 1; piece<PieceCode.KingW; piece+=2){//get all pieces for other team by integer code
			final long theirEnemies=board.alliedPieceMask(team);//get this team as the other's enemies
			final long blanks=~(theirEnemies | board.alliedPieceMask(!team));//get all squares that are blank
			int pieceIndex=Coord.maskToIndex(board.searchPiece(piece));//locate the piece
			while(pieceIndex!=Coord.ERROR_INDEX){//while there is a piece to find
				ArrayList<Integer> enemyMoves=new ArrayList<>();
				PieceCode.pieceObj(piece).getMoves(enemyMoves,theirEnemies,blanks,pieceIndex);//Get the legal moves from the enemy piece
				//todo Filter out pieces via bit masking
				for(int enemyMove: enemyMoves){//Check if any enemy move ends at the King's position
					if(Move.getEndIndex(enemyMove)==position) return true; //King is in check
				}
				pieceIndex=Coord.maskToNextIndex(board.searchPiece(piece),pieceIndex);//iterator
			}
		}
		return false;//King is not in check
	}

	/**
	 * Get the mask of squares this King can attack
	 * @param enemies Mask of enemies to capture
	 * @param blanks  Mask of blank squares
	 * @param pos     The integer position index
	 * @return a 64-bit integer bit mask
	 */
	@Override
	public long attackMask(final long enemies,final long blanks,final int pos){
		return attackMask[pos]&(enemies|blanks);//take attacking mask and only attack pieces that are blank or enemies
	}
}
