package com.dalton.ChessEngine;

import junit.framework.TestCase;

import static com.dalton.ChessEngine.Types.*;

/**
 * Tests the PGN converter class
 * @author Dalton Herrewynen
 * @version 0.1
 */
public class PGNConverterTest extends TestCase{
	Board board;

	public void setUp() throws Exception{
		board=new Board(Board.CLEAR);
	}

	public void tearDown() throws Exception{
		board=null;//Java doesn't have a delete, so give it to garbage collector
	}

	/** Test moves with only the destination square */
	public void testSearchMovesNoDiff(){
		for(int i=0; i<TOTAL_SQUARES; ++i){
			for(int j=0; j<TOTAL_SQUARES; ++j){
				if(j==i) continue;//skip squares that match, must have different starting and ending positions
			}
		}
	}

	public void testSearchMovesDiffX(){
	}

	public void testSearchMovesDiffY(){
	}
}