/*
Project: Chess Engine
File: Main.java
Copyright (C) 2023 Dalton Herrewynen. All Rights Reserved
 */
package com.dalton.ChessEngine;
/**
 * The central boot loading class for this project
 * @author Dalton Herrewynen
 * @version 1
 */
public class Main{
	public static void main(String[] args){
		GameController game=new GameController();
		game.startPrimaryLoop();
	}
}