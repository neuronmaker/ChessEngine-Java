# Chess Engine
<!-- TOC -->
* [Chess Engine](#chess-engine)
  * [How to run](#how-to-run)
    * [Automated build](#automated-build)
    * [Minimal requirements](#minimal-requirements)
  * [Design](#design)
    * [Board](#board)
    * [Pieces](#pieces)
    * [Coordinates](#coordinates)
    * [PGN](#pgn)
  * [Features](#features)
  * [Intellectual property](#intellectual-property)
<!-- TOC -->
My personal version of the Chess back end that could have been used in my computing science group project. Due to design difficulties and coordination issues, I was unable to get some of the improvements into the main project. This project was started as a backup option that could be used if our Chess game failed.

## How to run

This is a minimal Java program. There are several ways to run this program:

### Automated build

I used the [Ant](https://ant.apache.org/) build tool for this project. If you have Ant installed, then all you need to do is to run the following command.

```shell
ant run
```

Ant will automatically compile the Java code, pack it into a `JAR` file, and then run that jar file for you.

### Minimal requirements

In order to run this program, you will need Java to be installed. Install Java for your system in the recommended way.

You will need to run two commands, one compiles the program, the other runs it.

```shell
javac -d "build" src/com/dalton/ChessEngine/*

java -classpath "build" com.dalton.ChessEngine.Main
```

This will only run the program, it will not create a distributable `JAR` file. The automated build process creates a `JAR` file anyway, using Ant is the preferred way to build and run.

## Design

The backend is stripped down and made to be more efficient than the original group project. But the front-end is just a simple command line interface, a full graphical interface is not needed since I am focusing only on the engine, not on the user interface... user interfaces for Chess already exist and can use external engines.

This design is fully my own, moving closer and closer to what I initially envisioned for the engine and its internals. Time constraints prevented me from making some of the improvements I wanted to implement.

### Board

A bitboard based chess board. Also contains some supporting methods such as the ones used to apply a move to the board. However, the board does not have any legality checking, its job is only to be a data structure with some minimal support methods.

The board does not calculate any moves or do anything interesting, its only job is to store and update the board and piece positions:

- Store pieces
- Make moves
- Add pieces
- Delete pieces
- Search pieces that fit into a bitmask

### Pieces

Pieces are objects, but I am only using one instance of each Piece and then calling methods from that one instance by looking it up in a table. This approach was selected because in the group project, it was a lot faster than using a piece array as the board and still conformed to the requirement to heavily use objects whenever possible... even at the expense of speed and readability.

### Coordinates

Coordinates are stored as either an index integer or as a bit mask depending on if there are multiple squares or if the code in question can use bitmask techniques for faster operations.

### PGN

PGN notation is handled by the `PGNHandler` class.

PGN handler class jobs:

- Take PGN and strip out any information not needed (comments, metadata, etc.)
- Break PGN string into easily processed elements (possibly an array of strings)
- Given a board state, find which square did a PGN move started from
- Chain these together to apply PGN to a starting board and get the result
- Convert a list of integer moves into a PGN string
  - Make this work even with a single move first and then loop it

## Features

- Minimal working CLI
- PGN handler class
- Board
  - Convert to ASCII art string
  - Add/delete pieces
  - Take moves
  - Mask search
  - Piece search, gets the mask
  - Team search, like a piece search but for all pieces on that team
- Pieces
    - Move generation
    - Look up based on Piece codes

## Intellectual property

All code in this repository was authored by me alone.