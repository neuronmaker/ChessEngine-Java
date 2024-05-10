package com.dalton.ChessEngine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;

import static com.dalton.ChessEngine.Types.*;
import static com.dalton.ChessEngine.UtilsForTests.*;

/**
 * Tests for the King piece class
 * @author Dalton Herrewynen
 * @version 2
 */
public class KingTest{
	/** The offsets of the moves the King could make if there is room */
	public static final int[][] legalOffset=new int[][]{
			{1,-1},{1,0},{1,1},//top row
			{0,-1},{0,1},//skip the same square we are on
			{-1,-1},{-1,0},{-1,1}};//bottom row
	/** Offsets that a King cannot move to, use for testing range of motion */
	public static final int[][] illegalOffset=new int[][]{
			{2,-2},{2,-1},{2,0},{2,1},{2,2},//top row
			{1,-2},{1,2},//skip legal squares
			{0,-2},{0,2},//skip legal squares
			{-1,-2},{-1,2},//skip legal squares
			{-2,-2},{-2,-1},{-2,0},{-2,1},{-2,2},//bottom row
	};
	/** Constants for testing moves on and off the board */
	private static final int minOnBoard=1, maxOnBoard=6;
	/** Some arbitrary move that cannot contain a castle move */
	private static final int bogusNonCastleMove=Move.blank();
	Engine engine;
	Board board, blankState;
	ArrayList<Integer> gotMoves, filteredMoves;
	ArrayList<Coord> expectedCoords, gotCoords;//these are the destination coord

	King king;
	Rook qSideCastle, kSideCastle;
	Coord kingPos, kCastlePos, qCastlePos;

	@Before
	public void setup(){
		//set positions for white pieces, black ones are one translation move away
		kingPos=new Coord(4,0);
		kCastlePos=new Coord(7,0);
		qCastlePos=new Coord(0,0);
		board=new Board(Board.CLEAR);
		blankState=board.saveState();
		gotMoves=new ArrayList<>();
		filteredMoves=new ArrayList<>();
		gotCoords=new ArrayList<>();
		expectedCoords=new ArrayList<>();
		engine=new Engine(1,2);
	}

	@After
	public void tearDown(){
		kingPos=null;
		kCastlePos=null;
		qCastlePos=null;
		board=null;
		blankState=null;
		expectedCoords=null;
		gotCoords=null;
		gotMoves=null;
		filteredMoves=null;
		engine=null;
	}

	/** Test the WHITE King where it can make all its possible moves */
	@Test
	public void testBasicMoveWHITE(){
		for(int y=minOnBoard; y<=maxOnBoard; ++y){
			for(int x=minOnBoard; x<=maxOnBoard; ++x){//iterate all X and Y combinations that will only generate moves on the board
				Coord piecePos=new Coord(x,y);
				testBasicMoveTemplate(piecePos,WHITE);
			}
		}
	}

	/** Test the WHITE King where some possible moves would be off the board */
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

	/** Test the BLACK King where it can make all its possible moves */
	@Test
	public void testBasicMoveBLACK(){
		for(int y=minOnBoard; y<=maxOnBoard; ++y){
			for(int x=minOnBoard; x<=maxOnBoard; ++x){//iterate all X and Y combinations that will only generate moves on the board
				Coord piecePos=new Coord(x,y);
				testBasicMoveTemplate(piecePos,BLACK);
			}
		}
	}

	/** Test the BLACK King where some possible moves would be off the board */
	@Test
	public void testBasicMoveOffBoardBLACK(){
		for(int y=0; y<BOARD_SIZE; ++y){
			for(int x=0; x<BOARD_SIZE; ++x){//iterate over all squares, only use squares that can generate moves off the board
				if(x>=minOnBoard && x<maxOnBoard && y>=minOnBoard && y<maxOnBoard) continue;//don't recheck squares that only generate moves on the board, they're already checked in another test
				Coord piecePos=new Coord(x,y);//put it where it will potentially generate off-board moves
				testBasicMoveTemplate(piecePos,BLACK);
			}
		}
	}

	/**
	 * Used for testing the basic moves of the King Piece, code is repeated so moved to a method
	 * @param piecePos the coordinate of where to put the piece on the Board
	 * @param team     WHITE or BLACK
	 */
	private void testBasicMoveTemplate(Coord piecePos,boolean team){
		king=new King(team);
		board.setSquare(king.pieceCode,piecePos.getIndex());
		gotMoves=Engine.getLegalMoves(board,king.pieceCode);

		for(int gotMove: gotMoves){//check the piece code and special code
			assertEquals("Move should be of Normal type (0)",Move.normalMove,Move.getSpecialCode(gotMove));
			assertEquals("Piece code, should match King",king.pieceCode,Move.getPieceCode(gotMove));
		}

		for(int[] ints: legalOffset){
			Coord testDest=new Coord(piecePos);
			if(testDest.addVector(ints[0],ints[1])){//if piece would still be on the board
				expectedCoords.add(testDest);
			}
		}

		gotCoords=getDestCoords(gotMoves);//get destinations
		gotCoords.sort(Comparator.comparingInt(Coord::getIndex));//sort the destinations so they can be compared
		expectedCoords.sort(Comparator.comparingInt(Coord::getIndex));

		assertEquals("Number of moves generated and number expected were not the same",expectedCoords.size(),gotCoords.size());
		for(int i=0; i<gotCoords.size(); ++i){
			assertEquals("Failure at move "+i,expectedCoords.get(i).toString(),gotCoords.get(i).toString());
		}
		//clean up
		board.loadState(blankState);//make sure the board is blanked
		expectedCoords.clear();//clear out the expected destinations
	}

	/** Call the test for the Move structure for WHITE */
	@Test
	public void testMoveAnatomyWHITE(){
		testMoveAnatomyCommon(WHITE);
	}

	/** Call the test for the Move structure for BLACK */
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
		king=new King(team);
		board.setSquare(king.pieceCode,piecePos.getIndex());
		gotMoves=Engine.getLegalMoves(board,king.pieceCode);
		assertFalse("There should be encoded move integers here",gotMoves.isEmpty());
		for(int move: gotMoves){
			assertEquals("Moves should have starting position correct",piecePos.toString(),Coord.orderedPair(Move.getStartIndex(move)));
			assertEquals("Move should be of Normal type",Move.normalMove,Move.getSpecialCode(move));
			assertEquals("Piece code, converted to pretty printed name",PieceCode.decodePieceName(king.pieceCode),Move.getPieceName(move));
		}
	}

	/** Test for Queen Side Castle move for WHITE King */
	@Test
	public void testCastleQueenSideWHITE(){
		setUpCastles(WHITE);//Set up the board and pieces for castling
		int gotCastle=bogusNonCastleMove;//Store if we got a move here, set it to not a castle move for now
		gotMoves=Engine.getLegalMoves(board,king.pieceCode);
		gotMoves=findMovesByCode(gotMoves,Move.qSideCastle);
		for(int i=0; i<gotMoves.size(); ++i){//Search for a castling move
			if(Move.getSpecialCode(gotMoves.get(i))==Move.qSideCastle){//search by type
				if(gotCastle!=bogusNonCastleMove){//do not allow multiple of the same castling moves
					Assert.fail("There were multiple Queen Side Castle moves when there should be only one");
				}else{
					gotCastle=gotMoves.get(i);
				}
			}
		}
		assertNotEquals("This should have found one (and only one) Queen Side Castle move",bogusNonCastleMove,gotCastle);//should not still equal the arbitrary non-castle move
		assertEquals("Queen Side Castle Move should mark the King's position as the start position"
				,kingPos.toString(),Coord.orderedPair(Move.getStartIndex(gotCastle)));
		assertEquals("Move should have correct type",Move.qSideCastle,Move.getSpecialCode(gotCastle));
	}

	/** Test for King Side Castle move for WHITE King */
	@Test
	public void testCastleKingSideWHITE(){
		setUpCastles(WHITE);//Set up the board and pieces for castling
		int gotCastle=bogusNonCastleMove;//Store if we got a move here, set it to not a castle move for now
		gotMoves=Engine.getLegalMoves(board,king.pieceCode);
		gotMoves=findMovesByCode(gotMoves,Move.kSideCastle);
		for(int i=0; i<gotMoves.size(); ++i){//Search for a castling move
			if(Move.getSpecialCode(gotMoves.get(i))==Move.kSideCastle){//search by type
				if(gotCastle!=bogusNonCastleMove){//do not allow multiple of the same castling moves
					Assert.fail("There were multiple King Side Castle moves when there should be only one");
				}else{
					gotCastle=gotMoves.get(i);
				}
			}
		}
		assertNotEquals("This should have found one (and only one) King Side Castle move",bogusNonCastleMove,gotCastle);//should not still equal the arbitrary non-castle move
		assertEquals("Queen Side Castle Move should mark the King's position as the start position"
				,kingPos.toString(),Coord.orderedPair(Move.getStartIndex(gotCastle)));
		assertEquals("Move should have correct type",Move.kSideCastle,Move.getSpecialCode(gotCastle));
	}

	/** Test for Queen Side Castle move For BLACK King */
	@Test
	public void testCastleQueenSideBLACK(){
		setUpCastles(BLACK);//Set up the board and pieces for castling
		int gotCastle=bogusNonCastleMove;//Store if we got a move here, set it to not a castle move for now
		gotMoves=Engine.getLegalMoves(board,king.pieceCode);
		gotMoves=findMovesByCode(gotMoves,Move.qSideCastle);
		for(int gotMove: gotMoves){//Search for a castling move
			if(Move.getSpecialCode(gotMove)==Move.qSideCastle){//search by type
				if(gotCastle!=bogusNonCastleMove){//do not allow multiple of the same castling moves
					Assert.fail("There were multiple Queen Side Castle moves when there should be only one");
				}else{
					gotCastle=gotMove;
				}
			}
		}
		assertNotEquals("This should have found one (and only one) Queen Side Castle move",bogusNonCastleMove,gotCastle);//should not still equal the arbitrary non-castle move
		assertEquals("Queen Side Castle Move should mark the King's position as the start position"
				,kingPos.toString(),Coord.orderedPair(Move.getStartIndex(gotCastle)));
		assertEquals("Move should have correct type",Move.qSideCastle,Move.getSpecialCode(gotCastle));
	}

	/** Test for King Side Castle move For BLACK King */
	@Test
	public void testCastleKingSideBLACK(){
		setUpCastles(BLACK);//Set up the board and pieces for castling
		int gotCastle=bogusNonCastleMove;//Store if we got a move here, set it to not a castle move for now
		gotMoves=Engine.getLegalMoves(board,king.pieceCode);
		gotMoves=findMovesByCode(gotMoves,Move.kSideCastle);
		for(int gotMove: gotMoves){//Search for a castling move
			if(Move.getSpecialCode(gotMove)==Move.kSideCastle){//search by type
				if(gotCastle!=bogusNonCastleMove){//do not allow multiple of the same castling moves
					Assert.fail("There were multiple King Side Castle moves when there should be only one");
				}else{
					gotCastle=gotMove;
				}
			}
		}
		assertNotEquals("This should have found one (and only one) King Side Castle move",bogusNonCastleMove,gotCastle);//should not still equal the arbitrary non-castle move
		assertEquals("Queen Side Castle Move should mark the King's position as the start position"
				,kingPos.toString(),Coord.orderedPair(Move.getStartIndex(gotCastle)));
		assertEquals("Move should have correct type",Move.kSideCastle,Move.getSpecialCode(gotCastle));
	}

	/**
	 * Loads the board and pieces so that castling can be tested
	 * @param team Which side should be set up for tests
	 */
	public void setUpCastles(boolean team){
		if(team==WHITE){
			king=new King(WHITE);
			qSideCastle=new Rook(WHITE);
			kSideCastle=new Rook(WHITE);
		}else{
			king=new King(BLACK);
			qSideCastle=new Rook(BLACK);
			kSideCastle=new Rook(BLACK);
			kingPos=new Coord(4,7);
			kCastlePos=new Coord(7,7);
			qCastlePos=new Coord(0,7);
		}
		board.loadState(blankState);//make sure the board is blanked
		board.setSquare(king.pieceCode,kingPos.getIndex());
		board.setSquare(kSideCastle.pieceCode,kCastlePos.getIndex());
		board.setSquare(qSideCastle.pieceCode,qCastlePos.getIndex());
		board.setAllNotMoved();
	}

	/** Check captures for BLACK King */
	@Test
	public void captureTestBLACK(){
		captureTestCommon(BLACK);
	}

	/** Check captures for WHITE King */
	@Test
	public void captureTestWHITE(){
		captureTestCommon(WHITE);
	}

	/**
	 * Common code for capture tests
	 * @param team The team of this King
	 */
	public void captureTestCommon(boolean team){
		king=new King(team);
		Piece enemy=new Queen(!team), friendly=new Queen(team);
		Coord piecePos=new Coord(), enemyPos;
		for(int i=0; i<TOTAL_SQUARES; ++i){//check for all squares
			piecePos.setIndex(i);
			board.loadState(blankState);//force the board to be blank just in case
			board.setSquare(king.pieceCode,piecePos.getIndex());
			for(int[] offset: legalOffset){
				enemyPos=piecePos.getShiftedCoord(offset[0],offset[1]);//array of offsets with X at 0 and Y at 1
				if(enemyPos.isSet()==UNSET) continue;//only test if set so only valid coordinates are tested
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());//test captures
				gotMoves=Engine.getLegalMoves(board,king.pieceCode);//search all pseudo legal moves
				filteredMoves=findCaptures(gotMoves,board);//Filter out capture moves (should only be one of these because only one enemy)
				assertEquals("Tile: "+piecePos+" There should be exactly one capture at:"+enemyPos,1,filteredMoves.size());//should capture the enemy
				assertEquals("Tile: "+piecePos+" End position should match the enemy piece at:"+enemyPos,enemyPos.toString(),Coord.orderedPair(Move.getEndIndex(filteredMoves.get(0))));//match destinations
				assertTrue("Tile: "+piecePos+": capture move should flag capture",Move.isCapture(filteredMoves.get(0)));
				assertEquals("Tile: "+piecePos+" Piece code must match the King even on captures:"+enemyPos,king.pieceCode,Move.getPieceCode(filteredMoves.get(0)));

				board.setSquare(friendly.pieceCode,enemyPos.getIndex());//set to friendly unit
				gotMoves=Engine.getLegalMoves(board,king.pieceCode);//search all pseudo legal moves
				filteredMoves=findCaptures(gotMoves,board);//Filter out capture moves (Should not find any because we should not capture allied pieces)
				assertEquals("Tile: "+piecePos+" Should not have any capture moves against friendlies at:"+enemyPos,0,filteredMoves.size());//DO NOT CAPTURE YOUR OWN TEAMMATES

				board.setSquare(PieceCode.Blank,enemyPos.getIndex());//unset the square
			}
			for(int[] offset: illegalOffset){
				enemyPos=piecePos.getShiftedCoord(offset[0],offset[1]);
				if(enemyPos.isSet()==UNSET) continue;//only test if set
				board.setSquare(enemy.pieceCode,enemyPos.getIndex());
				gotMoves=Engine.getLegalMoves(board,king.pieceCode);
				filteredMoves=findCaptures(gotMoves,board);//Filter out capture moves (enemy is outside of King's attack range so should not filter out any capture moves)
				assertEquals("Tile: "+piecePos+" There should be no captures when the enemy is outside the range of the King at:"+enemyPos,0,filteredMoves.size());//Enemy is out of range, cannot capture

				board.setSquare(PieceCode.Blank,enemyPos.getIndex());
			}
			board.setSquare(PieceCode.Blank,piecePos.getIndex());//unset the piece square too
		}
	}
}
