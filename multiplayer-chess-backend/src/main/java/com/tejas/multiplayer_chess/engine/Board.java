package com.tejas.multiplayer_chess.engine;

import com.tejas.multiplayer_chess.engine.pieces.Piece;

public class Board {

    private Box[][] boxes;

    public Board() {
        boxes = new Box[8][8];
        initializeBoard();
    }

    public void initializeBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boxes[row][col] = new Box(row, col, null);
            }
        }
    }

    public Box getBox(int row, int col) {
        return boxes[row][col];
    }

    public void placePiece(int row, int col, Piece piece) {
        boxes[row][col].setPiece(piece);
    }

    /**
     * Creates a deep copy of the board.
     * Pieces are shared references — that's fine because
     * we only ever call setPiece() on the copy, never mutate
     * the Piece objects themselves.
     */
    public Board deepCopy() {
        Board copy = new Board();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = this.boxes[row][col].getPiece();
                copy.placePiece(row, col, piece); // piece ref is fine
            }
        }
        return copy;
    }
}