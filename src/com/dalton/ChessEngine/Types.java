/*
Project: Chess Engine
File: Types.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;

/**
 * Class that holds all types and enums that are globally used.<br/>
 * This class can't be instantiated, but it has static helper methods
 * @author Dalton Herrewynen
 * @version 1
 */
public abstract class Types{// class can't be instantiated, but it has static helper methods
	/** The teams (or colors) that the pieces can belong to */
	public static final boolean WHITE=true, BLACK=false;//white is lowercase
	/** Shorthands I am sure will help readability later */
	public static final boolean SUCCESS=true, FAIL=false, SET=true, UNSET=false;
	/** Constants in used to clarify the size of the board */
	public static final int BOARD_SIZE=8, TOTAL_SQUARES=64, XYMAX=7, XYMIN=0;

	/**
	 * Sets the capitalization of the char to match the case for that team
	 * @param piece The char representation for the piece
	 * @param team  The team of the piece
	 * @return WHITE is lowercase, BLACK is uppercase
	 */
	public static char setCaseFromTeam(char piece,boolean team){
		piece=(char) (piece | 0b0000000000100000);//bit tricks to get a lowercase value, ASCII was designed well
		if(team==BLACK && piece!=' ') piece=(char) (piece & 0b0000000001011111);//if BLACK and not a blank space, go to uppercase
		return piece;
	}

	/**
	 * Returns the team from a piece character
	 * @param piece The character representation of the piece
	 * @return WHITE if lowercase, BLACK if uppercase
	 */
	public static boolean getTeamFromChar(char piece){
		return (piece>='a' && piece<='z');
	}

	/**
	 * Checks if a <code>char</code> is upper case
	 * @param letter the char to check the case
	 * @return TRUE if uppercase, FALSE if lowercase
	 */
	public static boolean isUppercase(char letter){
		return (letter>='A' && letter<='Z');
	}

	/**
	 * Gets a human-friendly string of the Team
	 * @param team The Team of the piece
	 * @return String representing the Team
	 */
	public static String getTeamString(boolean team){
		if(team==WHITE) return "White";
		return "Black";
	}

	/**
	 * Forces a char to uppercase using bitwise masking
	 * @param c To uppercase
	 * @return upper case version of c
	 */
	public static char charUppercase(char c){
		return (char) (c & 0b0000000001011111);
	}

	/**
	 * Unoptimized method to convert a long to its binary string
	 * @param mask The long to check
	 * @return A string on 0's and 1's
	 */
	public static String maskString(long mask){
		StringBuilder representation=new StringBuilder();
		for(int i=63; i>=0; --i){
			representation.append(((mask & (1L << i))==0)? '0' : '1');// shift and test for equality with 0
		}
		return representation.toString();
	}

	/**
	 * Reverses a mask from front to back. Useful for rotations
	 * @param mask The mask to reverse
	 * @return Reversed version of mask
	 */
	public static long reverseMask(long mask){
		long rotated=0;
		for(int i=0; i<64; ++i){// always 64 bits so magic number is OK here
			rotated=rotated << 1;// move it left to expose a new right most bit
			rotated|=(mask >>> i) & 1L;// put the Ith bit on the right side of the new mask
		}
		return rotated;
	}
}