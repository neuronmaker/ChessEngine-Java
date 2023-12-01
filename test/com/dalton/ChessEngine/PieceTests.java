/*
File: PieceTest.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved.
License is hereby granted to The Kings University to store, compile, run, and display this file for grading and educational purposes.
Ownership is to be held by the primary author.
Licence is granted to the secondary members as noted in the Authors.md file for display, running, compiling, and modification for use in their future projects. Just keep my name on functions I wrote.
 */
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
