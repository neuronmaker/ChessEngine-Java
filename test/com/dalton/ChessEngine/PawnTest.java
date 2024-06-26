package com.dalton.ChessEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static com.dalton.ChessEngine.Types.*;
import static com.dalton.ChessEngine.UtilsForTests.*;

/**
 * Tests for the Pawn piece Class
 * @author Dalton Herrewynen
 * @version 2
 */
public class PawnTest{
	Board board;
	Engine engine;
	ArrayList<Integer> gotMoves;

	@Before
	public void setup(){
		board=new Board(Board.CLEAR);
		gotMoves=new ArrayList<>();
		engine=new Engine(1,2);
	}

	@After
	public void tearDown(){
		board=null;
		gotMoves=null;
		engine=null;
	}

	/** Test the basic forward moves of the WHITE pawn */
	@Test
	public void testBasicMovesWHITE(){
		testBasicMovesCommon(WHITE);
	}

	/** Test the basic forward moves of the BLACK pawn */
	@Test
	public void testBasicMovesBLACK(){
		testBasicMovesCommon(BLACK);
	}

	/**
	 * Basic moves common method, both colors are similar
	 * @param team The team to test against
	 */
	public void testBasicMovesCommon(boolean team){
		Pawn piece;
		Coord pawnPos;
		int deltaY;
		for(int x=0; x<BOARD_SIZE; ++x){//test a whole row
			piece=new Pawn(team);
			if(team==WHITE){//I think the Ternary operator is easier to read in this case, but since you don't like it, here you go, it's not critical, they're both decent
				pawnPos=new Coord(x,1);
				deltaY=1;//if white, move up
			}else{
				pawnPos=new Coord(x,BOARD_SIZE-2);
				deltaY=-1;//if black, move down
			}
			//test first move, it can be one of two
			board.setSquare(piece.pieceCode,pawnPos.getIndex());
			board.setAllNotMoved();

			gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
			boolean singleSquareMove=UNSET, doubleSquareMove=UNSET;//move up or down 1 and 2 tiles respectively
			for(int move: gotMoves){
				int endX=Coord.indexToX(Move.getEndIndex(move));
				int endY=Coord.indexToY(Move.getEndIndex(move));
				assertEquals("X coordinate should not change",pawnPos.getX(),endX);
				if(endY==pawnPos.getY()+(deltaY)) singleSquareMove=SET;
				if(endY==pawnPos.getY()+(deltaY*2)) doubleSquareMove=SET;
			}
			assertTrue("Tile: "+pawnPos+" Did not find the move to go up/down by 1 square",singleSquareMove);
			assertTrue("Tile: "+pawnPos+" Did not find the move to go up/down by 2 squares",doubleSquareMove);
			board.setSquare(PieceCode.Blank,pawnPos.getIndex());
			//test the subsequent move
			pawnPos.addVector(0,deltaY);//simulate a move
			board.setSquare(piece.pieceCode,pawnPos.getIndex());
			singleSquareMove=UNSET;
			doubleSquareMove=UNSET;
			for(int move: gotMoves){
				int endX=Coord.indexToX(Move.getEndIndex(move));
				int endY=Coord.indexToY(Move.getEndIndex(move));
				assertEquals("X coordinate should not change",pawnPos.getX(),endX);
				if(endY==pawnPos.getY()+(deltaY)) singleSquareMove=SET;
				if(endY==pawnPos.getY()+(deltaY*2)) doubleSquareMove=SET;
			}
			assertTrue("Tile: "+pawnPos+" Did not find the move to go up/down by 1 square after a move",singleSquareMove);
			assertFalse("Tile: "+pawnPos+" Should not find the move to go up/down by 2 squares after a move",doubleSquareMove);
			board.setSquare(PieceCode.Blank,pawnPos.getIndex());
		}
	}

	/** Test the Move structure for WHITE Pawns */
	@Test
	public void testMoveAnatomyWHITE(){
		testMoveAnatomyCommon(WHITE);
	}

	/** Test the Move structure for BLACK Pawns */
	@Test
	public void testMoveAnatomyBLACK(){
		testMoveAnatomyCommon(BLACK);
	}

	/**
	 * Test the Move structure for Both Pawns
	 * @param team Which team to test for?
	 */
	public void testMoveAnatomyCommon(boolean team){
		Coord piecePos=new Coord(4,4);
		Pawn piece=new Pawn(team);
		board.setSquare(piece.pieceCode,piecePos.getIndex());

		gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
		assertFalse("There should be encoded move integers here",gotMoves.isEmpty());
		for(int move: gotMoves){
			assertEquals("Moves should have starting position correct",piecePos.toString(),Coord.orderedPair(Move.getStartIndex(move)));
			assertEquals("Move should be of Normal type (0)",Move.normalMove,Move.getSpecialCode(move));
			assertEquals("Piece code, converted to pretty printed name",PieceCode.decodePieceName(piece.pieceCode),Move.getPieceName(move));
		}
	}

	/** Test capture moves for BLACK pawn */
	@Test
	public void testCaptureBLACK(){
		testCaptureMovesCommon(BLACK);
	}

	/** Test capture moves for WHITE pawn */
	@Test
	public void testCaptureWHITE(){
		testCaptureMovesCommon(WHITE);
	}

	/**
	 * Test capture moves for either WHITE or BLACK Pawns
	 * @param team WHITE or BLACK to test
	 */
	public void testCaptureMovesCommon(boolean team){
		Coord pawnPos=new Coord(), enemyPos;
		Piece piece=new Pawn(team), friendly=new Queen(team), enemy=new Queen(!team);
		final int deltaY=(team==WHITE) ? 1 : -1;//if WHITE move up (Y positive) otherwise go down if BLACK (Y negative)
		final int posY=(team==WHITE) ? 1 : BOARD_SIZE-2;//if WHITE, start on row 1 like a game, if BLACK start 1 from top
		ArrayList<Integer> captureMoves, standardMoves;
		for(int i=0; i<BOARD_SIZE; ++i){
			pawnPos.setCoord(i,posY);
			board.setSquare(piece.pieceCode,pawnPos.getIndex());
			enemyPos=pawnPos.getShiftedCoord(-1,deltaY);//capture to the left
			if(enemyPos.isSet()==SET){//if enemy is on the board
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());

				gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
				captureMoves=findCaptures(gotMoves,board);
				standardMoves=findJustMoves(gotMoves,board);
				assertNotEquals("Tile "+pawnPos+" Left: Did not find a capture move",0,captureMoves.size());
				assertEquals("Tile "+pawnPos+" Left: Should move Pawn to captured square",enemyPos.toString(),Coord.orderedPair(Move.getEndIndex(captureMoves.get(0))));//Hack to make Coord turn index into human-readable string
				assertTrue("Tile: "+pawnPos+": capture move should flag capture",Move.isCapture(captureMoves.get(0)));
				assertNotEquals("Tile "+pawnPos+" Left: Did not find a standard move",0,standardMoves.size());

				board.setSquare(friendly.pieceCode,enemyPos.getIndex());//test friendly piece, same place as enemy

				gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
				captureMoves=findCaptures(gotMoves,board);
				standardMoves=findJustMoves(gotMoves,board);
				assertEquals("Tile "+pawnPos+" Left: Should not try to capture a friendly piece",0,captureMoves.size());
				assertNotEquals("Tile "+pawnPos+" Left: Did not find a standard move",0,standardMoves.size());
				board.setSquare(PieceCode.Blank,enemyPos.getIndex());//blank the square
			}

			enemyPos=pawnPos.getShiftedCoord(1,deltaY);//capture to the right
			if(enemyPos.isSet()==SET){//if enemy is on the board
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());

				gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
				captureMoves=findCaptures(gotMoves,board);
				standardMoves=findJustMoves(gotMoves,board);
				assertNotEquals("Tile "+pawnPos+" Right: Did not find a capture move",0,captureMoves.size());
				assertEquals("Tile "+pawnPos+" Right: Should move Pawn to captured square",enemyPos.toString(),Coord.orderedPair(Move.getEndIndex(captureMoves.get(0))));
				assertTrue("Tile: "+pawnPos+": capture move should flag capture",Move.isCapture(captureMoves.get(0)));
				assertNotEquals("Tile "+pawnPos+" Right: Did not find a standard move",0,standardMoves.size());

				board.setSquare(friendly.pieceCode,enemyPos.getIndex());//test friendly piece, same place as enemy

				gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
				captureMoves=findCaptures(gotMoves,board);
				standardMoves=findJustMoves(gotMoves,board);
				assertEquals("Tile "+pawnPos+" Right: Should not try to capture a friendly piece",0,captureMoves.size());
				assertNotEquals("Tile "+pawnPos+" Right: Did not find a standard move",0,standardMoves.size());
				board.setSquare(PieceCode.Blank,enemyPos.getIndex());//blank the square
			}

			enemyPos=pawnPos.getShiftedCoord(0,deltaY);//blocked
			board.setSquare(enemy.pieceCode,enemyPos.getIndex());

			gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
			captureMoves=findCaptures(gotMoves,board);
			standardMoves=findJustMoves(gotMoves,board);
			assertEquals("Tile "+pawnPos+" Should not find a capture move when pawn blocked and nothing to its diagonal"
					,0,captureMoves.size());
			assertEquals("Tile "+pawnPos+" Should not find a standard move with pawn blocked",0,standardMoves.size());

			board.setSquare(friendly.pieceCode,enemyPos.getIndex());//test friendly piece

			gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
			captureMoves=findCaptures(gotMoves,board);
			standardMoves=findJustMoves(gotMoves,board);
			assertEquals("Tile "+pawnPos+" Should not find a capture move when pawn blocked and nothing to its diagonal"
					,0,captureMoves.size());
			assertEquals("Tile "+pawnPos+" Should not find a standard move with pawn blocked",0,standardMoves.size());

			board.setSquare(PieceCode.Blank,enemyPos.getIndex());//blank the square
			board.setSquare(PieceCode.Blank,pawnPos.getIndex());//blank the square
		}
	}

	/** Calls the En Passant testing method set for BLACK pawns */
	@Test
	public void testEnPassantGenerationBLACK(){
		testEnPassantGenerationCommon(BLACK);
	}

	/** Calls the En Passant testing method set for WHITE pawns */
	@Test
	public void testEnPassantGenerationWHITE(){
		testEnPassantGenerationCommon(WHITE);
	}

	/**
	 * Test either team's EnPassant move generation and application. Select which team tries to do the En Passant capture
	 * @param team WHITE or BLACK
	 */
	public void testEnPassantGenerationCommon(boolean team){
		int enemyRow, doubleMoveRow, attackDirection, dblMove;
		Coord enemyPos, enemyDblMoveDest, friendlyPos, attackDest;
		int gotMove=Move.blank();
		Pawn friendly=new Pawn(team), enemy=new Pawn(!team);
		if(team==WHITE){//when WHITE does the capturing
			enemyRow=6;//place enemy pawns on row 6 (rank 7)
			doubleMoveRow=4;//Row 4 (Rank 5) is where WHITE pawns go and BLACK pawns move to
			attackDirection=1;//Capturing pawns are WHITE and move up
		}else{//When BLACK is capturing
			enemyRow=1;//place enemy pawns on row 1 (rank 2)
			doubleMoveRow=3;//Row 3 (Rank 4) is where BLACK pawns go and WHITE pawns move to
			attackDirection=-1;//Capturing pawns are BLACK and move down
		}

		for(int x=0; x<BOARD_SIZE; ++x){//test cases when we should find En Passant moves
			enemyPos=new Coord(x,enemyRow);
			attackDest=enemyPos.getShiftedCoord(0,-attackDirection);//should be one square opposite direction of capturing
			enemyDblMoveDest=enemyPos.getShiftedCoord(0,-2*attackDirection);//enemy pawn double move destination, want test to work even if move generation fails so setting manually
			dblMove=Move.encode(Move.pawnDoubleMove,enemy.pieceCode,enemyPos.getIndex(),enemyDblMoveDest.getIndex());//the double move on start
			friendlyPos=new Coord(x-1,doubleMoveRow);//the attacking pawn, left of the attacked pawn, two spaces ahead of it
			if(friendlyPos.getSet()==SET){//test capturing pawn placed to the left if on the board
				board=new Board(Board.CLEAR);//blank the board
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());//set the pawn to be captured
				board.setAllNotMoved();//set the pawn to be captured as unmoved
				board.setSquare(friendly.pieceCode,friendlyPos.getIndex());//place attacking pawn

				board.makeMove(dblMove);//make the double move
				gotMove=friendly.EnPassant(board.getEnPassant(),board.alliedPieceMask(!friendly.team),friendlyPos.getIndex());//search for the EnPassant capture

				assertFalse("Left: Capturing Pawn: "+friendlyPos+" captured pawn: "+enemyPos+"->"+enemyDblMoveDest+" Should find 1 EnPassant move"
						,Move.isBlank(gotMove));
				assertEquals("Left: Capturing Pawn: "+friendlyPos+" Should have En Passant capture destination at"+attackDest
						,attackDest.getIndex(),Move.getEndIndex(gotMove));
			}

			friendlyPos=new Coord(x+1,doubleMoveRow);//the attacking pawn, right of the attacked pawn, two spaces ahead of it
			if(friendlyPos.getSet()==SET){//test capturing pawn placed to the right if on the board
				board=new Board(Board.CLEAR);//blank the board
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());//set the pawn to be captured
				board.setAllNotMoved();//set the pawn to be captured as unmoved
				board.setSquare(friendly.pieceCode,friendlyPos.getIndex());//place attacking pawn

				board.makeMove(dblMove);//make the double move
				gotMove=friendly.EnPassant(board.getEnPassant(),board.alliedPieceMask(!friendly.team),friendlyPos.getIndex());//search for the EnPassant capture

				assertFalse("Right: Capturing Pawn: "+friendlyPos+" captured pawn: "+enemyPos+"->"+enemyDblMoveDest+" Move should not be blank"
						,Move.isBlank(gotMove));
				assertEquals("right: Capturing Pawn: "+friendlyPos+" Should have En Passant capture destination at"+attackDest
						,attackDest.getIndex(),Move.getEndIndex(gotMove));
			}
		}
	}

	/** Calls the Promotion move generator tester for BLACK */
	@Test
	public void testPawnPromotionBLACK(){
		testPawnPromotionCommon(BLACK);
	}

	/** Calls the Promotion move generator tester for WHITE */
	@Test
	public void testPawnPromotionWHITE(){
		testPawnPromotionCommon(WHITE);
	}

	/**
	 * The test code for either team's promotion move generation
	 * @param team WHITE or BLACK
	 */
	public void testPawnPromotionCommon(boolean team){
		int lastRow=(team==WHITE)? XYMAX : 0;//if WHITE then go to top of board, if BLACK then go to bottom
		int direction=(team==WHITE)? 1 : -1;//if WHITE then go up, if BLACK then go down
		int pieceCodeOffset=(team==WHITE)? 0 : 1;//if WHITE then we don't add 1 to the codes, if BLACK we do because BLACK is on every other PieceCode
		int[] foundPromotions=new int[PieceCode.PIECE_TYPES];
		Coord before, after;
		Pawn pawn=new Pawn(team);

		for(int x=0; x<BOARD_SIZE; ++x){//loop over each square in the row
			Arrays.fill(foundPromotions,0);//0 out the array
			after=new Coord(x,lastRow);//the last row in this column
			before=after.getShiftedCoord(0,-direction);//the row behind the last row, note the negative sign
			String errMsgData="Tile "+before+"->"+after+" ";
			board=new Board(Board.CLEAR);
			board.setSquare(pawn.pieceCode,x,(lastRow-direction));

			gotMoves=Engine.getLegalMoves(board,pawn.pieceCode);
			for(int move: gotMoves){
				if(Move.isPawnPromotion(move))//for each promotion
					++foundPromotions[Move.getPieceCode(move)];//count each instance of each piece code
			}

			assertEquals(errMsgData+"Should not find a promotion to BLACK pawns",0,foundPromotions[PieceCode.PawnB]);//no pawns
			assertEquals(errMsgData+"Should not find a promotion to WHITE pawns",0,foundPromotions[PieceCode.PawnW]);
			assertEquals(errMsgData+"Should not find a promotion to BLACK kings",0,foundPromotions[PieceCode.KingB]);//no Kings
			assertEquals(errMsgData+"Should not find a promotion to WHITE kings",0,foundPromotions[PieceCode.KingW]);
			for(int i=pieceCodeOffset+PieceCode.RookW; i<PieceCode.KingW; i+=2){//search the other types, start at the Rooks because I put them just above the pawns, don't promote to a King
				assertEquals(errMsgData+"Should find 1 promotion move to "+PieceCode.decodePieceName(i),1,foundPromotions[i]);//look up each code count in the integer array
			}
		}
	}

	/**
	 * Get the correct attacking mask for a team
	 * @param pos  Where the Pawn is
	 * @param team WHITE or BLACK
	 * @return The Correct attack mask
	 */
	public long getCorrectAttackMask(final int pos, boolean team){
		long mask;
		int dy=(team==WHITE)?1:-1;
		if((Coord.indexToY(pos)>=XYMAX && team==WHITE) || (Coord.indexToY(pos)<=XYMIN && team==BLACK)){
			return 0;//do not generate the mask for squares the Pawn can't reach because it would be promoted
		}
		mask=switch(Coord.indexToX(pos)){
			case XYMIN ->//left side
					0b00000010L << ((Coord.indexToY(pos)+dy)*BOARD_SIZE);
			case XYMAX ->//right side
					0b01000000L << ((Coord.indexToY(pos)+dy)*BOARD_SIZE);
			default ->
					0b00000101L << (pos-1+dy*BOARD_SIZE);//mask is 1 space too far over because it won't fit otherwise
		};
		return mask;
	}
	/** Test attacking masks for both teams */
	@Test
	public void testAttackMask(){
		Pawn pawn=new Pawn(WHITE);//test WHITE first
		long blanks=0;
		for(int i=0; i<TOTAL_SQUARES; ++i){
			blanks|=1L<<i;
		}
		for(int i=0; Coord.indexToY(i)<XYMAX; ++i){
			String expected=maskString(getCorrectAttackMask(i,WHITE));
			String got=maskString(pawn.attackMask(0,blanks,i));
			assertEquals("At: "+Coord.orderedPair(i)+" "+Coord.indexToPGN(i),expected,got);
		}
		pawn=new Pawn(BLACK);//test BLACK next
		for(int i=Coord.XYToIndex(0,1); i<TOTAL_SQUARES; ++i){//first square of second row
			String expected=maskString(getCorrectAttackMask(i,BLACK));
			String got=maskString(pawn.attackMask(0,blanks,i));
			assertEquals("At: "+Coord.orderedPair(i)+" "+Coord.indexToPGN(i),expected,got);
		}
	}
}
