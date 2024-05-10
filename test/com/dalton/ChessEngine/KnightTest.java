package com.dalton.ChessEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;

import static com.dalton.ChessEngine.Types.*;
import static com.dalton.ChessEngine.UtilsForTests.*;

/**
 * Tests for the Knight piece class
 * @author Dalton Herrewynen
 * @version 2
 */
public class KnightTest{
	/** The offsets of the moves the Knight could make if there is room */
	public static final int[][] legalOffset={//0 is x, 1 is y
			{-2,1},{-1,2},{-2,-1},{-1,-2},//left half
			{2,1},{1,2},{2,-1},{1,-2}};   //right half
	/** Constants for testing moves on and off the board */
	public static final int minOnBoard=2, maxOnBoard=5;
	Engine engine;
	Board board, blankState;
	ArrayList<Integer> gotMoves, filteredMoves;
	ArrayList<Coord> expectedCoords, gotCoords;//these are the destination coord

	@Before
	public void setup(){
		board=new Board(Board.CLEAR);
		expectedCoords=new ArrayList<>();
		gotCoords=new ArrayList<>();
		gotMoves=new ArrayList<>();
		filteredMoves=new ArrayList<>();
		blankState=board.saveState();
		engine=new Engine(1,2);
	}

	@After
	public void tearDown(){
		board=null;
		expectedCoords=null;
		gotCoords=null;
		gotMoves=null;
		filteredMoves=null;
		blankState=null;
		engine=null;
	}

	/** Test the Move structure for WHITE */
	@Test
	public void testMoveAnatomyWHITE(){
		testMoveAnatomyCommon(WHITE);
	}

	/** Test the Move structure for BLACK */
	@Test
	public void testMoveAnatomyBLACK(){
		testMoveAnatomyCommon(BLACK);
	}

	/**
	 * Test the Move structure, the common code for both teams
	 * @param team Which team to test for
	 */
	public void testMoveAnatomyCommon(boolean team){
		Coord piecePos=new Coord(4,4);
		Knight piece=new Knight(team);
		board.setSquare(piece.pieceCode,piecePos.getIndex());
		gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
		assertFalse("There should be encoded move integers here",gotMoves.isEmpty());
		for(int move: gotMoves){
			assertEquals("Moves should have starting position correct",piecePos.toString(),Coord.orderedPair(Move.getStartIndex(move)));
			assertEquals("Move should be of Normal type (1)",Move.normalMove,Move.getSpecialCode(move));
			assertEquals("Piece code, converted to pretty printed name",PieceCode.decodePieceName(piece.pieceCode),Move.getPieceName(move));
		}
	}

	/** Test the WHITE Knight where it can make all its possible moves */
	@Test
	public void testBasicMoveWHITE(){
		for(int y=minOnBoard; y<=maxOnBoard; ++y){
			for(int x=minOnBoard; x<=maxOnBoard; ++x){//iterate all X and Y combinations that will only generate moves on the board
				Coord piecePos=new Coord(x,y);
				testBasicMoveTemplate(piecePos,WHITE);
			}
		}
	}

	/** Test the BLACK Knight where it can make all its possible moves */
	@Test
	public void testBasicMoveBLACK(){
		for(int y=minOnBoard; y<=maxOnBoard; ++y){
			for(int x=minOnBoard; x<=maxOnBoard; ++x){//iterate all X and Y combinations that will only generate moves on the board
				Coord piecePos=new Coord(x,y);
				testBasicMoveTemplate(piecePos,BLACK);
			}
		}
	}

	/** Test the WHITE Knight where some possible moves would be off the board */
	@Test
	public void testBasicMoveOffBoardWHITE(){
		for(int y=0; y<BOARD_SIZE; ++y){
			for(int x=0; x<BOARD_SIZE; ++x){//iterate over all squares, only use squares that can generate moves off the board
				if(x>=minOnBoard && x<maxOnBoard && y>=minOnBoard && y<maxOnBoard) continue;//don't recheck squares that only generate moves on the board, they're already checked in another test
				Coord piecePos=new Coord(x,y);//put it where it will potentially generate off-board moves
				testBasicMoveTemplate(piecePos,WHITE);
			}
		}
	}

	/** Test the BLACK Knight where some possible moves would be off the board */
	@Test
	public void testBasicMoveOffBoardBLACK(){
		for(int y=0; y<BOARD_SIZE; ++y){
			for(int x=0; x<BOARD_SIZE; ++x){//iterate over all squares
				if(x>=minOnBoard && x<maxOnBoard && y>=minOnBoard && y<maxOnBoard) continue;//don't recheck squares that only generate moves on the board, they're already checked in another test
				Coord piecePos=new Coord(x,y);//put it where it will potentially generate off-board moves
				testBasicMoveTemplate(piecePos,BLACK);
			}
		}
	}

	/**
	 * Used for testing the basic moves of the Knight Piece for either team
	 * @param piecePos the coordinate of where to put the piece on the Board
	 * @param team     WHITE or BLACK
	 */
	private void testBasicMoveTemplate(Coord piecePos,boolean team){
		Knight piece=new Knight(team);
		board.setSquare(piece.pieceCode,piecePos.getIndex());
		gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
		gotCoords=getDestCoords(gotMoves);
		expectedCoords=new ArrayList<>();

		for(int i=0; i<gotMoves.size(); ++i){//make sure Piece Codes match
			assertEquals("Move "+i+" "+Move.describe(gotMoves.get(i))+"  should have PieceCode match the Knight"
					,piece.pieceCode,Move.getPieceCode(gotMoves.get(i)));
		}

		for(int[] ints: legalOffset){
			Coord testDest=new Coord(piecePos);
			if(testDest.addVector(ints[0],ints[1])){//if piece would still be on the board
				expectedCoords.add(testDest);//add it to the list of coord that can be reached
			}
		}
		gotCoords.sort(Comparator.comparingInt(Coord::getIndex));//sort the destinations
		expectedCoords.sort(Comparator.comparingInt(Coord::getIndex));
		assertEquals("Tile "+piecePos+" Got and Expected coord list should be same length",expectedCoords.size(),gotCoords.size());
		for(int i=0; i<gotCoords.size(); ++i){
			assertEquals("Tile "+piecePos+" Failure at move "+i+" destinations do not match",expectedCoords.get(i).toString(),gotCoords.get(i).toString());
		}
		board.loadState(blankState);//clean up after myself
	}

	/** Call the capture moves test for BLACK Knights */
	@Test
	public void testCaptureBLACK(){
		testCaptureCommon(BLACK);
	}

	/** Call the capture moves test for WHITE Knights */
	@Test
	public void testCaptureWHITE(){
		testCaptureCommon(WHITE);
	}

	/**
	 * Common code for capture tests
	 * @param team The team of this Knight
	 */
	private void testCaptureCommon(boolean team){
		Piece piece=new Knight(team), enemy=new Pawn(!team), friendly=new Pawn(team);
		Coord piecePos=new Coord(), coords;
		for(int i=0; i<TOTAL_SQUARES; ++i){
			piecePos.setIndex(i);
			board.setSquare(piece.pieceCode,piecePos.getIndex());
			expectedCoords.clear();
			for(int[] offset: legalOffset){//figure out how many offsets are on the board
				coords=piecePos.getShiftedCoord(offset[0],offset[1]);
				if(coords.isSet()==SET) expectedCoords.add(coords.copy());//add only if set
			}
			for(Coord coord: expectedCoords){//fill reachable squares with enemies
				board.setSquare(enemy.pieceCode,coord.getIndex());//these should each generate a capture
			}

			gotMoves=Engine.getLegalMoves(board,piece.pieceCode);//get all moves
			for(int j=0; j<gotMoves.size(); ++j){//make sure Piece Codes match
				assertEquals("Move "+j+" "+Move.describe(gotMoves.get(j))+" should have PieceCode match the Knight"
						,piece.pieceCode,Move.getPieceCode(gotMoves.get(j)));
			}
			filteredMoves=findCaptures(gotMoves,board);//just check for captures

			assertEquals("Tile:"+piecePos+" Should have found same number of captures as enemies",expectedCoords.size(),filteredMoves.size());
			for(int move: gotMoves){
				assertEquals("Tile:"+piecePos+" capture move at "+Coord.orderedPair(Move.getEndIndex(move))
						+" should point to an enemy position",PieceCode.decodeChar(enemy.pieceCode),PieceCode.decodeChar(board.getSquare(Move.getEndIndex(move))));
				assertTrue("Tile: "+piecePos+": capture move at"+Coord.orderedPair(Move.getEndIndex(move))+
						" should flag capture",Move.isCapture(move));
			}

			for(Coord coord: expectedCoords){//fill reachable squares with friendlies
				board.setSquare(friendly.pieceCode,coord.getIndex());//these should not generate captures
			}

			gotMoves=Engine.getLegalMoves(board,piece.pieceCode);
			for(int j=0; j<gotMoves.size(); ++j){//make sure Piece Codes match
				assertEquals("Move "+j+" "+Move.describe(gotMoves.get(j))+" should have PieceCode match the Knight",
						piece.pieceCode,Move.getPieceCode(gotMoves.get(j)));
			}
			filteredMoves=findCaptures(gotMoves,board);//just check for captures

			assertEquals("Tile:"+piecePos+" Should have found 0 capture moves, should not capture friendlies",0,filteredMoves.size());

			for(Coord coord: expectedCoords){//clean up for next square
				board.setSquare(PieceCode.Blank,coord.getIndex());//blank out squares
			}
			board.setSquare(PieceCode.Blank,piecePos.getIndex());
		}
	}
}
