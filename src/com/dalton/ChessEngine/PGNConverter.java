package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

/**
 * Decodes PGN notation and generates PGN notation from game history
 * Designed to be small and efficient
 * @author Dalton Herrewynen
 * @version 0.1
 */
public class PGNConverter{
	/** What differentiations to use? Constants for making a switch more readable than a tree of if-else blocks */
	private static final int noDiff=0, DiffX=1, DiffY=2;

	/**
	 * Calculates what move a PGN token refers to
	 * Does not error check, function assumes the PGN token is valid
	 * @param board  The board after the move
	 * @param PGN    The PGN move
	 * @param player WHITE or BLACK
	 * @return The move which the algebraic notation encoded
	 */
	public static int getMove(Board board,String PGN,boolean player){
		ArrayList<Integer> moves;
		boolean capture=false;//is this move a capture
		char pieceInitial='P';//default to a pawn
		int dest=Coord.ERROR_INDEX;//default state is a failure unless we find a valid PGN token
		int startX=Coord.ERROR_INDEX, startY=Coord.ERROR_INDEX;//set x and y to error unless needed down the line
		int diffMethod=noDiff;//we assume no differentiation by default
		switch(PGN){
			case "0-0":
				return Move.encodeCastle(Move.kSideCastle,player);
			case "0-0-0":
				return Move.encodeCastle(Move.qSideCastle,player);
		}
		int i=PGN.length()-1;//hunt for the destination square back to front
		for(; i>0; --i){//look for the number, stop if there are not enough chars left to get a valid square
			if(PGN.charAt(i)>='1' && PGN.charAt(i)<='8'){//only care about numbers 1-8
				--i;//move back to get the letter
				dest=Coord.PGNToIndex(PGN.substring(i,i+2));
				break;//break the loop, we found what we are looking for
			}
		}
		if(dest==Coord.ERROR_INDEX) return Move.blankMove;//if we did not find a destination, early escape, save the cycles
		//otherwise, we need to go and hunt for things like differentiation
		for(; i>=0; --i){//Continue from where we found the coordinate, hunt for differentiations, piece initials, and captures
			if(PGN.charAt(i)=='x') capture=true;//if you see an x it's for a capture
			else if(isUppercase(PGN.charAt(i))){
				pieceInitial=PGN.charAt(i);//pieces are noted by uppercase letters, if they're not pawns
			}else if(PGN.charAt(i)>='1' && PGN.charAt(i)<='8'){
				startY=Coord.fromNumeral(PGN.charAt(i))-1;//Y coordinates are numbers 1-8, subtract 1 to make them 0-7
				diffMethod=DiffY;//flag to use differentiation by Y coordinate
			}else if(PGN.charAt(i)>='a' && PGN.charAt(i)<='h'){
				startX=Coord.fromLetter(PGN.charAt(i))-1;//X coordinates are a-h mapped 1-8, subtract 1 to offset to 0-7
				diffMethod=DiffX;//flag to use differentiation by X coordinate
			}
		}
		//get legal moves and search for the right one
		moves=Engine.getLegalMoves(board,PieceCode.encodeChar(pieceInitial,player));
		//search the moves based on differentiation method
		return switch(diffMethod){
			case DiffX -> searchMovesDiffX(moves,dest,startX,capture);//search by differentiation on the X coordinate
			case DiffY -> searchMovesDiffY(moves,dest,startY,capture);//search by differentiation on the Y coordinate
			default -> searchMovesNoDiff(moves,dest,capture);//if no differentiation, search by destination only
		};
	}

	/**
	 * Search for moves based only on the destination
	 * @param moves     ArrayList of move integers
	 * @param dest      The destination square
	 * @param isCapture Should we be looking for a capture move?
	 * @return An encoded move integer, either the matching move or a blank move if nothing found
	 */
	public static int searchMovesNoDiff(ArrayList<Integer> moves,int dest,boolean isCapture){
		//search for the moves
		for(int i=0; i<moves.size(); ++i){
			if(Move.getEndIndex(moves.get(i))==dest && Move.isCapture(moves.get(i))==isCapture)
				return moves.get(i);//if the move destination and capture flag match, return it
		}
		return Move.blankMove;//if no move found, return a blank move
	}

	/**
	 * Search for moves based on destination and X coordinate
	 * @param moves     ArrayList of move integers
	 * @param dest      The destination square
	 * @param startX    The starting X coordinate
	 * @param isCapture Should we be looking for a capture move?
	 * @return An encoded move integer, either the matching move or a blank move if nothing found
	 */
	public static int searchMovesDiffX(ArrayList<Integer> moves,int dest,int startX,boolean isCapture){
		//search for the moves
		for(int i=0; i<moves.size(); ++i){
			if(Move.getEndIndex(moves.get(i))==dest && //match destination
					Coord.indexToX(Move.getEndIndex(moves.get(i)))==startX && //Match starting X Coordinates
					Move.isCapture(moves.get(i))==isCapture)//match if move is a capture to our capture flag
				return moves.get(i);//if everything matches, return this move
		}
		return Move.blankMove;//if no move found, return a blank move
	}

	/**
	 * Search for moves based on destination and Y coordinate
	 * @param moves     ArrayList of move integers
	 * @param dest      The destination square
	 * @param startY    The starting Y coordinate
	 * @param isCapture Should we be looking for a capture move?
	 * @return An encoded move integer, either the matching move or a blank move if nothing found
	 */
	public static int searchMovesDiffY(ArrayList<Integer> moves,int dest,int startY,boolean isCapture){
		//search for the moves
		for(int i=0; i<moves.size(); ++i){
			if(Move.getEndIndex(moves.get(i))==dest && //match destination
					Coord.indexToY(Move.getEndIndex(moves.get(i)))==startY && //Match starting X Coordinates
					Move.isCapture(moves.get(i))==isCapture)//match if move is a capture to our capture flag
				return moves.get(i);//if everything matches, return this move
		}
		return Move.blankMove;//if no move found, return a blank move
	}

	/**
	 * Generates the PGN algebraic notation from an integer encoded move
	 * Does no error checking
	 * @param board The board state before the move (check for ambiguous moves)
	 * @param move  Integer encoded move
	 * @return A PGN encoded move String
	 */
	public static String getPGN(Board board,int move){
		String pgn=Coord.indexToPGN(Move.getEndIndex(move));//set the destination square
		if(Move.isCapture(move)) pgn="x"+pgn;//if a capture, then prepend an x before the coordinate
		int code=Move.getPieceCode(move);
		ArrayList<Integer> candidates=Engine.getLegalMoves(board,code);//find all the moves for this piece type
		for(int i=0; i<candidates.size(); ++i){//search for ambiguous moves, then differentiate
			if(Move.getEndIndex(candidates.get(i))==Move.getEndIndex(move) &&//if the move matches the destination
					Move.getStartIndex(candidates.get(i))!=Move.getStartIndex(move)){//and does not have same start index
				//check if the X matches or the Y matches, the put the opposite coordinate into the PGN
				if(Coord.indexToX(Move.getStartIndex(candidates.get(i)))==Coord.indexToX(Move.getStartIndex(move))){
					pgn=Coord.toNumeral(Coord.indexToY(Move.getStartIndex(move)))+pgn;//prepend the correct Y coordinate if the X matches
				}else{//if the X coordinate does not match, then the Y must match
					pgn=Coord.toLetter(Coord.indexToX(Move.getStartIndex(move)))+pgn;//prepend the correct X coordinate if the Y matches
				}
				break;//if we differentiated, then we are done
			}
		}
		if(code==PieceCode.PawnW||code==PieceCode.PawnB) return pgn;//for pawns, omit the piece initial
		else return charUppercase(PieceCode.decodeChar(code))+pgn;//prepend the uppercase piece initial and return the whole token
	}

	/**
	 * Generates a FEN string from the board state
	 * @param board The board in its current state
	 * @param team  WHITE or BLACK, Who goes next
	 * @return A FEN formatted string
	 */
	public static String generateFEN(Board board,boolean team){
		return "";
	}

	/**
	 * Takes a FEN string and applies its state to a new Board instance
	 * @param FEN The FEN string
	 * @return A Board with the FEN applied
	 */
	public static Board applyFEN(String FEN){
		return new Board();
	}
}
