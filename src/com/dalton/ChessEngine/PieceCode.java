/*
File: PieceCode.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved.
License is hereby granted to The Kings University to store, compile, run, and display this file for grading and educational purposes.
Ownership is to be held by the primary author.
Licence is granted to the secondary members as noted in the Authors.md file for display, running, compiling, and modification for use in their future projects. Just keep my name on functions I wrote.
 */

package com.dalton.ChessEngine;

import static com.dalton.ChessEngine.Types.*;


/**
 * Handles all the operations related to Piece integer codes and their conversions
 * @author Dalton Herrewynen
 * @version 1
 * @see Board
 * @see Piece
 */
public abstract class PieceCode{
	/**
	 * Piece codes to make arrays easier to use, Pieces named with color abbreviation,
	 * positions chosen so a bit flip can flip teams, even WHITE, odd BLACK, Blank is <b>ANY</b> invalid number
	 */
	public static final int PawnW=0, PawnB=1, RookW=2, RookB=3, KnightW=4, KnightB=5, BishopW=6, BishopB=7,
			QueenW=8, QueenB=9, KingW=10, KingB=11, PIECE_TYPES=12, Blank=PIECE_TYPES;//Blank is ANY invalid code
	/** Used with the Piece codes to form a sort of table */
	private static final Piece[] PIECE_OBJECT={new Pawn(WHITE),new Pawn(BLACK),new Rook(WHITE),new Rook(BLACK),
			new Knight(WHITE),new Knight(BLACK),new Bishop(WHITE),new Bishop(BLACK),new Queen(WHITE),new Queen(BLACK),
			new King(WHITE),new King(BLACK)};

	/**
	 * Gets team from the piece code
	 * @param code Piece code
	 * @return WHITE or BLACK
	 */
	public static boolean decodeTeam(int code){
		return (code%2==0);//I laid out all BLACK pieces on odds and WHITE pieces on evens
	}
	/**
	 * Gets team from the piece code
	 * @param code Piece code
	 * @return WHITE or BLACK
	 */
	public static String decodeTeamString(int code){
		return Types.getTeamString(code%2==0);//I laid out all BLACK pieces on odds and WHITE pieces on evens
	}

	/**
	 * Gets the Piece Char from the internal Piece Code
	 * @param code Integer piece code
	 * @return The Char representation
	 */
	public static char decodeChar(int code){
		return switch(code){
			case PawnW -> 'p';
			case PawnB -> 'P';
			case RookW -> 'r';
			case RookB -> 'R';
			case KnightW -> 'n';
			case KnightB -> 'N';
			case BishopW -> 'b';
			case BishopB -> 'B';
			case QueenW -> 'q';
			case QueenB -> 'Q';
			case KingW -> 'k';
			case KingB -> 'K';
			default -> ' ';//default is blank
		};
	}

	public static Piece pieceObj(int code){
		if(code<0 || code>=PIECE_TYPES) return null;
		return PIECE_OBJECT[code];
	}

	/**
	 * Encodes a char into an integer Piece code
	 * @param letter Letter to encode
	 * @return Integer Piece Code
	 */
	public static int encodeChar(char letter){
		return switch(letter){
			case 'p' -> PawnW;
			case 'P' -> PawnB;
			case 'r' -> RookW;
			case 'R' -> RookB;
			case 'n' -> KnightW;
			case 'N' -> KnightB;
			case 'b' -> BishopW;
			case 'B' -> BishopB;
			case 'q' -> QueenW;
			case 'Q' -> QueenB;
			case 'k' -> KingW;
			case 'K' -> KingB;
			default -> Blank;//default is blank
		};
	}

	/**
	 * Encodes a char into an integer Piece code, ignores case and uses the provided team value instead
	 * @param letter Letter to encode
	 * @param team   Override the team (WHITE or BLACK)
	 * @return Integer Piece Code
	 */
	public static int encodeChar(char letter, boolean team){
		int offset=(team==WHITE)?0:1;//offset for later user
		return switch(charUppercase(letter)){//force a known case
			case 'P' -> PawnW+offset;//use the offset to encode the team as calculated earlier
			case 'R' -> RookW+offset;
			case 'N' -> KnightW+offset;
			case 'B' -> BishopW+offset;
			case 'Q' -> QueenW+offset;
			case 'K' -> KingW+offset;
			default -> Blank;//default is blank
		};
	}

	/**
	 * Gets the string representation without a null pointer exception
	 * @param code The piece code to look up
	 * @return The pretty printed String
	 */
	public static String decodePieceName(int code){
		if(code<0 || code>=PIECE_TYPES) return "Blank";
		return PIECE_OBJECT[code].toString();
	}
}