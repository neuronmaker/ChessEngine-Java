package com.dalton.ChessEngine;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;

/**
 * Decodes PGN notation and generates PGN notation from game history
 * Designed to be small and efficient
 * @author Dalton Herrewynen
 * @version 0.3
 */
public class PGNConverter{
	/** What differentiations to use? Constants for making a switch more readable than a tree of if-else blocks */
	private static final int noDiff=0, DiffX=1, DiffY=2;
	/** Mask for enabling castling */
	private static final long KSideCastle=0b0000000000000000000000000000000000000000000000000000000010010000L,
			QSideCastle=0b0000000000000000000000000000000000000000000000000000000000010001L;

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
		char pieceInitial='P',promotedInitial=' ';//default to a pawn and no promotion
		int dest=Coord.ERROR_INDEX;//default state is a failure unless we find a valid PGN token
		int startX=Coord.ERROR_INDEX, startY=Coord.ERROR_INDEX;//set x and y to error unless needed down the line
		int diffMethod=noDiff,candidate;//we assume no differentiation by default
		switch(PGN){
			case "0-0":
			case "o-o":
			case "O-O":
				return Move.encodeCastle(Move.kSideCastle,player);
			case "0-0-0":
			case "o-o-o":
			case "O-O-O":
				return Move.encodeCastle(Move.qSideCastle,player);
		}
		int i=PGN.indexOf('=');//character that signals promotions
		if(i>0){//detect promotions, handle them later
			promotedInitial=charUppercase(PGN.charAt(i+1));//get the abbreviation of what the pawn becomes
			i=i-1;//hunt for destinations behind the = sign
		}else{
			i=PGN.length()-1;//Otherwise, hunt for the destination square back to front
		}
		for(; i>0; --i){//look for the number, stop if there are not enough chars left to get a valid square
			if(PGN.charAt(i)>='1' && PGN.charAt(i)<='8'){//only care about numbers 1-8
				--i;//move back to get the letter
				dest=Coord.PGNToIndex(PGN.substring(i,i+2));
				break;//break the loop, we found what we are looking for
			}
		}
		if(dest==Coord.ERROR_INDEX) return Move.blankMove;//if we did not find a destination, early escape
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
		candidate=switch(diffMethod){
			case DiffX -> searchMovesDiffX(moves,dest,startX,capture);//search by differentiation on the X coordinate
			case DiffY -> searchMovesDiffY(moves,dest,startY,capture);//search by differentiation on the Y coordinate
			default -> searchMovesNoDiff(moves,dest,capture);//if no differentiation, search by destination only
		};
		//handle promotions down here
		return switch(promotedInitial){
			case 'P','K' -> Move.blank();//invalid initials means invalid move
			case 'Q','N','R','B' -> Move.makePromotion(candidate,PieceCode.encodeChar(promotedInitial,player));
			default -> candidate;
		};
	}

	/**
	 * Search for moves based only on the destination
	 * @param moves     ArrayList of move integers
	 * @param dest      The destination square
	 * @param isCapture Should we be looking for a capture move?
	 * @return An encoded move integer, either the matching move or a blank move if nothing found
	 */
	private static int searchMovesNoDiff(ArrayList<Integer> moves,int dest,boolean isCapture){
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
	private static int searchMovesDiffX(ArrayList<Integer> moves,int dest,int startX,boolean isCapture){
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
	private static int searchMovesDiffY(ArrayList<Integer> moves,int dest,int startY,boolean isCapture){
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
		if(board.getSquare(Move.getEndIndex(move))!=PieceCode.Blank || Move.isCapture(move)) pgn="x"+pgn;//if a capture, then prepend an x before the coordinate
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
		else if(Move.isPawnPromotion(move)) return pgn+"="+charUppercase(PieceCode.decodeChar(code));//append the promoted Piece abbreviation if a promotion
		else return charUppercase(PieceCode.decodeChar(code))+pgn;//prepend the uppercase piece initial, return the token
	}

	/**
	 * Generates a FEN string from the board state
	 * @param board     The board in its current state
	 * @param team      WHITE or BLACK, Who goes next
	 * @param halfClock The halfMove clock (50 means a draw)
	 * @param fullMoves How many turns have elapsed
	 * @return A FEN formatted string
	 */
	public static String generateFEN(Board board,boolean team,int halfClock,int fullMoves){
		int empty=0;//no empty spaces yet
		long EnPassant=board.getEnPassant();
		boolean foundACastle=false;//assume no castles found until they are
		char piece;
		String fen="";
		//piece locations
		for(int y=XYMAX; y>=0; --y){//top to bottom (8th rank, 7th row down to 0th row)
			for(int x=0; x<BOARD_SIZE; ++x){//sweep across the rank
				piece=PieceCode.decodeChar(board.getSquare(x,y));
				if(piece==' '){
					++empty;//count empty spaces
				}else{//if there is a piece
					if(empty>0){//if there were empty squares
						fen+=empty;//print how many
						empty=0;//then reset
					}
					fen+=piece;//then print the piece
				}
			}
			if(empty>0){//if there were empty squares
				fen+=empty;//print how many
				empty=0;//then reset
			}
			if(y>0) fen+='/';//print a slash rank separator if not on bottom rank
		}
		//Who's turn
		fen+=(team==WHITE)? " w " : " b ";//print which player goes next
		//castling
		if(board.hasNotMoved(Coord.XYToIndex(Board.KRookX,0)) &&//castling for WHITE, check bottom rank
				board.hasNotMoved(Coord.XYToIndex(Board.KingX,0))){//King side castle, ask board if the rook and king have ever moved
			foundACastle=true;//tell the system to not print a blank
			fen+='K';
		}
		if(board.hasNotMoved(Coord.XYToIndex(Board.QRookX,0)) &&
				board.hasNotMoved(Coord.XYToIndex(Board.QueenX,0))){//Queen side castle, ask board if the rook and king have ever moved
			foundACastle=true;//tell the system to not print a blank
			fen+='Q';
		}
		if(board.hasNotMoved(Coord.XYToIndex(Board.KRookX,7)) &&//castling for BLACK, Check top rank
				board.hasNotMoved(Coord.XYToIndex(Board.KingX,7))){//King side castle, ask board if the rook and king have ever moved
			foundACastle=true;//tell the system to not print a blank
			fen+='k';
		}
		if(board.hasNotMoved(Coord.XYToIndex(Board.QRookX,7)) &&
				board.hasNotMoved(Coord.XYToIndex(Board.QueenX,7))){//Queen side castle, ask board if the rook and king have ever moved
			foundACastle=true;//tell the system to not print a blank
			fen+='q';
		}
		if(!foundACastle) fen+='-';//if no castling moves found, just print a blank placeholder
		//EnPassant
		if(EnPassant==0) fen+=" -";//if no EnPassant squares, then print a blank
		else if(team==WHITE){//if this turn is WHITE, then the last player was BLACK
			fen+=' '+Coord.indexToPGN(Coord.shiftIndex(Coord.maskToIndex(EnPassant),0,1));//get index, shift it because we store it differently than FEN
		}else{//the last player was WHITE
			fen+=' '+Coord.indexToPGN(Coord.shiftIndex(Coord.maskToIndex(EnPassant),0,-1));//same as above, but go downwards
		}
		fen+=" "+halfClock+" "+fullMoves;//add the half and full move clocks
		return fen;
	}

	/**
	 * Takes a FEN string and applies its state to a new Board instance
	 * @param fen The FEN string
	 * @return A Board with the FEN applied
	 */
	public static Board applyFEN(String fen){
		Board board=new Board(Board.CLEAR);
		String[] FEN_Parts=fen.split("\\s");//split on whitespace
		String fenBlk=FEN_Parts[0];
		int y=XYMAX, x=0;
		long unMovedMask=0;//0 out the mask
		boolean team;
		for(int i=0; i<fenBlk.length() && fenBlk.charAt(i)!=' ' && y>=0; ++i){//Extract Piece positions
			if(fenBlk.charAt(i)=='/' || fenBlk.charAt(i)=='\\'){//if a line separator (tolerate backslashes)
				--y;//move down a row
				x=0;//start back at beginning of the row
			}else if(fenBlk.charAt(i)>='0' && fenBlk.charAt(i)<='9'){//if a number
				x+=Coord.fromNumeral(fenBlk.charAt(i));//move forward and don't place anything on the blank squares
			}else{//only possible valid case is a piece character
				board.setSquare(PieceCode.encodeChar(fenBlk.charAt(i)),x,y);//attempt to set a piece
				++x;//move forward
			}
		}
		team=(FEN_Parts[1].equalsIgnoreCase("w"));//get who plays next w=WHITE=true
		fenBlk=FEN_Parts[2];//get castling rights, set all un moved squares
		for(int i=0; i<fenBlk.length(); ++i){
			switch(fenBlk.charAt(i)){
				case 'K'://WHITE king side
					unMovedMask|=KSideCastle;
					break;
				case 'Q'://WHITE queen side
					unMovedMask|=QSideCastle;
					break;
				case 'k'://BLACK king side
					unMovedMask|=Coord.shiftMask(KSideCastle,0,XYMAX);//shift up to the top row
					break;
				case 'q'://BLACK queen side
					unMovedMask|=Coord.shiftMask(QSideCastle,0,XYMAX);//shift up to the top row
					break;
			}
		}
		unMovedMask|=Pawn.BLACK_Promotion_mask & board.searchPiece(PieceCode.PawnW);//Mark any pawns that are in the starting position as unmoved
		unMovedMask|=Pawn.WHITE_Promotion_mask & board.searchPiece(PieceCode.PawnB);//Use the other team's promotion mask because it's this team's starting position
		board.setHasNotMoved(unMovedMask);//apply the mask to the pieces that have not moved

		if(!FEN_Parts[3].equals("-")){//check if there is EnPassant vulnerability
			int EnPassant=Coord.PGNToIndex(FEN_Parts[3]);//get the integer index
			if(team==WHITE) EnPassant=Coord.shiftIndex(EnPassant,0,-1);//move towards the direction of travel of the previous player (i.e. not this player)
			else EnPassant=Coord.shiftIndex(EnPassant,0,1);//if this player is BLACK, last player was WHITE, move in WHITE's direction
			board.setEnPassant(Coord.indexToMask(EnPassant));
		}
		return board;
	}
}
