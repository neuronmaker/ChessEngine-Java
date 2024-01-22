package com.dalton.ChessEngine;

import java.util.Arrays;

import static com.dalton.ChessEngine.PieceCode.*;
import static com.dalton.ChessEngine.Types.*;

/**
 * The game board, stored as a bitboard
 * @author Dalton Herrewynen
 * @version 2.1
 */
public class Board{
	/** These are ints to make use of fast Switch statements possible */
	public static final int CLEAR=1, DEFAULT=0;
	/** To reduce magic numbers */
	public static final int KingXCoord=4;
	/** Squares where pieces have not yet moved */
	private long unmoved;
	/** Squares with pawns vulnerable to EnPassant */
	private long EnPassant;
	/** Positions of each piece type */
	private long[] pieces=new long[PIECE_TYPES];

	/**
	 * Creates a Board class from another Board.
	 * @param board The board to copy
	 */
	public Board(Board board){
		this.unmoved=board.unmoved;
		this.EnPassant=board.getEnPassant();
		System.arraycopy(board.pieces,0,this.pieces,0,PIECE_TYPES);
	}

	/** Default constructor creates a default board */
	public Board(){
		this(DEFAULT);
	}

	/**
	 * The constructor which populates the Board by calling populate methods
	 * @param State Clear for blank board, Default for a chess game
	 */
	public Board(int State){
		switch(State){
			case CLEAR:
				populateBlankBoard();
				break;
			case DEFAULT:
			default:
				populateGameBoard();
				break;
		}
	}

	/** Blanks out the board */
	private void populateBlankBoard(){
		unmoved=0;
		EnPassant=0;
		Arrays.fill(pieces,0);
	}

	/** Set up a default board with pieces ready for a normal game */
	private void populateGameBoard(){
		populateBlankBoard();//blank out the board first
		setSquare(RookW,Coord.XYToIndex(0,0));
		setSquare(KnightW,Coord.XYToIndex(1,0));
		setSquare(BishopW,Coord.XYToIndex(2,0));
		setSquare(QueenW,Coord.XYToIndex(3,0));
		setSquare(KingW,Coord.XYToIndex(4,0));
		setSquare(BishopW,Coord.XYToIndex(5,0));
		setSquare(KnightW,Coord.XYToIndex(6,0));
		setSquare(RookW,Coord.XYToIndex(7,0));
		for(int i=0; i<BOARD_SIZE; ++i){
			setSquare(PawnW,Coord.XYToIndex(i,1));
			setSquare(PawnB,Coord.XYToIndex(i,6));
		}
		setSquare(RookB,Coord.XYToIndex(0,7));
		setSquare(KnightB,Coord.XYToIndex(1,7));
		setSquare(BishopB,Coord.XYToIndex(2,7));
		setSquare(QueenB,Coord.XYToIndex(3,7));
		setSquare(KingB,Coord.XYToIndex(4,7));
		setSquare(BishopB,Coord.XYToIndex(5,7));
		setSquare(KnightB,Coord.XYToIndex(6,7));
		setSquare(RookB,Coord.XYToIndex(7,7));
		setAllNotMoved();//mark all pieces as not moved
	}

	/** Sets all pieces on the board to think they've not been moved yet */
	public void setAllNotMoved(){
		unmoved=0;
		EnPassant=0;
		for(int i=0; i<PIECE_TYPES; ++i) unmoved|=pieces[i];//any piece will write a 1 to the has not moved place
	}

	/**
	 * Forces the given square(s) to be marked as not moved
	 * @param mask The square or squares to mark as not moved
	 */
	public void setHasNotMoved(long mask){
		unmoved|=mask;//if either has any square, flip to a 1 and mark not moved
		EnPassant=EnPassant & ~mask;//any bits in the mask should cancel the EnPassant
	}

	/**
	 * Gets all squares that have not been moved yet and are not blank
	 * @return long (64bit integer) bit mask
	 */
	public long getUnmoved(){
		long result=unmoved;
		for(int i=0; i<PIECE_TYPES; ++i){
			result&=pieces[i];
		}
		return result;
	}

	/**
	 * Checks if a piece has moved or not by its square index
	 * @param index The square to check
	 * @return True if square has piece that has not been moved, False otherwise.
	 */
	public boolean hasNotMoved(int index){
		return (unmoved & indexToMask(index))!=0;//use masking to check any square
	}

	/**
	 * Sets square(s) to a given piece and makes the piece(s) marked as moved
	 * @param code The piece code to set
	 * @param mask The mask representing all the squares to set
	 */
	public void setSquare(int code,long mask){
		mask=~mask;//make backwards for deletion of bits
		unmoved=unmoved & mask;
		EnPassant=0;//any move will cancel the EnPassant vulnerability
		for(int i=0; i<PIECE_TYPES; ++i){
			pieces[i]=pieces[i] & mask;//blank out the squares in the mask
		}
		if(code>=0 && code<PIECE_TYPES) pieces[code]|=~mask;//set this square to this code if the code is valid
	}

	/**
	 * Sets a square based to a piece code, makes the piece think it's been moved
	 * @param code  Which piece based on the code
	 * @param index Which square
	 */
	public void setSquare(int code,int index){
		setSquare(code,indexToMask(index));//jump straight to this one square
	}

	/**
	 * Sets a square based on the x and y coordinate values
	 * Calls the masked <code>setSquare(long mask)</code> method internally
	 * @param code The piece code to set
	 * @param x    X Coordinate
	 * @param y    Y Coordinate
	 */
	public void setSquare(int code,int x,int y){
		setSquare(code,Coord.XYToIndex(x,y));
	}

	/**
	 * Get the code of the piece at a given square
	 * Because of bit masking it is possible to check several squares at once but not useful to us (yet)
	 * @param mask bitmask for the square to get
	 * @return Integer piece code
	 */
	public int getSquare(long mask){
		for(int i=0; i<PIECE_TYPES; ++i){
			if((pieces[i] & mask)!=0) return i;//see why I used final ints now
		}
		return Blank;
	}

	/**
	 * Gets the code of the piece at a given square
	 * Just a wrapper for <code>getSquare(long mask)</code>
	 * @param index the square to check
	 * @return Integer piece code
	 */
	public int getSquare(int index){
		return getSquare(indexToMask(index));
	}

	/**
	 * Gets the code of the piece at a given square
	 * Just a wrapper for <code>getSquare(long mask)</code>
	 * @param x X Coordinate
	 * @param y Y Coordinate
	 * @return Integer piece code
	 */
	public int getSquare(int x,int y){
		return getSquare(Coord.XYToIndex(x,y));
	}

	/**
	 * Masks out which squares are vulnerable to en-passant rule, basic dumb getter
	 * @return 64 bit mask
	 */
	public long getEnPassant(){
		return EnPassant;
	}

	/**
	 * Sets squares as vulnerable to EnPassant
	 * @param mask The 64 bit mask of squares to set
	 */
	public void setEnPassant(long mask){
		EnPassant=mask;//override EnPassant mask with new mask
	}

	/**
	 * Returns a bitmask of all the pieces that are on the board based on their code
	 * @param pieceCode The piece code to get
	 * @return long (64bit integer) bit mask
	 * @see PieceCode
	 */
	public long searchPiece(int pieceCode){
		return pieces[pieceCode];
	}

	/**
	 * Rotates the current Board object 180 degrees
	 * @return A copy of the Board object that is rotated 180 degrees
	 */
	public Board getRotatedBoard(){
		Board rotatedBoard=new Board(CLEAR);
		for(int i=0; i<PIECE_TYPES; ++i){//for each piece
			rotatedBoard.pieces[i]=reverseMask(pieces[i]);
		}
		rotatedBoard.unmoved=reverseMask(unmoved);
		return rotatedBoard;
	}

	/** Rotates the current Board object 180 degrees */
	public void rotateBoard(){
		for(int i=0; i<PIECE_TYPES; ++i){//for each piece
			pieces[i]=reverseMask(pieces[i]);
		}
		unmoved=reverseMask(unmoved);
	}

	/**
	 * Applies a move to the board, this version uses a move encoded as a single integer value (sorry Andrew, speed matters here)
	 * @param move The move data encoded into a single integer
	 * @see Move
	 */
	public void makeMove(int move){
		switch(Move.getSpecialCode(move)){//calculate what type of move this is
			case Move.blankMove://if a blank move tag is detected
				break;//do nothing, ignore blank moves
			case Move.kSideCastle://king side castling
				kingSideCastle(move);
				break;
			case Move.qSideCastle://queen side castling
				queenSideCastle(move);
				break;
			case Move.pawnDoubleMove://for double move, make the move, then set the en passant vulnerability
				setSquare(Blank,Move.getStartIndex(move));//blank out where we were
				setSquare(Move.getPieceCode(move),Move.getEndIndex(move));//move
				setEnPassant(indexToMask(Move.getEndIndex(move)));//record vulnerability
				break;
			case Move.EnPassantCapture://delete the square behind the pawn that moved because it captures that square
				setSquare(Blank,EnPassant | Move.getStartMask(move));//Delete the only piece that was vulnerable to EnPassant, and the pawn that does the capturing
				setSquare(Move.getPieceCode(move),Move.getEndIndex(move));//move the capturing pawn
				break;
			default://pawn promotion, capture, or normal move
				setSquare(Blank,Move.getStartIndex(move));//blank the square
				setSquare(Move.getPieceCode(move),Move.getEndIndex(move));//set to destination to the encoded piece code (pawn promotion will have other piece, normal moves have same piece)
		}
	}

	/**
	 * Handles the king side castling for both teams
	 * @param move the encoded move integer
	 */
	private void kingSideCastle(int move){//TODO change to using just bit masking
		int y=Coord.indexToY(Move.getStartIndex(move));
		if(y==0){//if on WHITE side of board
			setSquare(KingW,6,0);
			setSquare(RookW,5,0);
		}else{//if on BLACK side of board
			setSquare(KingB,6,7);
			setSquare(RookB,5,7);
		}
		setSquare(Blank,4,y);
		setSquare(Blank,7,y);
	}

	/**
	 * Handles the queen side castling for both teams
	 * @param move the encoded move integer
	 */
	private void queenSideCastle(int move){//TODO change to using just bit masking
		int y=Coord.indexToY(Move.getStartIndex(move));
		if(y==0){//if on WHITE side of board
			setSquare(KingW,2,0);
			setSquare(RookW,3,0);
		}else{//if on BLACK side of board
			setSquare(KingB,2,7);
			setSquare(RookB,3,7);
		}
		setSquare(Blank,4,y);
		setSquare(Blank,0,y);
	}

	/**
	 * Copies the Board to a new Board instance
	 * @return A copy of this Board
	 */
	public Board saveState(){
		return new Board(this);
	}

	/**
	 * Loads an instance of a Board into this Board instance
	 * @param state A Board instance
	 */
	public void loadState(Board state){
		unmoved=state.unmoved;
		EnPassant=state.EnPassant;
		pieces=Arrays.copyOf(state.pieces,PIECE_TYPES);
	}

	/**
	 * Checks if a square is on the board
	 * @param x The row number
	 * @param y The column number
	 * @return True if square exists, False if not
	 */
	public static boolean isValidSquare(int x,int y){
		return Coord.isCoordValid(x,y);
	}

	/**
	 * Generates a mask for filtering one square at a time
	 * @param index which square to filter
	 * @return a long which is a mask with a single 1 in it
	 */
	public static long indexToMask(int index){
		return 1L << index;
	}

	/**
	 * Gets the mask of all squares with allied pieces on them
	 * @param team Which team to check
	 * @return long (64-bit integer) used as a bit mask
	 */
	public long alliedPieceMask(boolean team){
		long posMask=0;
		int i=(team) ? 0 : 1;//skip one if BLACK
		for(; i<PIECE_TYPES; i+=2){//move up by 2 so we skip enemy pieces
			posMask|=pieces[i];//bitwise or will add all allied pieces masks
		}
		return posMask;
	}

	/**
	 * Converts the board to ASCII art
	 * @return String of ASCII art
	 */
	public String toString(){
		StringBuilder output=new StringBuilder();
		output.append("   Black Side       Unmoved Pieces"//Header
				+"   EnPassant: "+Coord.orderedPair(Coord.maskToIndex(EnPassant))//En Passant Square
				+"\n=================<>=================");//top bar
		for(int y=BOARD_SIZE-1; y>=0; --y){
			output.append("\n|");//start the line with a bar
			for(int x=0; x<BOARD_SIZE; ++x){
				int piece=getSquare(x,y);
				if(piece!=Blank){//print the piece's internal char representation
					output.append(PieceCode.decodeChar(piece)).append("|");
				}else{//blank squares are blank
					switch((y+x)%2){//using number trickery to generate a checkerboard pattern
						case 1://white squares
							output.append(" |");
							break;
						case 0://black squares
							output.append(".|");
							break;
					}
				}
			}
			output.append("<>|");
			for(int x=0; x<BOARD_SIZE; ++x){//print which squares are unmoved
				if(0!=(unmoved & (1L << Coord.XYToIndex(x,y)))) output.append("+|");//unmoved are marked with a thing
				else output.append(" |");
			}
			output.append("\n=================<>=================");//middle and bottom bars
		}
		output.append("\n   White Side       Unmoved Pieces");
		return output.toString();
	}
}
