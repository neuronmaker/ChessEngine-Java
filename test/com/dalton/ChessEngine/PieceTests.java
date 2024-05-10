package com.dalton.ChessEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.dalton.ChessEngine.Types.*;

/**
 * Tests for the Piece Super Class
 * @author Dalton Herrewynen
 * @version 1
 */
public class PieceTests{
	final char[] pieceCode={'k','n','b','r','q','p'};
	Board board;

	@Before
	public void setup(){
		board=new Board(Board.CLEAR);
	}

	@After
	public void tearDown(){
		board=null;
	}
}
