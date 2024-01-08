package com.dalton.ChessEngine;

import static com.dalton.ChessEngine.Types.*;

/**
 * Handles all coordinate and position related operations
 * @author Dalton Herrewynen
 * @version 1
 */
public class Coord{
	/** Directions for line diagonal line checks */
	public static final int NE=7, NW=9, SE=-9, SW=-7;
	/** This is the index that flags an error in searching or something */
	public static final int ERROR_INDEX=-1;
	public static final char MAX_LETTER='a'+BOARD_SIZE-1;//gets the highest letter based on the BOARD_SIZE
	private boolean isSet=UNSET;
	private int index;

	//Constructors

	/** Default constructor is used mostly for constructor chaining and for making a blank object */
	public Coord(){
		isSet=false;
	}

	/** Copy from existing coordinate */
	public Coord(Coord oldCoord){
		isSet=oldCoord.getSet();
		index=oldCoord.index;
	}

	/**
	 * Constructs a coordinate from the index provided
	 * @param newIndex index integer directly inserted
	 */
	public Coord(int newIndex){
		this();
		isSet=setIndex(newIndex);//make true if successfully set
	}

	/**
	 * Constructs a coordinate on the board from x and y coordinates
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 */
	public Coord(int x,int y){
		this();
		isSet=setCoord(x,y);//make true if successfully set
	}

	/**
	 * Constructs a coordinate on the board from PGN formatted tile address (a1-h8)
	 * @param x the X coordinate 1-8
	 * @param y the Y coordinate
	 */
	public Coord(char x,int y){
		this();
		isSet=setFromPGN(x,y);//make true if successfully set
	}

	/**
	 * Constructs a coordinate from the String notation
	 * @param PGNCoord String PGN letter-number format coordinate
	 */
	public Coord(String PGNCoord){
		this();
		isSet=setFromPGN(PGNCoord);
	}

	/**
	 * Sets the coordinate from the first 1 in a mask
	 * @param mask The mask to convert into a coord
	 */
	public Coord(long mask){
		this();
		setIndex(maskToIndex(mask));
	}
	//Accessors

	/**
	 * Generates and shifts a copy of this coordinate, I did this copy then shift too much by hand, got to automate
	 * @param x The X component of the shift
	 * @param y The Y component of the shift
	 * @return new Coord, blank if off board or this object is not set, set and filled upon success
	 */
	public Coord getShiftedCoord(int x,int y){
		if(isSet==UNSET) return new Coord();//return fully blank coord if this one is unset
		x+=indexToX(index);//shift
		y+=indexToY(index);
		return new Coord(x,y);
	}

	/**
	 * Shifts this coordinate by an offset
	 * @param x The x-axis offset
	 * @param y The y-axis offset
	 * @return True if the offset applied to coordinate is still valid and the coordinate was set, False if anything went wrong
	 */
	public boolean addVector(int x,int y){
		x+=indexToX(index);
		y+=indexToY(index);
		index=XYToIndex(x,y);
		return isSet && isCoordValid(x,y);//flag if coordinate is not set or if the new coordinate is invalid
	}

	/**
	 * Copies this chess coord, useful for passing without passing the same reference (man I miss C pointer madness)
	 * @return A copy of this coordinate object
	 */
	public Coord copy(){
		return new Coord(this);
	}

	/**
	 * Checks if the coordinates are loaded
	 * @return True if the coordinates are loaded, False if unset
	 */
	public boolean isSet(){
		return isSet;
	}

	/**
	 * Checks if the coordinates are loaded, wrapper for isSet because I kept mixing them up
	 * @return True if the coordinates are loaded, False if unset
	 */
	public boolean getSet(){
		return isSet();
	}

	/**
	 * Sets the internal location from a user supplied index and checks if it's within board bounds
	 * @param givenIndex An index that fits within the board
	 * @return True if the point exists, False if it's off the board
	 */
	public boolean setIndex(int givenIndex){
		if(isIndexValid(givenIndex)){//only go if the index is valid
			isSet=true;//if the coords are not flagged as set, then now they are
			index=givenIndex;
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Get the mask of this coordinate object
	 * @return A long with a single 1 in it as it's a mask for the Board object
	 */
	public long getMask(){
		return indexToMask(index);
	}

	/**
	 * Sets the coord from a mask
	 * @param mask The 64bit integer used as a mask
	 * @return True if successful, False if there was a problem
	 */
	public boolean setMask(long mask){
		int ind=maskToIndex(mask);
		if(!isIndexValid(ind)) return false;//flag error if there was an issue
		index=ind;
		return true;
	}

	/**
	 * Sets to point to the tile which is referenced by the given X and Y coordinates
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return True if the point exists on the board, false otherwise
	 */
	public boolean setCoord(int x,int y){
		if(isCoordValid(x,y)){
			isSet=true;//if the coords are not flagged as set, then now they are
			index=XYToIndex(x,y);
			return true;
		}else{//on invalid coordinate, flag an error
			return false;
		}
	}

	/**
	 * Gets the tile index stored in this object
	 * @return The referenced tile index
	 */
	public int getIndex(){
		return index;
	}

	/**
	 * Gets the X coordinate from the object
	 * @return The X coordinate
	 */
	public int getX(){
		return indexToX(index);
	}

	/**
	 * Gets the Y coordinate from the object
	 * @return The Y coordinate
	 */
	public int getY(){
		return indexToY(index);
	}


	/**
	 * Generates a printable ordered pair notation of the internal coordinate
	 * @return String of the coordinate or a Null and Void message
	 */
	public String toString(){
		if(isSet) return orderedPair(index);
		else return "Not Set";
	}
	//PGN letter-number format accessors

	/**
	 * Sets the internal state from the first valid PGN coordinate in a string, ignoring non-letter, non-number characters
	 * @param PGNCoord Contains the PGN coordinate somewhere.
	 * @return True if the coordinate was valid, False if there was an error
	 */
	public boolean setFromPGN(String PGNCoord){
		char x=' ', cursor;//set these to invalid values for now
		int y=-1, i;
		for(i=0; i<PGNCoord.length(); ++i){//find one letter, ignore things like spaces
			cursor=PGNCoord.charAt(i);
			if((cursor>='a' && cursor<=MAX_LETTER) ||
					(cursor>='A' && cursor<=MAX_LETTER-32)){//subtract 32 to change to uppercase because ASCII is ingenious (bit flip)
				x=cursor;//found the needed char
				break;//break the loop
			}
		}
		for(; i<PGNCoord.length(); ++i){//find next number, ignore things like spaces
			cursor=PGNCoord.charAt(i);
			if(cursor>='0' && cursor<='9'){
				y=cursor-'0';//convert char to integer because ASCII has digits sequentially
				break;
			}
		}
		if(x==' ' || y==-1) return false;//if the string did not contain a valid coordinate, flag an error
		else return setFromPGN(x,y);//else try to set the coordinate
	}

	/**
	 * Sets a coordinate internals from the letter-number notation a1=(0,0) and h8=(7,7)
	 * @param x X coordinate letter (a-h)
	 * @param y Y coordinate number (1-8)
	 * @return True if point is on board, False if not
	 */
	public boolean setFromPGN(char x,int y){
		return setCoord((fromLetter(x)-1),(y-1));
	}

	/**
	 * Returns a pretty printed version of the coordinate
	 * @return letter number pair a1 -> h8
	 */
	public String getPGN(){
		return indexToPGN(index);
	}

	/**
	 * Turns an index into a pretty printed Coordinate
	 * @param index the board index
	 * @return a1->H8
	 */
	public static String indexToPGN(int index){
		return Types.charUppercase(toLetter(indexToX(index)+1))+""+(indexToX(index)+1);
	}

	/**
	 * Gets the X coordinate in number-letter (PGN compatible) format
	 * @return letter 'a' through 'h'
	 */
	public char getXPGN(){
		return toLetter(getX()+1);
	}

	/**
	 * Gets the Y coordinate in number-letter (PGN compatible) format
	 * @return integer 1 through 8
	 */
	public char getYPGN(){
		return (char) ('0'+getY()+1);
	}

	/**
	 * Converts numbers to letter addresses, offset so indexed to 1 and not 0, does <b>NO</b> error checking
	 * @param num the number 1 through 8
	 * @return a letter mapped from a to h
	 */
	public static char toLetter(final int num){
		return (char) ('a'+(num-1));
	}

	/**
	 * Converts numbers to letter addresses, offset so indexed to 1 and not 0, does <b>NO</b> error checking
	 * @param letter the letter 'a through 'h'
	 * @return a number from 1 to 8
	 */
	public static int fromLetter(char letter){
		if(letter>='A' && letter<='Z') letter=(char) (letter+32);//convert to lower case if uppercase
		return 1+(letter-'a');
	}

	/**
	 * Converts a coordinate to a numerical index, does not error check
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 * @return The calculated index
	 */
	public static int XYToIndex(final int x,final int y){
		return (y*BOARD_SIZE+x);
	}

	/**
	 * Shifts an integer index by a coordinate pair
	 * Renamed to match convention, refactored to use the constant instead of 8
	 * @param index The index to shift
	 * @param x     The X Coordinate
	 * @param y     The Y Coordinate
	 * @return A numerical index after shifting by X and Y
	 */
	public static int shiftIndex(final int index,final int x,final int y){
		return (index+x+y*BOARD_SIZE);
	}

	/**
	 * Checks if a shift will still be on the board
	 * @param index The index to be shifted
	 * @param x     X coordinate
	 * @param y     Y coordinate
	 * @return True if the shift wil be on the board, False otherwise
	 */
	public static boolean isShiftValid(final int index,int x,int y){
		x+=indexToX(index);
		y+=indexToY(index);
		return isCoordValid(x,y);
	}

	/**
	 * Shifts a bit mask by a coordinate pair
	 * @param mask The bitmask to shift around
	 * @param x    The X Coordinate
	 * @param y    The X Coordinate
	 * @return A shifted bitmask (64-bit integer used as a bitmask)
	 */
	public static long shiftMask(long mask,final int x,final int y){
		if(XYToIndex(x,y)<0) return mask >>> (-XYToIndex(x,y));//if negative offset, then shift right by a positive
		else return mask << XYToIndex(x,y);//if positive offset, just left shift
	}

	/**
	 * Gets the X Coordinate from the given index, does not error check
	 * @param givenIndex the index to be converted
	 * @return the X coordinate
	 */
	public static int indexToX(final int givenIndex){
		return givenIndex%BOARD_SIZE;
	}

	/**
	 * Gets the Y Coordinate from the given index, does not error check
	 * @param givenIndex the index to be converted
	 * @return the Y coordinate
	 */
	public static int indexToY(final int givenIndex){
		return givenIndex/BOARD_SIZE;
	}

	/**
	 * Gets the first 1 in a bitmask and gets the equivalent index, Literally 2X faster than the old method
	 * @param mask The bitmask to extract the index from
	 * @return integer position of first 1 bit in the mask
	 */
	public static int maskToIndex(long mask){
		return maskToNextIndex(mask,-1);//wrapper method, start at 0 index
	}

	/**
	 * Gets the next index from a multiple bit mask
	 * @param mask      The mask to search
	 * @param lastIndex Where the last was found, search from here
	 * @return integer position of next 1 bit in the mask
	 */
	public static int maskToNextIndex(long mask,final int lastIndex){
		final int offset=4;
		for(int i=lastIndex+1; i<TOTAL_SQUARES; i+=offset){//this is going to be used in high impact areas, SPEED MATTERS
			//Andrew, you may not like this, but it's at LEAST 2x faster than ifs, not including pipeline flushes
			switch((int) ((mask >>> i) & 0b1111)){//going to try shifting by 4 and masking out just those 4 bits
				case 0b0001://checking 4 bits at a time this way
				case 0b0011:
				case 0b0101:
				case 0b0111:
				case 0b1001:
				case 0b1011:
				case 0b1101:
				case 0b1111://check all with first bit set
					return i;//position 0, add 0 to i and return
				case 0b0010:
				case 0b0110:
				case 0b1010:
				case 0b1110://check all with 2nd bit set
					return i+1;//position 1 add 1 to i
				case 0b0100://all with 3rd bit set
				case 0b1100:
					return i+2;//etc
				case 0b1000:
					return i+3;
			}
		}
		return ERROR_INDEX;//failure if we get here
	}

	/**
	 * Generates a mask for filtering one square at a time
	 * @param index which square make mask for
	 * @return a long which is a mask with a single 1 in it
	 */
	public static long indexToMask(int index){//we may not need this here, it may be faster to just write it, it's here for readability
		return 1L << index;
	}

	/**
	 * Converts an index to an ordered pair
	 * @param index the index
	 * @return A String with ordered pair notation
	 */
	public static String orderedPair(final int index){
		if(isIndexValid(index)) return "("+indexToX(index)+","+indexToY(index)+")";
		else return "Invalid Index";
	}

	/**
	 * Checks if the index provided exists on the board
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return True if both coordinates are on the board, False if one or both are not
	 */
	public static boolean isCoordValid(final int x,final int y){
		return ((x>=0 && x<BOARD_SIZE) && (y>=0 && y<BOARD_SIZE));
	}

	/**
	 * Checks if the index provided exists on the board
	 * @param givenIndex The desired numerical index
	 * @return True if index is on the board, False if not
	 */
	public static boolean isIndexValid(final int givenIndex){
		return (givenIndex>=0 && givenIndex<(TOTAL_SQUARES));
	}
}