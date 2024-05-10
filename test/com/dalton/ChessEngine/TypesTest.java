package com.dalton.ChessEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.dalton.ChessEngine.Types.*;
import static com.dalton.ChessEngine.UtilsForTests.*;
import static org.junit.Assert.*;

/**
 * Tests for the Types utility class
 * @author Dalton Herrewynen
 * @version 1
 */
public class TypesTest{
	Board blankBoard, gameBoard;

	@Before
	public void setUp(){
		blankBoard=new Board(Board.CLEAR);
		gameBoard=new Board(Board.DEFAULT);
	}

	@After
	public void tearDown(){
		blankBoard=null;
		gameBoard=null;
	}

	/** Test for the team setting case from team */
	@Test
	public void testTeamCases(){
		//white is lowercase
		assertEquals("WHITE team is lowercase",'a',setCaseFromTeam('a',WHITE));
		assertEquals("WHITE team is lowercase",'a',setCaseFromTeam('A',WHITE));
		assertEquals("BLACK team is UPPERCASE",'A',setCaseFromTeam('a',BLACK));
		assertEquals("BLACK team is UPPERCASE",'A',setCaseFromTeam('A',BLACK));
	}

	/** Test for valid team value */
	@Test
	public void testConstantsValues(){
		assertNotEquals("Teams should not be the same value",BLACK,WHITE);
		assertNotEquals("SET and UNSET should be different",SET,UNSET);
		assertNotEquals("Success and failure should be different",SUCCESS,FAIL);
		assertTrue("Success should be true because that makes sense",SUCCESS);
	}

	/** Test the ability to reverse masks */
	@Test
	public void testReverseMask(){
		long mask=0b0101000000000000000000000000000000000000000000000000000000001101L;//pattern covers most possibilities
		long expected=0b1011000000000000000000000000000000000000000000000000000000001010L;//most fails will manifest in these end patterns
		long gotLong=reverseMask(mask);
		testLongStringByChar(maskString(expected),maskString(gotLong),8);
	}

	/** Test the correctness of mask printing */
	@Test
	public void testMaskString(){
		String got="", expected="1010000000001000000000000000000000000000100000000000000000001111";//arbitrary but should make it should cover reversed and other failure modes
		long mask=0b1010000000001000000000000000000000000000100000000000000000001111L;//the long
		got=maskString(mask);
		testLongStringByChar(expected,got,8);
	}
}