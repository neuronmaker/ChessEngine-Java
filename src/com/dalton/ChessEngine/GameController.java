package com.dalton.ChessEngine;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

import static com.dalton.ChessEngine.PieceCode.*;
import static com.dalton.ChessEngine.Types.*;

/**
 * The Game controller
 * @author Dalton Herrewynen
 * @version 0.3
 */
public class GameController{
	Scanner scanner;
	Stack<Board> states;
	Stack<Board> undoBuffer;
	Stack<String> PGNMoves, PGNUndoBuffer;
	Board board;
	Engine engine;
	boolean playerColor;
	boolean isWhiteAI, isBlackAI;
	int turns, halfMoveClock;//how many turns and how many moves since a capture or pawn advance
	int WhiteAILevel;
	int BlackAILevel;

	/** Runs the game */
	public void startPrimaryLoop(){
		String command="-start";//display starting screen and then prompt for input
		while(!command.equalsIgnoreCase("-quit") && gameRunning()){
			switch(command){
				case "-start":
				case "-status":
				case "status":
				case "stat":
				case "-stat":
					showBoard();
					showConfig();
					System.out.println("Show help with \"-help\" at the prompt");
					break;
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
				case "-raw":
					System.out.println(board.toString());
					break;
				case "-history":
				case "history":
					printHistory();
					break;
				case "-conf":
				case "-config":
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
				case "ai":
				case "-ai":
					if(makeAiMove()==SUCCESS) playerColor=!playerColor;
					showBoard();
					break;
				default:
					if((isWhiteAI && playerColor==WHITE) || (isBlackAI && playerColor==BLACK)){//if player is AI
						System.out.println("Player is AI, making a move");
						if(makeAiMove()==SUCCESS) playerColor=!playerColor;//attempt to make a move
					}else{//if a human player
						if(parseMove(command)==SUCCESS) playerColor=!playerColor;//attempt to get move from user
					}
					showBoard();
					break;
			}
			if((isWhiteAI && playerColor==WHITE) || (isBlackAI && playerColor==BLACK)){
				System.out.println("Player is AI, type any non-command to let computer move");
			}
			System.out.print(Types.getTeamString(playerColor)+" -> ");
			command=scanner.nextLine();
		}
		System.out.println("Game over");
		printHistory();
	}

	/**
	 * Searches for game over events, win, lose, draw
	 * @return True if game is on, False if game ended
	 */
	public boolean gameRunning(){
		if(engine.isCheckmate(board,WHITE)) return false;//For if BLACK wins by checkmating WHITE
		if(engine.isCheckmate(board,BLACK)) return false;//For if WHITE wins by checkmating BLACK
		//add stalemates here
		if(board.searchPiece(KingW)==0 || board.searchPiece(KingB)==0) return false;//if either king is gone, game is over
		return true;
	}
	/**
	 * Tells the AI go generate a move, and the applies it to the board
	 * @return True upon success, False if there was a problem
	 */
	public boolean makeAiMove(){
		System.out.println("Making AI move");
		int move=engine.getBestMove(new Board(board),playerColor,(playerColor==WHITE)? WhiteAILevel : BlackAILevel);//Tell the engine what maximum depth to search
		System.out.println("Player: "+Types.getTeamString(playerColor)+": "+PGNConverter.getPGN(board,move)+": "+Move.describe(move));
		if(Move.isBlank(move)) return false;//if no legal moves found, flag error
		makeMove(move);//if a move was not blank, make it
		return true;//flag success
	}

	/**
	 * Attempt to get a pgn out of Algebraic notation
	 * @param pgn Algebraic notation to be decoded
	 * @return TRUE if pgn valid, FALSE otherwise
	 */
	public boolean makePGNMove(String pgn){
		int i=0, move;
		for(; i<pgn.length(); ++i){
			//loop until I find a letter or a number, then trim the starting chars from there
			if(pgn.charAt(i)!=' '){
				pgn=pgn.substring(i);
				break;
			}
		}
		move=PGNConverter.getMove(board,pgn,playerColor);
		if(Move.isBlank(move)) return false;//if a blank move then there was an error
		System.out.println("Move: "+Move.describe(move));
		makeMove(move);
		return true;
	}

	/**
	 * Parses a move from the command line, does not do PGN
	 * @param move The text of the first
	 * @return True if a move was made, False if not
	 */
	private boolean parseMove(String move){//TODO change this to return a move instead of making one
		if(move.length()>3 && move.substring(0,3).equalsIgnoreCase("PGN")){//look for a switch phrase that tells us to instead decode algebraic notation
			System.out.println("Making PGN move");
			return makePGNMove(move.substring(3));//spaghetti code, just chops off the switch phrase "PGN"
		}
		ArrayList<Integer> legalMoves;
		Coord start=new Coord(move), end=new Coord();
		while(start.isSet()==UNSET || board.getSquare(start.getIndex())==Blank || PieceCode.decodeTeam(board.getSquare(start.getIndex()))!=playerColor){//loop until the player selects one of their pieces
			if(move.equalsIgnoreCase("-show")) showBoard();
			else System.out.println("Invalid coordinate try again (type -abort to abort):");
			System.out.print(Types.getTeamString(playerColor)+" -> ");
			move=scanner.nextLine();
			if(move.equalsIgnoreCase("-abort")) return false;
			start.setFromPGN(move);
			if(PieceCode.decodeTeam(board.getSquare(start.getIndex()))==playerColor && board.getSquare(start.getIndex())!=Blank)
				System.out.println("Not your piece!");
		}
		legalMoves=Engine.getLegalMoves(board,board.getSquare(start.getIndex()));//get all moves for all pieces of this type
		legalMoves=Move.findMovesByStart(legalMoves,start.getIndex());//filter by starting index, this will be changed if I find a better way
		showBoard(start.getMask() | Move.destinationsToMask(legalMoves));//highlight this square and all legal moves
		System.out.print(Types.getTeamString(playerColor)+"TO? "+start.getPGN()+"-> ");
		move=scanner.nextLine();
		end.setFromPGN(move);
		while(end.isSet()==UNSET || Move.isBlank(Move.findMoveByDest(legalMoves,end.getIndex()))){//if the end isn't set or isn't in the piece's legal move destinations
			if(move.equalsIgnoreCase("-show")) showBoard(start.getMask() | Move.destinationsToMask(legalMoves));
			else System.out.println("Invalid coordinate try again (type -abort to abort):");
			System.out.print(Types.getTeamString(playerColor)+"TO? "+start.getPGN()+"-> ");
			move=scanner.nextLine();
			if(move.equalsIgnoreCase("-abort")) return false;
			end.setFromPGN(move);
		}
		int filteredMove=Move.findMoveByDest(legalMoves,end.getIndex());
		if(Move.isPawnPromotion(filteredMove)){//if there is a pawn promotion move, ask which piece to promote too.
			char promotionCode=' ';
			while(promotionCode==' '){//loop until the user gives a valid piece abbreviation
				System.out.println("Promote pawn to? Q-Queen, K/N-Knight, B-Bishop, R-Rook?");
				System.out.print("Piece? -> ");
				move=scanner.nextLine();
				if(move.equalsIgnoreCase("-abort")) return false;//abort and signal failure
				promotionCode=switch(charUppercase(move.charAt(0))){
					case 'Q' -> 'q';//queen
					case 'K','N' -> 'n';//knight
					case 'B' -> 'b';//bishop
					case 'R','C' -> 'r';//rook aka castle
					default -> {
						System.out.println("Invalid Abbreviation");
						yield ' ';
					}
				};
			}
			filteredMove=Move.encode(Move.getSpecialCode(filteredMove)|Move.pawnPromote,//add the pawn promotion flag
					PieceCode.encodeChar(promotionCode,playerColor),//set the new piece
					start.getIndex(),end.getIndex());//start and end index
		}
		makeMove(filteredMove);//if here then we know the move is legal, go find it again... slow but this is CLI so not performance heavy
		return true;
	}

	/**
	 * Applies an integer move to the Board and saves the state for undo functionality
	 * @param move the integer move to apply
	 */
	public void makeMove(int move){
		String pgn=PGNConverter.getPGN(board,move);
		if(Move.isBlank(move)) return;//skip for blank moves
		PGNUndoBuffer.clear();
		undoBuffer.clear();//clear out the forward history, we're changing the past
		PGNMoves.push(pgn);//push newly made moves to their stacks
		states.push(board.saveState());
		board.makeMove(move);
		System.out.println("PGN: "+pgn+" "+Move.describe(move));
	}

	/** undo last move */
	private void undo(){
		if(states.isEmpty()) return;//escape if no moves were made at this point
		undoBuffer.push(board.saveState());
		board.loadState(states.pop());
		PGNUndoBuffer.push(PGNMoves.pop());//Same as above but no intermediate board variable for PGN strings
		playerColor=!playerColor;//flip player
		showBoard();
	}

	/** redo last undone move */
	private void redo(){
		if(undoBuffer.isEmpty()){
			System.out.println("No moves to redo");
			return;//escape on error
		}
		PGNMoves.push(PGNUndoBuffer.pop());
		states.push(board.saveState());
		board.loadState(undoBuffer.pop());
		playerColor=!playerColor;//flip player back
		showBoard();
	}

	/** settings screen */
	private void settings(){
		String input="";
		boolean isAI;
		int givenLevel;
		int playerEdit;//1 white, 2 black, 0 invalid
		int i;//tracker of position
		while(!(input.equalsIgnoreCase("-save") || input.equalsIgnoreCase("save"))){
			showConfig();
			System.out.println("""
					-save saves current config
					'white' or 'w' edits White
					'black' or 'b' edits Black
					Type 'human' or 'ai' to select human or ai and then the search depth with a number
					Example: white ai 5
					""");
			System.out.print("-> ");
			input=scanner.nextLine().toLowerCase();//don't bother with case
			switch(input.substring(0,1)){
				case "w":
					playerEdit=1;//edit white
					break;
				case "b":
					playerEdit=2;//edit black
					break;
				case "-":
					playerEdit=0;//not editing a player
					break;//break early on commands
				default:
					System.out.println("Invalid.");
					continue;
			}
			if(playerEdit!=0){
				isAI=false;//initialize to assume a human player
				//assuming all is well, hunt for the tokens
				for(i=0; i<input.length(); ++i){
					if(input.charAt(i)=='a'){//hunt for first letter of "ai"
						isAI=true;//set to make AI generate the move
						break;//break the loop
					}else if(input.charAt(i)=='h'){//hunt for first letter of "human"
						isAI=false;//set to prompt human for a move
						break;//break the loop
					}
				}
				for(; i<input.length(); ++i){//move forward until there is a space
					if(input.charAt(i)==' '){
						++i;//move ahead of the space
						break;//break the loop
					}
				}
				try{
					givenLevel=Math.abs(Integer.parseInt(input.substring(i)));
					if(playerEdit==1){//white
						isWhiteAI=isAI;
						WhiteAILevel=givenLevel;
					}else{//black
						isBlackAI=isAI;
						BlackAILevel=givenLevel;
					}
				}catch(Exception e){
					System.out.println("Cannot find a number, invalid expression");
				}
			}
		}
	}

	/** show help */
	public void help(){
		String helpText="""
				Help Screen:
				Make a move by specifying a coordinate (a1, b2, etc) or with "pgn" and a move
				Legal squares will be marked, castling is done by moving the King 2 spaces
				End the move by typing the end coordinate (a1, b2, etc)
				See this message again with -help
				Enter configuration with -config
				Print game history with -history
				Make the AI generate a move with "-ai"
				Command -quit, -undo, -redo are self explanatory
				""";
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
		res+="\nBlack: ";
		if(isBlackAI){
			res+="AI Depth: "+BlackAILevel;
		}else{
			res+="Human/Terminal";
		}
		System.out.println(res);
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
		for(int y=XYMAX; y>=0; --y){
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
		output.append("\n    A   B   C   D   E   F   G   H\n   White Side");
		System.out.println(output);
	}

	/** Prints the entire move history to the screen */
	public void printHistory(){
		String pgn;
		int line=1;//track moves and lines
		System.out.print("Game History");
		for(int i=0; i<PGNMoves.size(); ++i){
			if(i%2==0){
				System.out.print("\n"+line+".");//print the line numbers for even moves (when WHITE moves)
				++line;//then increase next line
			}
			pgn=PGNMoves.elementAt(i);
			while(pgn.length()<4){
				pgn+=' ';//pad the move until they are all the same length
			}
			System.out.print(" "+pgn);//print the move with padding
		}
		System.out.println();//print a blank buffer line
		showBoard();
	}

	/** Initializes the game */
	public GameController(){
		states=new Stack<>();
		undoBuffer=new Stack<>();
		PGNMoves=new Stack<>();
		PGNUndoBuffer=new Stack<>();
		board=new Board(Board.DEFAULT);
		playerColor=WHITE;
		isWhiteAI=false;
		isBlackAI=false;
		WhiteAILevel=4;
		BlackAILevel=4;
		scanner=new Scanner(System.in);
		engine=new Engine(1,30);//old game used 4, 10, and 30 as depth level
		System.out.println("Game initialized");
	}

	/**
	 * Makes the game AI vs AI
	 * @param bothLevel How many turns ahead are both side looking?
	 */
	public void startAIGame(int bothLevel){
		isWhiteAI=true;
		isBlackAI=true;
		WhiteAILevel=bothLevel;
		BlackAILevel=bothLevel;
	}
}
