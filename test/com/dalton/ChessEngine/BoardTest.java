package com.dalton.ChessEngine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.dalton.ChessEngine.Types.*;
import static com.dalton.ChessEngine.PieceCode.*;

/**
 * Tests for Dalton's Board class (a bitboard implementation)
 * @author Dalton Herrewynen
 * @version 2
 */
public class BoardTest{
	Board board;//normal game board
	Board blankBoard;//blank
	Board customBoard;//specially crafted to show faults in setters and getters
	Board blankState, defaultState;
	int pawnRowNum, knightRowNum, rookRowNum;
	Coord KingPos, QueenPos;
	Piece expected, got;

	@Before
	public void setupBoard(){
		board=new Board(Board.DEFAULT);
		blankBoard=new Board(Board.CLEAR);
		defaultState=board.saveState();//save default state
		blankState=blankBoard.saveState();//save the blank board
		//customized board
		customBoard=new Board(Board.CLEAR);
		//positions and row numbers
		KingPos=new Coord(0,0);
		QueenPos=new Coord(0,XYMAX);
		pawnRowNum=1;//2nd from bottom row: pawns
		knightRowNum=3;//middle row: knights
		rookRowNum=BOARD_SIZE-2;//2nd from top row: rooks
		//load it
		customBoard.setSquare(PieceCode.KingB,KingPos.getIndex());//left bottom is king
		customBoard.setSquare(PieceCode.QueenB,QueenPos.getIndex());//left top is queen
		for(int i=0; i<BOARD_SIZE; ++i){
			customBoard.setSquare(PieceCode.PawnB,Coord.XYToIndex(i,pawnRowNum));
			customBoard.setSquare(PieceCode.KnightB,Coord.XYToIndex(i,knightRowNum));
			customBoard.setSquare(PieceCode.RookB,Coord.XYToIndex(i,rookRowNum));
		}
		customBoard.setAllNotMoved();//only needed for custom boards
		got=null;
		expected=null;
	}

	@After
	public void tearDownBoard(){
		board=null;
		blankBoard=null;
		customBoard=null;
		blankState=null;
		defaultState=null;
		got=null;
		expected=null;
	}

	/** Testing to be sure the square validity checker works */
	@Test
	public void testIsSquareValid(){
		for(int x=0; x<BOARD_SIZE; ++x){//testing all tiles on the board
			for(int y=0; y<BOARD_SIZE; ++y){
				assertTrue("Square ("+x+","+y+") should be on the board but isn't.",Board.isValidSquare(x,y));
			}
		}
		for(int i=-1; -BOARD_SIZE<i; --i){//start at -1 go to negative of BOARD_SIZE
			assertFalse("Square ("+i+",0) should be invalid but isn't",Board.isValidSquare(i,0));//testing negative row, valid column
			assertFalse("Square (0,"+i+") should be invalid but isn't",Board.isValidSquare(0,i));//testing valid row, negative column
		}
	}

	/** Tests the get square method */
	@Test
	public void testGetter(){
		//bottom left: King
		got=PieceCode.pieceObj(customBoard.getSquare(KingPos.getIndex()));
		expected=new King(BLACK);
		assertEquals("Bottom left",expected.toString(),got.toString());
		//top left: Queen
		got=PieceCode.pieceObj(customBoard.getSquare(QueenPos.getIndex()));
		expected=new Queen(BLACK);
		assertEquals("Top left",expected.toString(),got.toString());
		//test rows
		for(int i=0; i<BOARD_SIZE; ++i){
			//Pawns in row 2
			expected=new Pawn(BLACK);
			got=PieceCode.pieceObj(customBoard.getSquare(new Coord(i,pawnRowNum).getIndex()));
			assertEquals("At ("+i+","+pawnRowNum+")",expected.toString(),got.toString());
			//Knights in row 3
			expected=new Knight(BLACK);
			got=PieceCode.pieceObj(customBoard.getSquare(new Coord(i,knightRowNum).getIndex()));
			assertEquals("At ("+i+","+knightRowNum+")",expected.toString(),got.toString());
			//Rooks second from the top
			expected=new Rook(BLACK);
			got=PieceCode.pieceObj(customBoard.getSquare(new Coord(i,rookRowNum).getIndex()));
			assertEquals("At ("+i+","+rookRowNum+")",expected.toString(),got.toString());
		}
	}

	/** Testing setting a square from a mask */
	@Test
	public void testSetterMask(){
		long mask;
		for(long i=0; i<64; ++i){
			mask=(1L<<i);
			assertNull("Blank board should not have a piece at "+i,PieceCode.pieceObj(blankBoard.getSquare(mask)));
			expected=new Pawn(WHITE);
			blankBoard.setSquare(expected.pieceCode,mask);
			assertEquals("Blank board at "+i,expected.toString(),PieceCode.decodePieceName(blankBoard.getSquare(mask)));
		}
	}

	/** Testing setting a square from a Coord object */
	@Test
	public void testSetterXYInts(){
		for(int i=0; i<TOTAL_SQUARES; ++i){//Loop over all squares
			assertEquals("Blank board should not have a piece at "+Coord.orderedPair(i),PieceCode.Blank,blankBoard.getSquare(i));
			expected=new Pawn(WHITE);
			blankBoard.setSquare(expected.pieceCode,Coord.indexToX(i),Coord.indexToY(i));
			assertEquals("Blank board at "+Coord.orderedPair(i),expected.toString(),PieceCode.decodePieceName(blankBoard.getSquare(i)));
		}
	}

	/** Test searching pieces */
	@Test
	public void testPieceSearch(){
		int gotIndex;
		char pieceChar, searchChar;
		for(int code=0; code<PieceCode.PIECE_TYPES; ++code){//test on all pieces
			pieceChar=PieceCode.decodeChar(code);//set the human friendly Piece Char for this code
			for(int i=0; i<TOTAL_SQUARES; ++i){//test placed on all squares
				board.loadState(blankState);//blank it out
				board.setSquare(code,i);//set one and only one piece
				for(int searchCode=0; searchCode<PieceCode.PIECE_TYPES; ++searchCode){//search all piece types
					searchChar=PieceCode.decodeChar(searchCode);//Human friendly piece code we are looking for
					int foundIndex=Coord.maskToIndex(board.searchPiece(searchCode));//search all squares for this code
					if(searchCode!=code){//if this code is not the same code I placed, expect a failure flag
						assertEquals("Square "+i+" piece "+pieceChar+" found "+searchChar+" at "+foundIndex+
								" Should not have found anything square "+foundIndex+" content "+
								PieceCode.decodePieceName(board.getSquare(foundIndex)),Coord.ERROR_INDEX,foundIndex);
					}else{//if same code searching as set, then expect same index as I set earlier
						assertEquals("Square "+i+" piece "+pieceChar+" found at incorrect Index, square "+
										foundIndex+" content "+PieceCode.decodePieceName(board.getSquare(foundIndex)),
								i,foundIndex);//expect found index to be same as i
					}
				}
			}
		}
	}

	/** Tests conversion of index to a single bit mask */
	@Test
	public void indexMaskTest(){
		long expected=1L;
		for(int i=0; i<64; ++i){//longs always have 64 bits as per definition of a long
			long mask=Board.indexToMask(i);
			assertEquals("Position: "+i,expected,mask);
			expected*=2;
		}
	}

	/** Test tracking of which pieces have and have not been moved */
	@Test
	public void testHasMovedTrack(){
		for(int i=0; i<TOTAL_SQUARES; ++i){//Loop over all squares
			blankBoard=new Board(Board.CLEAR);
			blankBoard.setSquare(PieceCode.PawnW,i);
			blankBoard.setAllNotMoved();
			assertTrue("Piece has not moved yet, should not be seen as moved",blankBoard.hasNotMoved(i));
			blankBoard.setSquare(PieceCode.PawnB,i);//set a piece, same or otherwise, should mean the flag gets tripped and the piece has now moved
			assertFalse("Piece has now been moved, should be marked as such",blankBoard.hasNotMoved(i));
		}
	}

	/** Test the ability to save and load a board state */
	@Test
	public void testLoadSaveState(){
		Board state=board.saveState();
		blankBoard.loadState(state);
		for(int i=0; i<TOTAL_SQUARES; ++i){//Loop over all squares
			assertEquals("Tile: "+Coord.orderedPair(i)+" pieces should match after load",board.getSquare(i),blankBoard.getSquare(i));
			assertEquals("Tile: "+Coord.orderedPair(i)+" EnPassant should match after load",board.getEnPassant() & (1L<<i),blankBoard.getEnPassant() & (1L<<i));
			assertEquals("Tile: "+Coord.orderedPair(i)+" hasMoved() should match after load",board.hasNotMoved(i),blankBoard.hasNotMoved(i));
		}
	}

	/** Test the ability to load from another board object */
	@Test
	public void testLoadBoard(){
		blankBoard.loadState(board);
		for(int i=0; i<TOTAL_SQUARES; ++i){//Loop over all squares
			assertEquals("Tile: "+Coord.orderedPair(i)+" pieces should match after load",board.getSquare(i),blankBoard.getSquare(i));
			assertEquals("Tile: "+Coord.orderedPair(i)+" EnPassant should match after load",board.getEnPassant() & (1L<<i),blankBoard.getEnPassant() & (1L<<i));
			assertEquals("Tile: "+Coord.orderedPair(i)+" hasMoved() should match after load",board.hasNotMoved(i),blankBoard.hasNotMoved(i));
		}
	}

	/** Testing the ability to make a move using an encoded move integer */
	@Test
	public void testMakeMove(){
		int pieceCode=PieceCode.QueenW, dest, move;
		for(int start=0; start<TOTAL_SQUARES; ++start){//going to test basic moves by placing a queen and moving it around manually
			board.loadState(blankState);//blank out the board
			if(Coord.indexToY(start)<7){//if not on top row, go up one row
				dest=Coord.XYToIndex(Coord.indexToX(start),Coord.indexToY(start)+1);
			}else{//if on top row go down a row
				dest=Coord.XYToIndex(Coord.indexToX(start),Coord.indexToY(start)-1);
			}
			move=Move.encodeNormal(pieceCode,start,dest);//encode the move
			board.setSquare(pieceCode,start);//set the piece on the square
			board.makeMove(move);
			assertEquals("Move "+start+" -> "+dest+" Incorrect piece destination "+Coord.maskToIndex(board.searchPiece(pieceCode))
					,pieceCode,board.getSquare(dest));//check if it's actually present where it should be
		}
	}

	/** King Side castling ability */
	@Test
	public void testKingSideCastling(){
		int move;
		//Test WHITE side
		Coord expectedCastle=new Coord(5,0), expectedKing=new Coord(6,0);//where the castles and kings should go to
		Coord placeCastle=new Coord(7,0), placeKing=new Coord(4,0);//where to place the kings and castles

		board.loadState(blankState);//Set up the board, load pieces, set as not moved
		board.setSquare(KingW,placeKing.getIndex());
		board.setSquare(RookW,placeCastle.getIndex());
		board.setAllNotMoved();//make all not moved yet

		move=Move.encode(Move.kSideCastle,KingW,placeKing.getIndex(),0);//end coordinates are irrelevant for castling, only record position of the king
		board.makeMove(move);//generate and make the move

		//check if it was successful
		assertEquals("Expected a WHITE King at "+expectedKing,decodePieceName(KingW),decodePieceName(board.getSquare(expectedKing.getIndex())));
		assertEquals("Expected a WHITE Rook at "+expectedCastle,decodePieceName(RookW),decodePieceName(board.getSquare(expectedCastle.getIndex())));

		//Test BLACK side
		expectedCastle.addVector(0,7);
		expectedKing.addVector(0,7);
		placeCastle.addVector(0,7);
		placeKing.addVector(0,7);//Move up to BLACK side

		board.loadState(blankState);//Set up the board, load pieces, set as not moved
		board.setSquare(KingB,placeKing.getIndex());
		board.setSquare(RookB,placeCastle.getIndex());
		board.setAllNotMoved();//make all not moved yet

		move=Move.encode(Move.kSideCastle,KingB,placeKing.getIndex(),0);//end coordinates are irrelevant for castling, only record position of the king
		board.makeMove(move);//generate and make the move

		//check if it was successful
		assertEquals("Expected a BLACK King at "+expectedKing,decodePieceName(KingB),decodePieceName(board.getSquare(expectedKing.getIndex())));
		assertEquals("Expected a BLACK Rook at "+expectedCastle,decodePieceName(RookB),decodePieceName(board.getSquare(expectedCastle.getIndex())));
	}

	/** Queen Side castling ability */
	@Test
	public void testQueenSideCastling(){
		int move;
		//Test WHITE side
		Coord expectedCastle=new Coord(3,0), expectedKing=new Coord(2,0);//where the castles and kings should go to
		Coord placeCastle=new Coord(0,0), placeKing=new Coord(4,0);//where to place the kings and castles

		board.loadState(blankState);//set up the board, load pieces
		board.setSquare(KingW,placeKing.getIndex());
		board.setSquare(RookW,placeCastle.getIndex());
		board.setAllNotMoved();//make all not moved yet

		move=Move.encode(Move.qSideCastle,KingW,placeKing.getIndex(),0);//end coordinates are irrelevant for castling, only record position of the king
		board.makeMove(move);//generate and make the move

		//Check if it was successful
		assertEquals("Expected a WHITE King at "+expectedKing,decodePieceName(KingW),decodePieceName(board.getSquare(expectedKing.getIndex())));
		assertEquals("Expected a WHITE Rook at "+expectedCastle,decodePieceName(RookW),decodePieceName(board.getSquare(expectedCastle.getIndex())));

		//Test BLACK side
		expectedCastle.addVector(0,7);
		expectedKing.addVector(0,7);
		placeCastle.addVector(0,7);
		placeKing.addVector(0,7);//Move up to BLACK side

		board.loadState(blankState);//set up the board, load pieces
		board.setSquare(KingB,placeKing.getIndex());
		board.setSquare(RookB,placeCastle.getIndex());
		board.setAllNotMoved();//make all not moved yet

		move=Move.encode(Move.qSideCastle,KingB,placeKing.getIndex(),0);//end coordinates are irrelevant for castling, only record position of the king
		board.makeMove(move);//generate and make the move

		//Check if it was successful
		assertEquals("Expected a BLACK King at "+expectedKing,decodePieceName(KingB),decodePieceName(board.getSquare(expectedKing.getIndex())));
		assertEquals("Expected a BLACK Rook at "+expectedCastle,decodePieceName(RookB),decodePieceName(board.getSquare(expectedCastle.getIndex())));
	}

	/** Test En Passant rules for BLACK */
	@Test
	public void testEnPassantBLACK(){
		int move;
		Coord start=new Coord(), end;
		for(int i=0; i<BOARD_SIZE; ++i){//Test for all Black pawns
			start.setCoord(i,BOARD_SIZE-2);//black pawns 1 row from top
			end=start.getShiftedCoord(0,-2);//go down by 2

			assertEquals("Tile: "+start+" -> "+end+" Should not EnPassant the starting position, Mask filters only this square so the result should be all 0's"//Check that we cannot Enpassant the start square
					,0,board.getEnPassant() & (1L<<start.getIndex()));//with masking just the start square
			assertEquals("Tile: "+start+" -> "+end+" Should not EnPassant the ending position, Mask filters only this square so the result should be all 0's"//Check that we cannot Enpassant the End square
					,0,board.getEnPassant() & (1L<<end.getIndex()));//with masking just the end square

			move=Move.encode(Move.pawnDoubleMove,PawnB,start.getIndex(),end.getIndex());//generate the move
			board.makeMove(move);//make the move

			assertEquals("Tile: "+start+" -> "+end+" Should not EnPassant the starting position, Mask filters only this square so the result should be all 0's"//Check that we cannot Enpassant the start square
					,0,board.getEnPassant() & (1L<<start.getIndex()));//with masking just the start square
			assertEquals("Tile: "+start+" -> "+end+" Should EnPassant the ending position, Mask filter extracts just this square if correct"//Check that we can Enpassant the End square
					,end.getIndex(),Coord.maskToIndex(board.getEnPassant() & (1L<<end.getIndex())));//with masking just the end square
		}
	}

	/** Test En Passant rules for WHITE */
	@Test
	public void testEnPassantWHITE(){
		int move;
		Coord start=new Coord(), end;
		for(int i=0; i<BOARD_SIZE; ++i){//Test for all White pawns
			start.setCoord(i,1);//white pawns on bottom
			end=start.getShiftedCoord(0,2);//go up by 2

			assertEquals("Tile: "+start+" -> "+end+" Should not EnPassant the starting position, Mask filters only this square so the result should be all 0's"//Check that we cannot Enpassant the start square
					,0,board.getEnPassant() & (1L<<start.getIndex()));//with masking just the start square
			assertEquals("Tile: "+start+" -> "+end+" Should not EnPassant the ending position, Mask filters only this square so the result should be all 0's"//Check that we cannot Enpassant the End square
					,0,board.getEnPassant() & (1L<<end.getIndex()));//with masking just the end square

			move=Move.encode(Move.pawnDoubleMove,PawnW,start.getIndex(),end.getIndex());//generate the move
			board.makeMove(move);//make the move

			assertEquals("Tile: "+start+" -> "+end+" Should Not EnPassant the starting position, Mask filters only this square so the result should be all 0's"//Check that we cannot Enpassant the start square
					,0,board.getEnPassant() & (1L<<start.getIndex()));//with masking just the start square
			assertEquals("Tile: "+start+" -> "+end+" Should EnPassant the ending position, Mask filter extracts just this square if correct"//Check that we can Enpassant the End square
					,end.getIndex(),Coord.maskToIndex(board.getEnPassant() & (1L<<end.getIndex())));//with masking just the end square
		}
	}

	/** Test searching of all allied pieces */
	@Test
	public void testAlliedPiecesSearch(){
		long alliedMask;
		String errPrefix;
		for(int piece=0; piece<PIECE_TYPES; ++piece){//for each piece
			for(int i=0; i<TOTAL_SQUARES; ++i){//test placing at each square
				errPrefix="Tile:"+Coord.orderedPair(i)+" piece: "+PieceCode.decodeChar(piece)+" ";
				board=new Board(Board.CLEAR);//blank the board
				board.setSquare(piece,i);//place a piece at this square
				alliedMask=board.alliedPieceMask(!PieceCode.decodeTeam(piece));//get for the other team (works for both teams)
				assertEquals(board+errPrefix+"Should not find any Pieces for the other team",0,alliedMask);
				alliedMask=board.alliedPieceMask(PieceCode.decodeTeam(piece));//get for this team
				assertEquals(board+errPrefix+"Incorrect allied Piece mask (piece: "+PieceCode.decodePieceName(piece)+
								" Location: expected: "+Coord.orderedPair(i)+" got: "+Coord.orderedPair(Coord.maskToIndex(alliedMask))
						,i,Coord.maskToIndex(alliedMask));
			}
		}
	}

	/** Test memory pointers */
	@Test
	public void testMemoryPointersCopy(){
		Board testBoard=new Board(blankBoard);
		assertNotSame("Board copying should have copied content, not pointer",testBoard,blankBoard);
		for(int i=0; i<PIECE_TYPES; ++i){//test basic piece array copy
			blankBoard=new Board(Board.CLEAR);
			testBoard=new Board(blankBoard);
			blankBoard.setSquare(i,0,1);
			assertEquals("The piece should not have been copied from board to board since setting was after copy"
					,PieceCode.Blank,testBoard.getSquare(0,1));
		}

		for(int i=0; i<BOARD_SIZE; ++i){//test EnPassant flag copying
			blankBoard=new Board(Board.CLEAR);
			testBoard=new Board(blankBoard);
			blankBoard.setSquare(PawnW,i,1);
			blankBoard.setAllNotMoved();
			blankBoard.makeMove(Move.encode(Move.pawnDoubleMove,PawnW,Coord.XYToIndex(i,1),Coord.XYToIndex(i,3)));//make double move
			assertNotEquals("EnPassant flag should have been set (use other tests for its validity)",0,blankBoard.getEnPassant());
			assertNotEquals("The EnPassant should not have been copied from board to board since setting was after copy"
					,testBoard.getEnPassant(),blankBoard.getEnPassant());
		}
	}

	/** Test the copy constructor */
	@Test
	public void testCopyConstructor(){
		blankBoard=new Board(board);
		assertEquals("EnPassant masks need to match",board.getEnPassant(),blankBoard.getEnPassant());
		assertEquals("Move tracking masks need to match",board.getUnmoved(),blankBoard.getUnmoved());
		for(int i=0; i<PIECE_TYPES; ++i){
			assertEquals("Piece tracking masks for code: "+i+" need to match",board.searchPiece(i),blankBoard.searchPiece(i));
		}
	}
}
