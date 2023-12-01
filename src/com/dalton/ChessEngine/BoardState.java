package com.dalton.ChessEngine;

/**
 * A record which represents a single state of the game board.
 * @author Dalton Herrewynen
 * @version 0.1
 */
public record BoardState(long unmoved,long EnPassant,long[] pieces){
}
