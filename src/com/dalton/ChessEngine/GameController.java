package com.dalton.ChessEngine;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import static com.dalton.ChessEngine.PieceCode.*;
import static com.dalton.ChessEngine.Types.*;

/**
 * The Game controller
 * @author Dalton Herrewynen
 * @version 0
 */
public class GameController{
	Scanner scanner;
	Stack<BoardState> states;
	Stack<BoardState> undoBuffer;
	Board board;
	boolean playerColor;
	boolean isWhiteAI,isBlackAI;
	int WhiteAILevel;
	int BlackAILevel;

	/** Runs the game */
	public void startPrimaryLoop(){
		String command="-start";
		while(!command.equalsIgnoreCase("-quit")){
			switch(command){
				case "-start":
					showBoard();
					showConfig();
				case "":
				case "-help":
					help();
					break;
				case "-show":
					showBoard();
					break;
				case "-settings":
					settings();
					break;
				case "-undo":
					undo();
					break;
				case "-redo":
					redo();
					break;
				default:
					if(parseMove(command)==SUCCESS) playerColor=!playerColor;
					showBoard();
					break;
			}
			System.out.print(Types.getTeamString(playerColor)+" -> ");
			command=scanner.nextLine();
		}
	}

	/**
	 * Parses a move from the command line, does not do PGN
	 * @param move The text of the first
	 * @return True if a move was made, False if not
	 */
	private boolean parseMove(String move){
		ArrayList<Integer> legalMoves;
		Coord start=new Coord(move),end=new Coord();
		while(start.isSet()==UNSET && board.getSquare(start.getIndex())!=Blank){
			System.out.println("invalid coordinate try again (type -abort to abort):");
			move=scanner.nextLine();
			if(move.equalsIgnoreCase("-abort")) return false;
			start.setFromPGN(move);
		}
		legalMoves=PieceCode.pieceObj(board.getSquare(start.getIndex())).getMoves(board,start.getIndex());//store the legal moves for the piece here
		showBoard(start.getMask() | Move.destinationsToMask(legalMoves));//highlight this square and all legal moves
		System.out.print("TO? "+start.getPGN()+"-> ");
		move=scanner.nextLine();
		end.setFromPGN(move);
		while(end.isSet()==UNSET || Move.isBlank(Move.findMoveByDest(legalMoves,end.getIndex()))){//if the end isn't set or isn't in the piece's legal move destinations
			System.out.println("invalid coordinate try again (type -abort to abort):");
			move=scanner.nextLine();
			if(move.equalsIgnoreCase("-abort")) return false;
			end.setFromPGN(move);
		}
		makeMove(Move.findMoveByDest(legalMoves,end.getIndex()));//if here then we know the move is legal, go find it again... slow but this is CLI so not performance heavy
		return true;
	}

	/** settings screen */
	private void settings(){
		System.out.println("Settings not working yet");
	}

	/** undo last move */
	private void undo(){
		System.out.println("Undo not working yet");
	}
	/** redo last move */
	private void redo(){
		System.out.println("Redo not working yet");
	}

	/** show help */
	public void help(){
		String helpText="Starting/Help Screen:\n"
				+"Make a move by specifying a coordinate (a1, b2, etc)\n"
				+"Legal squares will be marked, castling is done by moving the King 2 spaces\n"
				+"End the move by typing the end coordinate (a1, b2, etc)\n"
				+"See this message again with -help\n"
				+"Enter settings with -settings\n"
				+"Command -quit, -undo, -redo are self explanatory\n";
		System.out.println(helpText);
	}

	/** Prints the current configuration to the screen */
	public void showConfig(){
		String res="Current Configuration:\n";
		res+="White: ";
		if(isWhiteAI){
			res+="AI Depth: "+WhiteAILevel;
		}else{
			res+="Human/Terminal";
		}
		res+="\t Black: ";
		if(isBlackAI){
			res+="AI Depth: "+BlackAILevel;
		}else{
			res+="Human/Terminal";
		}
		System.out.println(res);
	}

	/**
	 * Applies an integer move to the Board and saves the state
	 * @param move the integer move to apply
	 */
	public void makeMove(int move){
		if(Move.isBlank(move)) return;//skip for blank moves
		undoBuffer.clear();//clear out the forward history, we're changing the past
		states.push(board.saveState());
		board.makeMove(move);
	}

	/** Prints the board on the screen */
	public void showBoard(){
		showBoard(0);
	}

	/**
	 * Prints the board with the squares highlighted
	 * @param highlightedSquares the mask of squares to highlight
	 */
	public void showBoard(long highlightedSquares){
		StringBuilder output=new StringBuilder();
		output.append("   Black Side")//Header
				.append("\n    A   B   C   D   E   F   G   H");//top bar
		for(int y=BOARD_SIZE-1; y>=0; --y){
			output.append("\n").append(y+1).append(" |");//start the line with a bar
			for(int x=0; x<BOARD_SIZE; ++x){
				boolean highlight=(0!=(highlightedSquares & (1L << Coord.XYToIndex(x,y))));//check if this square is in the highlighted mask
				int piece=board.getSquare(x,y);
				if(piece!=Blank){//print the piece's internal char representation
					if(highlight) output.append("|").append(PieceCode.decodeChar(piece)).append("||");
					else output.append(" ").append(PieceCode.decodeChar(piece)).append(" |");
				}else{//blank squares are blank
					switch((y+x)%2){//using number trickery to generate a checkerboard pattern
						case 1://white squares
							if(highlight) output.append("| ||");//highlighted squares
							else output.append("   |");//not highlighted
							break;
						case 0://black squares
							if(highlight) output.append("| ||");//highlighted squares
							else output.append(" . |");//not highlighted
							break;
					}
				}
			}
			output.append(" ").append(y+1);//end numbers
		}
		output.append("\n    A   B   C   D   E   F   G   H\n   White Side"   );
		System.out.println(output);
	}

	/** Initializes the game */
	public GameController(){
		states=new Stack<>();
		undoBuffer=new Stack<>();
		board=new Board(Board.DEFAULT);
		playerColor=true;
		isWhiteAI=false;
		isBlackAI=false;
		WhiteAILevel=0;
		BlackAILevel=0;
		scanner=new Scanner(System.in);
		System.out.println("Game initialized");
	}
}
