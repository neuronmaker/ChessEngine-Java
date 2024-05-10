package com.dalton.ChessEngine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.Assert.*;
import static com.dalton.ChessEngine.Types.*;
import static com.dalton.ChessEngine.PieceCode.*;
import static com.dalton.ChessEngine.UtilsForTests.*;

/**
 * Tests for the Bishop piece class
 * @author Dalton Herrewynen
 * @version 2
 */
public class BishopTest{
	Board board;
	Engine engine;
	ArrayList<Integer> gotMoves, filteredMoves;
	ArrayList<Coord> gotCoords, expectedCoords;

	@Before
	public void setup(){
		board=new Board(Board.CLEAR);
		gotCoords=new ArrayList<>();
		expectedCoords=new ArrayList<>();
		gotMoves=new ArrayList<>();
		filteredMoves=new ArrayList<>();
		engine=new Engine(1,2);
	}

	@After
	public void tearDown(){
		board=null;
		gotCoords=null;
		expectedCoords=null;
		gotMoves=null;
		filteredMoves=null;
		engine=null;
	}

	/** Test basic movement for WHITE Bishop, do not test capture */
	@Test
	public void testBasicMoveWHITE(){
		testBasicMoveCommon(WHITE);
	}

	/** Test basic movement for BLACK Bishop, do not test capture */
	@Test
	public void testBasicMoveBLACK(){
		testBasicMoveCommon(BLACK);
	}

	/**
	 * Tests each color's basic moves to avoid code re-use
	 * @param team WHITE or BLACK
	 */
	public void testBasicMoveCommon(boolean team){
		Bishop piece=new Bishop(team);
		int pieceCode=piece.pieceCode;
		Coord piecePos=new Coord();
		for(int i=0; i<TOTAL_SQUARES; ++i){//test on all tiles
			piecePos.setIndex(i);
			board.setSquare(pieceCode,piecePos.getIndex());

			gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
			gotCoords=getDestCoords(gotMoves);

			expectedCoords=new ArrayList<>();
			expectedCoords.addAll(getLineOfCoords(piecePos,1,1));
			expectedCoords.addAll(getLineOfCoords(piecePos,1,-1));
			expectedCoords.addAll(getLineOfCoords(piecePos,-1,1));
			expectedCoords.addAll(getLineOfCoords(piecePos,-1,-1));

			gotCoords.sort(Comparator.comparingInt(Coord::getIndex));//sort the coord, so they are in same order and comparisons will work
			expectedCoords.sort(Comparator.comparingInt(Coord::getIndex));

			for(int move: gotMoves){//test correct piece code
				assertEquals("Piece code should match the Bishop",pieceCode,Move.getPieceCode(move));
			}

			assertEquals("Square "+piecePos+": Must have same number of Coord in the got and expected list",
					expectedCoords.size(),gotCoords.size());
			for(int j=0; j<gotCoords.size(); ++j){
				assertEquals("Square "+piecePos+": Move "+j+" destinations don't match",
						expectedCoords.get(j).toString(),gotCoords.get(j).toString());
			}
			board.setSquare(Blank,piecePos.getIndex());//remove this piece and start again
		}
	}

	/** Test the Move structure WHITE */
	@Test
	public void testMoveAnatomyWHITE(){
		testMoveAnatomyCommon(WHITE);
	}

	/** Test the Move structure BLACK */
	@Test
	public void testMoveAnatomyBLACK(){
		testMoveAnatomyCommon(BLACK);
	}

	/**
	 * The move anatomy tester for both teams to avoid code re-use
	 * @param team WHITE or BLACK
	 */
	public void testMoveAnatomyCommon(boolean team){
		Coord piecePos=new Coord(4,4);
		Bishop piece=new Bishop(team);
		board.setSquare(piece.pieceCode,piecePos.getIndex());
		gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
		assertFalse("There should be encoded move integers here",gotMoves.isEmpty());
		for(int move: gotMoves){
			assertEquals("Moves should have starting position correct",piecePos.toString(),Coord.orderedPair(Move.getStartIndex(move)));
			assertEquals("Move should be of Normal type (0)",Move.normalMove,Move.getSpecialCode(move));
			assertEquals("Piece code, converted to pretty printed name",PieceCode.decodePieceName(piece.pieceCode),Move.getPieceName(move));
		}
	}

	/** Test BLACK Bishop Captures */
	@Test
	public void testBLACKCapture(){
		testCaptureCommon(BLACK);
	}

	/** Test WHITE Bishop Captures */
	@Test
	public void testWHITECapture(){
		testCaptureCommon(WHITE);
	}

	/**
	 * Test captures for either team, void code re-use
	 * @param team WHITE or BLACK
	 */
	public void testCaptureCommon(boolean team){
		Bishop piece=new Bishop(team), friendly=new Bishop(team), enemy=new Bishop(!team);
		Coord piecePos=new Coord();
		ArrayList<Coord> reachableSquares=new ArrayList<>();

		for(int i=0; i<TOTAL_SQUARES; ++i){//testing all squares
			piecePos.setIndex(i);
			board.setSquare(piece.pieceCode,piecePos.getIndex());

			reachableSquares.clear();//reset
			reachableSquares.addAll(getLineOfCoords(piecePos,1,1));//4 diagonal directions
			reachableSquares.addAll(getLineOfCoords(piecePos,1,-1));
			reachableSquares.addAll(getLineOfCoords(piecePos,-1,1));
			reachableSquares.addAll(getLineOfCoords(piecePos,-1,-1));

			for(Coord enemyPos: reachableSquares){//Check ALL squares reachable by the Rook, one by one
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());//Test capture
				gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
				filteredMoves=findCaptures(gotMoves,board);
				assertEquals("Tile: "+piecePos+": Should not have exactly one capture",1,filteredMoves.size());
				assertEquals("Tile: "+piecePos+": capture move should end on the enemy piece",
						enemyPos.toString(),Coord.orderedPair(Move.getEndIndex(filteredMoves.get(0))));
				assertTrue("Tile: "+piecePos+": capture move should flag capture",Move.isCapture(filteredMoves.get(0)));
				assertEquals("Tile: "+piecePos+": capture move should have this Bishop's code set in its pieceCode field",
						piece.pieceCode,Move.getPieceCode(filteredMoves.get(0)));

				board.setSquare(friendly.pieceCode,enemyPos.getIndex());//Don't capture friendlies
				gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
				filteredMoves=findCaptures(gotMoves,board);
				assertEquals("Tile: "+piecePos+": Should not capture friendlies at "+enemyPos+" Size of list should be 0"
						,0,filteredMoves.size());
				board.setSquare(PieceCode.Blank,enemyPos.getIndex());//Clean up for next run
			}
			board.setSquare(PieceCode.Blank,piecePos.getIndex());//remove the piece I placed for next square
		}
	}
}
