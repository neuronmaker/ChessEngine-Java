# Chess Engine

My personal version of the Chess back end that could have been used in my computing science group project. Due to design difficulties and coordination issues, I was unable to get some of the improvements into the main project. This project was started as a last gasp to get something that could be used if our Chess game lost against another group. Now it's a project to see what could have been done with my design had the group project been designed and coordinated correctly.

## Intellectual property

Since I wrote the overwhelming majority of the latest version of the old game, I am the author of those lines of code and I own the rights to use what I wrote. Any contested lines are going to be removed before Version 1 anyway, they're only there to be compatible with the old game, a task which is no longer relevant and would actively hinder this versions if I left those parts as-is.

## Design

The backend is stripped down and made to be more efficient than the original group project. Only code that I wrote will survive in this project. So far, only the scoring algorithm was borrowed from the other authors of my group project, and I plan to remove and re-write it anyway since I can write something better.

The design is not a 1:1 replacement of the old game, mostly because of the headache of converting to the systems and designs used by some parts of the old game (some methods actively use both the old and new move types... not good). Hence, this is more of a fresh start.

### Board

For speed and memory reasons, I found that the original version of the game ran faster eliminated null pointer problems when using a bitboard and passing integer piece codes around instead of Piece pointers. Searching is a lot faster when using a bitboard, and integer piece codes have some unique advantages including the possibility of using `switch` statements and look up tables for speed.

The board does not calculate any moves or do anything interesting, its only job is to store and update the board and piece positions:

- Store pieces
- Make moves
- Add pieces
- Delete pieces
- Get masked piece positions

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

- To Do:
  - Go over the Git log of the old group project
    - Remove and parts that are not entirely my creation
    - Replace said parts with faster and all around better versions
  - My own PGN handling class
- In the works
  - Engine
    - AI
      - Minimax algorithm
      - Configurable difficulty (search depth)
      - Threading
    - Human player
- Done
  - Working CLI
  - Board
    - Convert to ASCII art
    - Add/delete pieces
    - Take moves
    - Mask search
    - Piece search, gets the mask
    - Team search, like a piece search but for all pieces on that team
  - Pieces
      - Move generation
      - Look up based on Piece codes
  - Write README.md file
