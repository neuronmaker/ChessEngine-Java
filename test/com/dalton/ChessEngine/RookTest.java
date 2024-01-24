/*
File: RookTest.java
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
 * Tests for the Rook piece
 * @author Dalton Herrewynen
 * @version 2
 */
public class RookTest{
	Board board;
	ArrayList<Integer> gotMoves, filteredMoves;
	ArrayList<Coord> gotCoords, expectedCoords;

	@Before
	public void setup(){
		board=new Board(Board.CLEAR);
		gotMoves=new ArrayList<>();
		filteredMoves=new ArrayList<>();
		gotCoords=new ArrayList<>();
		expectedCoords=new ArrayList<>();
	}

	@After
	public void tearDown(){
		board=null;
		gotCoords=null;
		expectedCoords=null;
		gotMoves=null;
		filteredMoves=null;
	}

	/** Test basic movement for WHITE Rook, do not test capture */
	@Test
	public void testBasicMoveWHITE(){
		testBasicMoveCommon(WHITE);
	}

	/** Test basic movement for BLACK Rook, do not test capture */
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
		Rook rook=new Rook(team);
		for(int i=0; i<TOTAL_SQUARES; ++i){//test on all tiles
			piecePos=new Coord(i);
			board.setSquare(rook.pieceCode,piecePos.getIndex());
			gotMoves=rook.getMoves(board,piecePos.getIndex());
			gotCoords=getDestCoords(gotMoves);

			expectedCoords=new ArrayList<>();
			expectedCoords.addAll(getLineOfCoords(piecePos,0,1));//4 cardinal directions
			expectedCoords.addAll(getLineOfCoords(piecePos,0,-1));
			expectedCoords.addAll(getLineOfCoords(piecePos,1,0));
			expectedCoords.addAll(getLineOfCoords(piecePos,-1,0));

			gotCoords.sort(Comparator.comparingInt(Coord::getIndex));//sort the coord, so they are in same order and comparisons will work
			expectedCoords.sort(Comparator.comparingInt(Coord::getIndex));

			for(int move: gotMoves){//test correct piece code
				assertEquals("Piece code should match the Rook",rook.pieceCode,Move.getPieceCode(move));
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
	 * Test move anatomy for either team
	 * @param team WHITE or BLACK
	 */
	public void testMoveAnatomyCommon(boolean team){
		Coord piecePos=new Coord(4,4);
		Rook piece=new Rook(team);
		board.setSquare(piece.pieceCode,piecePos.getIndex());
		gotMoves=piece.getMoves(board,piecePos.getIndex());
		assertFalse("There should be encoded move integers here",gotMoves.isEmpty());
		for(int move: gotMoves){
			assertEquals("Moves should have starting position correct",piecePos.toString(),Coord.orderedPair(Move.getStartIndex(move)));
			assertEquals("Move should be of Normal type (0)",Move.normalMove,Move.getSpecialCode(move));
			assertEquals("Piece code, converted to pretty printed name",PieceCode.decodePieceName(piece.pieceCode),Move.getPieceName(move));
		}
	}

	/** Call the capture move test for BLACK Rook */
	@Test
	public void testBLACKCapture(){
		testCaptureCommon(BLACK);
	}

	/** Call the capture move test for WHITE Rook */
	@Test
	public void testWHITECapture(){
		testCaptureCommon(WHITE);
	}

	/**
	 * Tests capture moves for either team
	 * @param team WHITE or BLACK
	 */
	public void testCaptureCommon(boolean team){
		Rook piece=new Rook(team), friendly=new Rook(team), enemy=new Rook(!team);
		Coord piecePos=new Coord();
		ArrayList<Coord> reachableSquares=new ArrayList<>();
		for(int i=0; i<TOTAL_SQUARES; ++i){//testing all squares
			piecePos.setIndex(i);
			board.setSquare(piece.pieceCode,piecePos.getIndex());

			reachableSquares.clear();//clear and find all squares in range
			reachableSquares.addAll(getLineOfCoords(piecePos,0,1));//4 cardinal directions
			reachableSquares.addAll(getLineOfCoords(piecePos,0,-1));
			reachableSquares.addAll(getLineOfCoords(piecePos,1,0));
			reachableSquares.addAll(getLineOfCoords(piecePos,-1,0));

			for(Coord enemyPos: reachableSquares){//Check ALL squares reachable by the Rook, one by one
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());//Test capture
				gotMoves=piece.getMoves(board,piecePos.getIndex());
				filteredMoves=findCaptures(gotMoves,board);
				assertEquals("Tile: "+piecePos+": Should not have exactly one capture",1,filteredMoves.size());
				assertEquals("Tile: "+piecePos+": capture move should end on the enemy piece",
						enemyPos.toString(),Coord.orderedPair(Move.getEndIndex(filteredMoves.get(0))));
				assertTrue("Tile: "+piecePos+": capture move should flag capture",Move.isCapture(filteredMoves.get(0)));
				assertEquals("Tile: "+piecePos+": capture move should have this Rook's code set in its pieceCode field",
						piece.pieceCode,Move.getPieceCode(filteredMoves.get(0)));

				board.setSquare(friendly.pieceCode,enemyPos.getIndex());//Don't capture friendlies
				gotMoves=piece.getMoves(board,piecePos.getIndex());
				filteredMoves=findCaptures(gotMoves,board);
				assertEquals("Tile: "+piecePos+": Should not capture friendlies at "+enemyPos+" Size of list should be 0"
						,0,filteredMoves.size());
				board.setSquare(PieceCode.Blank,enemyPos.getIndex());//Clean up for next run
			}
			board.setSquare(PieceCode.Blank,piecePos.getIndex());//remove the piece I placed for next square
		}
	}
}
