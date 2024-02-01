/*
File: QueenTest.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved.
License is hereby granted to The Kings University to store, compile, run, and display this file for grading and educational purposes.
Ownership is to be held by the primary author.
Licence is granted to the secondary members as noted in the Authors.md file for display, running, compiling, and modification for use in their future projects. Just keep my name on functions I wrote.
 */
package com.dalton.ChessEngine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.Assert.*;
import static com.dalton.ChessEngine.Types.*;
import static com.dalton.ChessEngine.UtilsForTests.*;

/**
 * Tests for the Queen piece class
 * @author Dalton Herrewynen
 * @version 2
 */
public class QueenTest{
	Board board;
	ArrayList<Integer> gotMoves,filteredMoves;
	ArrayList<Coord> gotCoords, expectedCoords;

	@Before
	public void setup(){
		board=new Board(Board.CLEAR);
		gotCoords=new ArrayList<>();
		expectedCoords=new ArrayList<>();
		filteredMoves=new ArrayList<>();
	}

	@After
	public void tearDown(){
		board=null;
		gotCoords=null;
		expectedCoords=null;
		gotMoves=null;
		filteredMoves=null;
	}

	/** Test basic movement for WHITE Queen, do not test capture */
	@Test
	public void testBasicMoveWHITE(){
		testBasicMoveCommon(WHITE);
	}

	/** Test basic movement for BLACK Queen, do not test capture */
	@Test
	public void testBasicMoveBLACK(){
		testBasicMoveCommon(BLACK);
	}

	/**
	 * Tests each color's basic moves to avoid code re-use
	 * @param team Which team to check? WHITE or BLACK
	 */
	public void testBasicMoveCommon(boolean team){
		Coord piecePos;
		Queen queen=new Queen(team);
		for(int i=0; i<TOTAL_SQUARES; ++i){//test on all tiles
			piecePos=new Coord(i);
			board.setSquare(queen.pieceCode,piecePos.getIndex());
			long enemies=board.alliedPieceMask(!team);
			long blanks=~(enemies | board.alliedPieceMask(team));
			gotMoves=new ArrayList<>();
			queen.getMoves(gotMoves,enemies,blanks,piecePos.getIndex());
			gotCoords=getDestCoords(gotMoves);

			expectedCoords=new ArrayList<>();
			expectedCoords.addAll(getLineOfCoords(piecePos,1,1));
			expectedCoords.addAll(getLineOfCoords(piecePos,1,-1));
			expectedCoords.addAll(getLineOfCoords(piecePos,-1,1));
			expectedCoords.addAll(getLineOfCoords(piecePos,-1,-1));

			expectedCoords.addAll(getLineOfCoords(piecePos,0,1));
			expectedCoords.addAll(getLineOfCoords(piecePos,0,-1));
			expectedCoords.addAll(getLineOfCoords(piecePos,1,0));
			expectedCoords.addAll(getLineOfCoords(piecePos,-1,0));

			gotCoords.sort(Comparator.comparingInt(Coord::getIndex));//sort the coord, so they are in same order and comparisons will work
			expectedCoords.sort(Comparator.comparingInt(Coord::getIndex));

			for(int move: gotMoves){//test correct piece code
				assertEquals("Piece code should match the Queen",queen.pieceCode,Move.getPieceCode(move));
			}

			assertEquals("Square "+piecePos+": Must have same number of Coord in the got and expected list",
					expectedCoords.size(),gotCoords.size());
			for(int j=0; j<expectedCoords.size(); ++j){
				assertEquals("Square "+piecePos+": Move "+j+" destinations don't match",
						expectedCoords.get(j).toString(),gotCoords.get(j).toString());
			}
			board.setSquare(PieceCode.Blank,piecePos.getIndex());//remove this piece and start again
		}
	}

	/** Call the test the Move structure for WHITE */
	@Test
	public void testMoveAnatomyWHITE(){
		testMoveAnatomyCommon(WHITE);
	}

	/** Call the test the Move structure for BLACK */
	@Test
	public void testMoveAnatomyBLACK(){
		testMoveAnatomyCommon(BLACK);
	}

	/**
	 * Test the Move structure for both teams
	 * @param team Which team? WHITE or BLACK
	 */
	public void testMoveAnatomyCommon(boolean team){
		Coord piecePos=new Coord(4,4);
		Queen queen=new Queen(team);
		board.setSquare(queen.pieceCode,piecePos.getIndex());
		long enemies=board.alliedPieceMask(!team);
		long blanks=~(enemies | board.alliedPieceMask(team));
		gotMoves=new ArrayList<>();
		queen.getMoves(gotMoves,enemies,blanks,piecePos.getIndex());
		assertFalse("There should be encoded move integers here",gotMoves.isEmpty());
		for(int move: gotMoves){
			assertEquals("Moves should have starting position correct",piecePos.toString(),Coord.orderedPair(Move.getStartIndex(move)));
			assertEquals("Move should be of Normal type (0)",Move.normalMove,Move.getSpecialCode(move));
			assertEquals("Piece code, converted to pretty printed name",PieceCode.decodePieceName(queen.pieceCode),Move.getPieceName(move));
		}
	}

	/** Call the test method for Queen Captures for BLACK */
	@Test
	public void testBLACKCapture(){
		testCaptureCommon(BLACK);
	}

	/** Call the test method for Queen Captures for WHITE */
	@Test
	public void testWHITECapture(){
		testCaptureCommon(WHITE);
	}

	/**
	 * Test both team's captures
	 * @param team Which team? WHITE or BLACK
	 */
	public void testCaptureCommon(boolean team){
		Queen piece=new Queen(team), friendly=new Queen(team), enemy=new Queen(!team);
		Coord piecePos=new Coord();
		ArrayList<Coord> reachableSquares=new ArrayList<>();

		for(int i=0; i<TOTAL_SQUARES; ++i){//testing all squares
			piecePos.setIndex(i);
			board.setSquare(piece.pieceCode,piecePos.getIndex());
			reachableSquares.clear();//reset
			reachableSquares.addAll(getLineOfCoords(piecePos,0,1));//4 cardinal directions
			reachableSquares.addAll(getLineOfCoords(piecePos,0,-1));
			reachableSquares.addAll(getLineOfCoords(piecePos,1,0));
			reachableSquares.addAll(getLineOfCoords(piecePos,-1,0));

			reachableSquares.addAll(getLineOfCoords(piecePos,1,1));//4 diagonal directions
			reachableSquares.addAll(getLineOfCoords(piecePos,1,-1));
			reachableSquares.addAll(getLineOfCoords(piecePos,-1,1));
			reachableSquares.addAll(getLineOfCoords(piecePos,-1,-1));

			for(Coord enemyPos: reachableSquares){//Check ALL possible squares for captures
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());//Test capture

				long enemies=board.alliedPieceMask(!team);
				long blanks=~(enemies | board.alliedPieceMask(team));
				gotMoves=new ArrayList<>();
				piece.getMoves(gotMoves,enemies,blanks,piecePos.getIndex());
				filteredMoves=findCaptures(gotMoves,board);
				assertEquals("Tile: "+piecePos+": Should not have exactly one capture",1,filteredMoves.size());
				assertEquals("Tile: "+piecePos+": capture move should end on the enemy piece",
						enemyPos.toString(),Coord.orderedPair(Move.getEndIndex(filteredMoves.get(0))));
				assertTrue("Tile: "+piecePos+": capture move should flag capture",Move.isCapture(filteredMoves.get(0)));
				assertEquals("Tile: "+piecePos+": capture move should have this Queen's code set in its pieceCode field",
						piece.pieceCode,Move.getPieceCode(filteredMoves.get(0)));

				board.setSquare(friendly.pieceCode,enemyPos.getIndex());//Don't capture friendlies

				enemies=board.alliedPieceMask(!team);
				blanks=~(enemies | board.alliedPieceMask(team));
				gotMoves=new ArrayList<>();
				piece.getMoves(gotMoves,enemies,blanks,piecePos.getIndex());
				filteredMoves=findCaptures(gotMoves,board);
				assertEquals("Tile: "+piecePos+": Should not capture friendlies at "+enemyPos,0,filteredMoves.size());

				board.setSquare(PieceCode.Blank,enemyPos.getIndex());//Clean up for next run
			}
			board.setSquare(PieceCode.Blank,piecePos.getIndex());//remove the piece I placed for next square
		}
	}
}
