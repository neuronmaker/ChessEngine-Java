/*
File: UtilsForTests.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved.
License is hereby granted to The Kings University to store, compile, run, and display this file for grading and educational purposes.
Ownership is to be held by the primary author.
Licence is granted to the secondary members as noted in the Authors.md file for display, running, compiling, and modification for use in their future projects. Just keep my name on functions I wrote.
 */
package com.dalton.ChessEngine;
import org.junit.Ignore;

import java.util.ArrayList;

import static com.dalton.ChessEngine.Types.*;
import static org.junit.Assert.assertEquals;

/**
 * Class that holds commonly used utility code used by unit tests
 * @author Dalton Herrewynen
 * @version 2
 */
@Ignore("Class not for testing")
public class UtilsForTests{
	/**
	 * Extracts the destination coordinates from a list of Moves
	 * @param givenMoves The ArrayList of Integer encoded moves
	 * @return a list of coordinates from the destination coordinates in the moves
	 */
	public static ArrayList<Coord> getDestCoords(ArrayList<Integer> givenMoves){
		ArrayList<Coord> coords=new ArrayList<>();
		for(int move: givenMoves){//put the destinations into a list
			coords.add(new Coord(Move.getEndIndex(move)));
		}
		return coords;
	}

	/**
	 * Generates a straight line of Coord starting at a given point, goes to edge of board regardless of what the line crosses
	 * @param start The point at which to start on
	 * @param x     X increment value
	 * @param y     Y increment value
	 * @return ArrayList of coordinates
	 */
	public static ArrayList<Coord> getLineOfCoords(Coord start,int x,int y){
		if((x==0 && y==0) || start.isSet()==UNSET) return null;//can't get a line when we can't move or if no position
		ArrayList<Coord> result=new ArrayList<>();
		Coord pos=new Coord(start);
		while(pos.addVector(x,y)){//shift and copy until coord reaches end of board
			result.add(pos.copy());
		}
		return result;
	}

	/**
	 * Finds capture moves in the move list, does not check for legality
	 * @param moves List (array) of moves encoded as integers
	 * @return All capture moves in an ArrayList
	 */
	public static ArrayList<Integer> findCaptures(ArrayList<Integer> moves,Board board){
		ArrayList<Integer> found=new ArrayList<>();
		if(moves!=null && board!=null){//only search if both variables are set
			for(int move: moves){
				if(Move.getSpecialCode(move)==Move.capture &&//if move is both normal
						board.getSquare(Move.getEndIndex(move))!=PieceCode.Blank){//and covers a non-blank space
					found.add(move);
				}
			}
		}
		return found;
	}

	/**
	 * Finds just move instances in the move list, does not check for legality
	 * @param moves List (array) of moves encoded as integers
	 * @return All just move integers in an ArrayList
	 */
	public static ArrayList<Integer> findJustMoves(ArrayList<Integer> moves,Board board){
		ArrayList<Integer> found=new ArrayList<>();
		if(moves!=null && board!=null){//only search if both variables are set
			for(int move: moves){
				if(Move.getSpecialCode(move)==Move.normalMove &&//if move is both normal
						board.getSquare(Move.getEndIndex(move))==PieceCode.Blank){//and covers a blank spot
					found.add(move);
				}
			}
		}
		return found;
	}

	/**
	 * Finds all the encoded moves with a given SpecialCode field value
	 * @param moves      List (array) of moves encoded as integers
	 * @param searchCode The specialCode to search against
	 * @return All encoded Move integers with the target SpecialCode in an ArrayList
	 */
	public static ArrayList<Integer> findMovesByCode(ArrayList<Integer> moves,int searchCode){
		ArrayList<Integer> found=new ArrayList<>();
		if(moves!=null){//only search if there are moves
			for(int move: moves){
				if(Move.getSpecialCode(move)==searchCode){//if the codes match
					found.add(move);//add to list
				}
			}
		}
		return found;
	}

	/**
	 * Tests a long string a few characters at a time so that:
	 * 1. Problems can be pinpointed to <i>where</i> they were made
	 * 2. So that the string actually fits on the Junit web page
	 * @param expected The expected string
	 * @param got      The string that was produced by the test
	 * @param step     How many chars to check at one time
	 */
	public static void testLongStringByChar(String expected,String got,int step){
		for(int i=0; i<Math.min(expected.length(),got.length()); i+=step){//check a few digits at a time, so they will display on the webpage properly
			assertEquals("Between "+i+"-"+(i+step),expected.substring(i,i+step),got.substring(i,i+step));
		}
		assertEquals("Expected and got strings were different lengths",expected.length(),got.length());
	}
}
