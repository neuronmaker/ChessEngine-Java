/*
File: CoordTest.java
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

import static com.dalton.ChessEngine.Coord.*;
import static com.dalton.ChessEngine.Types.*;
import static org.junit.Assert.*;

/**
 * Tests for the Coord class
 * @author Dalton Herrewynen
 * @version 1
 */
public class CoordTest{
	Coord coord, bottomLeft, bottomRight, topLeft, topRight;
	char[] letterIndex;
	final char[] numberIndex={'1','2','3','4','5','6','7','8'};

	@Before
	public void setupCoordsTest(){
		// Add any testing methods here
		coord=new Coord();
		bottomLeft=new Coord(0,0);
		bottomRight=new Coord(7,0);
		topLeft=new Coord(0,7);
		topRight=new Coord(7,7);
		letterIndex=new char[BOARD_SIZE];
		for(int i=0; i<BOARD_SIZE; ++i){
			letterIndex[i]=(char) ('a'+i);//generate a list of letters based on board size
		}
	}

	@After
	public void tearDownCoordsTest(){
		// Clean-up anything initialized in the setup method
		coord=null;
		bottomLeft=null;
		bottomRight=null;
		topLeft=null;
		topRight=null;
	}

	/** Test and verify that the copier works */
	@Test
	public void copyConstructorTest(){
		Coord got;
		for(int i=0; i<TOTAL_SQUARES; ++i){
			coord=new Coord(i);
			got=new Coord(coord);
			assertEquals("Expected: "+coord+" Got: "+got,coord.getIndex(),got.getIndex());
			assertNotSame("Should not point to same instance.",coord,got);
		}
	}

	/** Verify the coordinate constructor works */
	@Test
	public void XYConstructorTest(){
		for(int y=0; y<BOARD_SIZE; ++y){
			for(int x=0; x<BOARD_SIZE; ++x){
				coord=new Coord(x,y);
				assertTrue("Expected: ("+x+","+y+") Got: ("+coord.getX()+","+coord.getY()+")",
						x==coord.getX() && y==coord.getY());
			}
		}
	}

	/** Verify the index constructor works */
	@Test
	public void IndexConstructorTest(){
		for(int i=0; i<TOTAL_SQUARES; ++i){
			coord=new Coord(i);
			assertEquals("Expected: "+i+" Got: "+coord.getIndex(),i,coord.getIndex());
		}
	}

	/** Tests the setter for coordinate pairs */
	@Test
	public void setCoordsTest(){
		for(int x=0; x<BOARD_SIZE; ++x){
			for(int y=0; y<BOARD_SIZE; ++y){
				assertTrue("The coordinates ("+x+","+y+") should be valid (or board size is set wrong).",coord.setCoord(x,y));
				coord.setCoord(x,y);
			}
		}
		assertFalse("Negative x should not be valid",coord.setCoord(-1,0));
		assertFalse("Negative y should not be valid",coord.setCoord(0,-1));
		assertFalse("Too large x should not be valid",coord.setCoord(BOARD_SIZE+1,0));
		assertFalse("Too large y should not be valid",coord.setCoord(0,BOARD_SIZE+1));
	}

	/** Setter and getter for index based use */
	@Test
	public void setAndGetIndexTest(){
		for(int i=-1; i<TOTAL_SQUARES+1; ++i){
			assertEquals("Value "+i+" did not match validity of the checker function",Coord.isIndexValid(i),coord.setIndex(i));
			if(Coord.isIndexValid(i)){
				coord.setIndex(i);
				assertEquals("Coordinates were not the same, supplied "+i+" got back "+coord.getIndex(),i,coord.getIndex());
			}
		}
	}

	/** Tests both getters for X and Y at same time */
	@Test
	public void getX_getY_Test(){
		for(int x=0; x<BOARD_SIZE; ++x){
			for(int y=0; y<BOARD_SIZE; ++y){
				coord.setCoord(x,y);
				assertEquals("X set and X got should be the same, got "+x+" and "+coord.getX(),x,coord.getX());
				assertEquals("Y set and Y got should be the same, got "+y+" and "+coord.getY(),y,coord.getY());
			}
		}
	}

	/** Validity test methods to ensure the test methods work */
	@Test
	public void isIndexValidTest(){
		//check valid index
		for(int i=0; i<TOTAL_SQUARES; ++i){
			assertTrue("Index "+i+" should be valid",Coord.isIndexValid(i));
		}
		//check invalid index
		assertFalse("Negative index should not be valid.",Coord.isIndexValid(-1));
		assertFalse("Index larger than max index should not be valid.",Coord.isIndexValid(TOTAL_SQUARES));
	}

	/** Validity test methods to ensure the test methods work */
	@Test
	public void isCoordValidTest(){
		//check all valid coordinates
		for(int x=0; x<BOARD_SIZE; ++x){
			for(int y=0; y<BOARD_SIZE; ++y){
				assertTrue("Coordinate ("+x+","+y+") should be considered valid.",Coord.isCoordValid(x,y));
			}
		}
		//check coordinates too far left and right
		assertFalse("X coordinate is too far left, should not be valid",Coord.isCoordValid(-1,0));
		assertFalse("X coordinate is too far right, should not be valid",Coord.isCoordValid(BOARD_SIZE,0));
		//check coordinates too far up and down
		assertFalse("Y coordinate is too far down, should not be valid",Coord.isCoordValid(0,-1));
		assertFalse("Y coordinate is too far up, should not be valid",Coord.isCoordValid(0,BOARD_SIZE));
	}

	/** Getting the X coordinate from an index, static method */
	@Test
	public void indexToXTest(){
		for(int i=0; i<TOTAL_SQUARES; ++i){
			int correct=i%BOARD_SIZE;
			int got=Coord.indexToX(i);
			assertEquals("Expected value for X: "+correct+" got: "+got,correct,got);
		}
	}

	/** Getting the Y coordinate from an index, static method */
	@Test
	public void indexToYTest(){
		for(int i=0; i<TOTAL_SQUARES; ++i){
			int correct=i/BOARD_SIZE;
			int got=indexToY(i);
			assertEquals("Expected value for Y: "+correct+" got: "+got,correct,got);
		}
	}

	/** Gets the correct letter for one axis, a component of PGN testing */
	@Test
	public void coordNumToCharTest(){
		for(int i=1; i<BOARD_SIZE; ++i){
			assertEquals("Letter index for "+i+" should be "+letterIndex[i-1]+" but is "+Coord.toLetter(i),
					Coord.toLetter(i),letterIndex[i-1]);
		}
	}

	/** Gets the correct number from a letter for one axis, a component of PGN testing */
	@Test
	public void coordChartoNumTests(){
		for(int i=1; i<BOARD_SIZE; ++i){
			assertEquals("Letter "+letterIndex[i-1]+" should become "+i+" but is "+Coord.fromLetter(letterIndex[i]),
					Coord.fromLetter(letterIndex[i-1]),i);
		}
	}

	/** Export in a format that can be used for PGN notation */
	@Test
	public void formatPGNTests(){
		//Test every tile, remember to add 1 to y because PGN is 1 based not 0 based
		for(int x=0; x<BOARD_SIZE; ++x){
			for(int y=0; y<BOARD_SIZE; ++y){
				coord.setCoord(x,y);
				assertEquals("X coordinate at: "+x+" should be: "+letterIndex[x]+" got: "+coord.getXPGN(),
						letterIndex[x],coord.getXPGN());
				assertEquals("Y coordinate at: "+y+" should be: "+numberIndex[y]+" got: "+coord.getYPGN(),
						numberIndex[y],coord.getYPGN());
			}
		}
	}

	/** Test the ability to take and use a string input in the constructor, slightly more difficult test than the documentation says is allowed */
	@Test
	public void PGNStringTests(){
		String PGNCoord;
		int[] expected, got;
		for(int x=0; x<BOARD_SIZE; ++x){
			for(int y=0; y<BOARD_SIZE; ++y){
				PGNCoord=String.valueOf(Coord.toLetter(x+1))+(y+1);
				coord.setFromPGN(PGNCoord);
				assertTrue("Coordinate "+PGNCoord+" from ("+x+","+y+") should be valid",coord.getSet());
				if(coord.getSet()){//only test for correctness if the coordinates were actually set
					expected=new int[]{x,y};
					got=new int[]{coord.getX(),coord.getY()};
					assertArrayEquals("Coordinate "+PGNCoord+" from ("+x+","+y+") gave ("+got[0]+","+got[1]+")",
							expected,got);
				}
			}
		}
		//run same test again with garbage in the coordinate
		String garbage="\"';: <>,./?!@#$%^&*()_+-=`~{}[]\\|";
		for(int x=0; x<BOARD_SIZE; ++x){
			for(int y=0; y<BOARD_SIZE; ++y){
				PGNCoord=garbage+Coord.toLetter(x+1)+garbage+(y+1);
				coord.setFromPGN(PGNCoord);
				assertTrue("Coordinate "+PGNCoord+" from ("+x+","+y+") should be valid",coord.getSet());
				if(coord.getSet()){//only test for correctness if the coordinates were actually set
					expected=new int[]{x,y};
					got=new int[]{coord.getX(),coord.getY()};
					assertArrayEquals("Coordinate "+PGNCoord+" from ("+x+","+y+") gave ("+got[0]+","+got[1]+")",
							expected,got);
				}
			}
		}
	}

	/** Sanity check to ensure that the set state tracker works, should return true when the coordinate is set correctly */
	@Test
	public void getSetTest(){
		assertFalse("Object should be void when it wasn't set to anything.",coord.getSet());
		coord.setCoord(0,0);
		assertTrue("Object should not flag void when it has been set.",coord.getSet());
	}

	/** Test the ability to shift this coordinate by a given offset */
	@Test
	public void shiftTest(){
		//test some valid shifts
		int distance=3;
		Coord center=new Coord(distance,distance);//be far enough away from the edge
		Coord destination=new Coord();
		for(int x=-distance; x<=distance; ++x){//shift to valid squares
			for(int y=-distance; y<=distance; ++y){
				destination.setIndex(center.getIndex());
				destination.addVector(x,y);
				assertEquals("Failure to shift X by "+x,center.getX(),destination.getX()-x);
				assertEquals("Failure to shift Y by "+y,center.getY(),destination.getY()-y);
			}
		}
		//go too far left
		destination.setIndex(bottomLeft.getIndex());
		assertFalse("Did not detect going too far left",destination.addVector(-1,0));
		destination.setIndex(bottomLeft.getIndex());
		assertFalse("Did not detect going too far right",destination.addVector(BOARD_SIZE,0));
		destination.setIndex(bottomLeft.getIndex());
		assertFalse("Did not detect going too far down",destination.addVector(0,-1));
		destination.setIndex(bottomLeft.getIndex());
		assertFalse("Did not detect going too far up",destination.addVector(0,BOARD_SIZE));
	}

	/** Shift and copy method needs to return a new (shifted) coordinate */
	@Test
	public void shiftCopyTest(){
		int distance=3;
		Coord start=new Coord(distance,distance);//be far enough away from the edge
		Coord destination;
		for(int x=-distance; x<=distance; ++x){//shift to valid squares
			for(int y=-distance; y<=distance; ++y){
				destination=start.getShiftedCoord(x,y);
				assertNotSame("Should not be referencing the old coordinate",destination,start);
				assertEquals("Failure to shift X by "+x,start.getX(),destination.getX()-x);
				assertEquals("Failure to shift Y by "+y,start.getY(),destination.getY()-y);
			}
		}
		//go too far left
		destination=bottomLeft.getShiftedCoord(-1,0);
		assertFalse("Did not detect going too far left",destination.getSet());
		destination=bottomLeft.getShiftedCoord(BOARD_SIZE,0);
		assertFalse("Did not detect going too far right",destination.getSet());
		destination=bottomLeft.getShiftedCoord(0,-1);
		assertFalse("Did not detect going too far down",destination.getSet());
		destination=bottomLeft.getShiftedCoord(0,BOARD_SIZE);
		assertFalse("Did not detect going too far up",destination.getSet());
	}

	/** Test copy method for memory pointer issues, value not pointer should be same */
	@Test
	public void testCopyPointers(){
		coord=new Coord(4,4);
		Coord got=coord.copy();
		assertNotSame("Should not have same reference",coord,got);
		assertEquals("Should have same value though",coord.toString(),got.toString());
	}

	/** Test the ArrayList sorting functionality */
	@Test
	public void testSorting(){
		ArrayList<Coord> coordList=new ArrayList<>();
		int[] indices={1,0,2,5,40,8,60,64,55,22,33,9,61,62,53,54,58,57,43,6,10,11,13,12,14};//arbitrary list of indices
		for(int i=0; i<indices.length; ++i){
			coordList.add(new Coord(indices[i]));
		}
		coordList.sort(Comparator.comparingInt(Coord::getIndex));
		coord=coordList.get(0);
		for(int i=1; i<coordList.size(); ++i){
			assertTrue("Previous index value should be lower than this one: old:"
							+coord.getIndex()+" new: "+coordList.get(i).getIndex(),
					coord.getIndex()<=coordList.get(i).getIndex());
			coord=coordList.get(i).copy();
		}
	}

	/** Test the ability to generate Coord from a mask */
	@Test
	public void testCoordsFromMask(){
		for(int i=0; i<TOTAL_SQUARES; ++i){
			coord=new Coord();//blank it out
			assertTrue("Should not flag an error at"+i,coord.setMask(1L<<i));//shift by i to simulate all 64 single square masks
			assertEquals("Mismatch at "+i,i,coord.getIndex());
		}
	}

	/** Test the ability to generate index from a mask using <b>FAST</b> methods */
	@Test
	public void testIndexFromMaskSTATIC(){
		for(int i=0; i<TOTAL_SQUARES; ++i){
			assertEquals("Default method: Mismatch at "+i,i,Coord.maskToIndex(1L<<i));//shift by i to simulate all 64 single square masks
		}
	}
	/** Test the ability to generate the next index from a mask with multiple bits using <b>FAST</b> methods */
	@Test
	public void testNextIndexFromMaskSTATIC(){
		long mask;
		for(int i=0; i<TOTAL_SQUARES; ++i){
			for(int j=i; j<TOTAL_SQUARES; ++j){
				mask=(1L << i) | (1L << j);//add masks together
				if(i==j){//in case of one bit, should signal failure
					assertEquals("Should signal failure when there are no other bits in the mask"+maskString(mask)
							,ERROR_INDEX,Coord.maskToNextIndex(mask,i));
				}else{
					assertEquals("Should find the next index, it exists "+maskString(mask),
							j,Coord.maskToNextIndex(mask,i));
				}
			}
		}
	}

	/** Test the ability to generate Coord from a mask */
	@Test
	public void testGetMask(){
		long mask;
		for(int i=0; i<TOTAL_SQUARES; ++i){
			coord=new Coord(i);//blank it out
			mask=coord.getMask();
			assertEquals("Mismatch at "+i,1L<<i,mask);
		}
	}

	/** Test the Static index shift method */
	@Test
	public void testIndexShifter(){
		for(int i=0; i<TOTAL_SQUARES; ++i){//for each square
			for(int y=0; y<BOARD_SIZE-indexToY(i); ++y){//shift up by every y that fits
				for(int x=0; x<BOARD_SIZE-indexToX(i); ++x){//and right by every x that fits
					assertEquals("Mismatch at "+i+" -> ("+x+","+y+")="+XYToIndex(x,y),i+XYToIndex(x,y),shiftIndex(i,x,y));
				}
			}
			for(int y=-indexToY(i); y<=0; ++y){//shift down by every y that fits
				for(int x=-indexToX(i); x<=0; ++x){//and left by every x that fits
					assertEquals("Mismatch at "+i+" -> ("+x+","+y+")="+XYToIndex(x,y),i+XYToIndex(x,y),shiftIndex(i,x,y));
				}
			}
		}
	}

	/** Test the static index shift validity checker */
	@Test
	public void testIndexShiftValid(){
		int oldx,oldy;
		for(int i=0; i<TOTAL_SQUARES; ++i){//on each square
			for(int y=0; y<BOARD_SIZE; ++y){//test each x and y, see if they get flagged as true or false correctly
				for(int x=0; x<BOARD_SIZE; ++x){
					oldx=indexToX(i);
					oldy=indexToY(i);
					//test positive shift
					assertEquals("Mismatch at "+Coord.orderedPair(i)+" by ("+x+","+y+")",isCoordValid(oldx+x,oldy+y),isShiftValid(i,x,y));
					//test negative shift
					assertEquals("Mismatch at "+Coord.orderedPair(i)+" by ("+-x+","+-y+")",isCoordValid(oldx-x,oldy-y),isShiftValid(i,-x,-y));
				}
			}
		}
	}

	/** Test the Static index shift method */
	@Test
	public void testMaskShifter(){
		for(int i=0; i<TOTAL_SQUARES; ++i){//for each square
			long mask=1L<<i;
			for(int y=0; y<BOARD_SIZE-indexToY(i); ++y){//shift up by every y that fits
				for(int x=0; x<BOARD_SIZE-indexToX(i); ++x){//and right by every x that fits
					assertEquals("Mismatch at "+i+" -> ("+x+","+y+")="+XYToIndex(x,y),i+XYToIndex(x,y),maskToIndex(shiftMask(mask,x,y)));
				}
			}
			for(int y=-indexToY(i); y<=0; ++y){//shift down by every y that fits
				for(int x=-indexToX(i); x<=0; ++x){//and left by every x that fits
					assertEquals("Mismatch at "+i+" -> ("+x+","+y+")="+XYToIndex(x,y),i+XYToIndex(x,y),maskToIndex(shiftMask(mask,x,y)));
				}
			}
		}
	}
}