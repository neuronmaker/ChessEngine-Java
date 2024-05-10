package com.dalton.ChessEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.dalton.ChessEngine.Types.*;
import static com.dalton.ChessEngine.Move.*;

/**
 * Tests for the new and faster Move system, essential since I'm using a bunch of bit masks.
 * @author Dalton Herrewynen
 * @version 2
 */
public class MoveTest{
	@Before
	public void setUp(){
	}

	@After
	public void tearDown(){
	}

	/** Encode and decode a series of starting positions */
	@Test
	public void testDecodeStartPosIndex(){
		int move;
		for(int start=0; start<TOTAL_SQUARES; ++start){//for each starting coordinate
			for(int end=0; end<TOTAL_SQUARES; ++end){//try encoding with all the ending squares
				for(int code=0; code<PieceCode.PIECE_TYPES; ++code){
					move=Move.encodeNormal(code,start,end);
					assertEquals("At square "+start+" -> "+end+" Code:"+code,start,Move.getStartIndex(move));
				}
			}
		}
	}

	/** Encode and decode a series of ending positions */
	@Test
	public void testDecodeEndPosIndex(){
		int move;
		for(int start=0; start<TOTAL_SQUARES; ++start){//for each ending coordinate
			for(int end=0; end<TOTAL_SQUARES; ++end){//try encoding with all the starting squares
				for(int code=0; code<PieceCode.PIECE_TYPES; ++code){
					move=Move.encodeNormal(code,start,end);
					assertEquals("At square "+start+" -> "+end+" Code:"+code,end,Move.getEndIndex(move));
				}
			}
		}
	}

	/** Encode and decode every possible piece code */
	@Test
	public void testDecodePieceCode(){
		int move;
		for(int start=0; start<TOTAL_SQUARES; ++start){//for each ending coordinate
			for(int end=0; end<TOTAL_SQUARES; ++end){//try encoding with all the starting squares
				for(int code=0; code<PieceCode.PIECE_TYPES; ++code){
					move=Move.encodeNormal(code,start,end);
					assertEquals("At square "+start+" -> "+end+" Code:"+code,code,Move.getPieceCode(move));
				}
			}
		}
	}

	/** Test the encoding and structure of castling moves */
	@Test
	public void testDecodeCastling(){
		int WhiteKingPos=Coord.XYToIndex(0,4);
		int BlackKingPos=Coord.XYToIndex(7,4);
		int move;
		//test Queen Side castling
		move=Move.encode(Move.qSideCastle,PieceCode.KingW,WhiteKingPos,0);//the ending position is ignored for castling
		assertEquals("Queen Side castle WHITE team, wrong special code",Move.qSideCastle,Move.getSpecialCode(move));
		move=Move.encode(Move.qSideCastle,PieceCode.KingB,BlackKingPos,0);
		assertEquals("Queen Side castle BLACK team, wrong special code",Move.qSideCastle,Move.getSpecialCode(move));
		//test King Side castling
		move=Move.encode(Move.kSideCastle,PieceCode.KingW,WhiteKingPos,0);//the ending position is ignored for castling
		assertEquals("King Side castle WHITE team, wrong special code",Move.kSideCastle,Move.getSpecialCode(move));
		move=Move.encode(Move.kSideCastle,PieceCode.KingB,BlackKingPos,0);
		assertEquals("King Side castle BLACK team, wrong special code",Move.kSideCastle,Move.getSpecialCode(move));
	}

	/** Test the structure of the special codes, make sure no collisions */
	@Test
	public void testSpecialCodeStructure(){
		int move;
		//basic moves
		move=encode(blankMove,0,0,0);//blank
		assertEquals("Problem with blank move code",blankMove,getSpecialCode(move));
		move=encode(normalMove,0,0,0);//just move
		assertEquals("Problem with normal move code",normalMove,getSpecialCode(move));
		move=encode(capture,0,0,0);//capture
		assertEquals("Problem with capture move code",capture,getSpecialCode(move));

		//castling
		assertEquals("Problem with king and queen side castle codes not being 1 bit apart",1,Math.abs(kSideCastle-qSideCastle));
		move=encode(kSideCastle,0,0,0);//castling (King side)
		assertEquals("Problem with King side castling code",kSideCastle,getSpecialCode(move));
		move=encode(qSideCastle,0,0,0);//castling (Queen Side)
		assertEquals("Problem with Queen side castling code",qSideCastle,getSpecialCode(move));

		//pawns todo: generate tests for Pawn codes
	}

	/** Test encoding and decoding all special codes, just the special codes though */
	@Test
	public void testSpecialCodes(){
		int move;
		int expectedCode;
		int gotCode;
		final int definedCodeSize=256;
		for(int i=0; i<definedCodeSize; ++i){
			expectedCode=i;
			move=encode(expectedCode,0,0,0);//only populate the special code field
			gotCode=getSpecialCode(move);
			assertEquals("Special Code field did not preserve the code",expectedCode,gotCode);
		}
	}
}