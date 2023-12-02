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
				case "-status":
				case "status":
				case "stat":
				case "-stat":
					showBoard();
					showConfig();
				case "":
				case "-help":
				case "help":
				case "h":
					help();
					break;
				case "-show":
				case "show":
					showBoard();
					break;
				case "-settings":
				case "-set":
				case "-conf":
				case "-config":
				case "settings":
				case "set":
				case "conf":
				case "config":
					settings();
					break;
				case "-undo":
				case "undo":
					undo();
					break;
				case "-redo":
				case "redo":
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
	 * Tells the AI go generate a move, and the applies it to the board
	 * @param AIColor WHITE or BLACK
	 * @return True upon success, False if there was a problem
	 */
	public boolean makeAiMove(boolean AIColor){

		return true;
	}

	/**
	 * Parses a move from the command line, does not do PGN
	 * @param move The text of the first
	 * @return True if a move was made, False if not
	 */
	private boolean parseMove(String move){//TODO change this to return a move instead of making one
		ArrayList<Integer> legalMoves;
		Coord start=new Coord(move),end=new Coord();
		while(start.isSet()==UNSET || board.getSquare(start.getIndex())==Blank || PieceCode.decodeTeam(board.getSquare(start.getIndex()))==playerColor){
			if(move.equalsIgnoreCase("-show")) showBoard();
			else System.out.println("invalid coordinate try again (type -abort to abort):");
			System.out.print(Types.getTeamString(playerColor)+" -> ");
			move=scanner.nextLine();
			if(move.equalsIgnoreCase("-abort")) return false;
			start.setFromPGN(move);
			if(PieceCode.decodeTeam(board.getSquare(start.getIndex()))==playerColor && board.getSquare(start.getIndex())!=Blank)
				System.out.println("Not your piece!");
		}
		legalMoves=PieceCode.pieceObj(board.getSquare(start.getIndex())).getMoves(board,start.getIndex());//store the legal moves for the piece here
		showBoard(start.getMask() | Move.destinationsToMask(legalMoves));//highlight this square and all legal moves
		System.out.print(Types.getTeamString(playerColor)+"TO? "+start.getPGN()+"-> ");
		move=scanner.nextLine();
		end.setFromPGN(move);
		while(end.isSet()==UNSET || Move.isBlank(Move.findMoveByDest(legalMoves,end.getIndex()))){//if the end isn't set or isn't in the piece's legal move destinations
			if(move.equalsIgnoreCase("-show")) showBoard();
			else System.out.println("invalid coordinate try again (type -abort to abort):");
			System.out.print(Types.getTeamString(playerColor)+"TO? "+start.getPGN()+"-> ");
			move=scanner.nextLine();
			if(move.equalsIgnoreCase("-abort")) return false;
			end.setFromPGN(move);
		}
		makeMove(Move.findMoveByDest(legalMoves,end.getIndex()));//if here then we know the move is legal, go find it again... slow but this is CLI so not performance heavy
		return true;
	}

	/** settings screen */
	private void settings(){
		String input="";
		boolean isAI;
		int givenLevel;
		int playerEdit;//1 white, 2 black, 0 invalid
		int i;//tracker of position
		while(!input.equalsIgnoreCase("-save")){
			showConfig();
			System.out.println("-save saves current config");
			System.out.println("'white' or 'w' edits White");
			System.out.println("'black' or 'b' edits Black");
			System.out.println("Type 'human' or 'ai' to select human or ai and then the search depth with a number");
			System.out.println("Example: white ai 5");
			System.out.print("-> ");
			input=scanner.nextLine().toLowerCase();//don't bother with case
			switch(input.substring(0,1)){
				case "w":
					playerEdit=1;
					break;
				case "b":
					playerEdit=2;
					break;
				case "-":
					playerEdit=0;
					break;//break early on commands
				default:
					System.out.println("Invalid.");
					continue;
			}
			if(playerEdit!=0){
				//assuming all is well, hunt for the tokens
				System.out.println(playerEdit);
				for(i=0; i<input.length(); ++i){
					if(input.charAt(i)==' '){
						++i;//move ahead of the space
						break;//break the loop
					}
				}
				isAI=(input.charAt(i)=='a');//first letter of "ai", stores if we want this player to be ai
				for(; i<input.length(); ++i){
					if(input.charAt(i)==' '){
						++i;//move ahead of the space
						break;//break the loop
					}
				}
				givenLevel=Math.abs(Integer.parseInt(input.substring(i)));
				if(playerEdit==1){//white
					isWhiteAI=isAI;
					WhiteAILevel=givenLevel;
				}else{//black
					isBlackAI=isAI;
					BlackAILevel=givenLevel;
				}
			}
		}
	}

	/** undo last move */
	private void undo(){
		undoBuffer.push(board.saveState());
		board.loadState(states.pop());
		playerColor=!playerColor;//flip player
		showBoard();
	}
	/** redo last move */
	private void redo(){
		states.push(board.saveState());
		board.loadState(undoBuffer.pop());
		showBoard();
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
