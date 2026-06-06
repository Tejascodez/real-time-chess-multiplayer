package com.tejas.multiplayer_chess.engine.pieces;

import com.tejas.multiplayer_chess.engine.Board;
import com.tejas.multiplayer_chess.engine.Box;
import com.tejas.multiplayer_chess.model.PlayerColor;

public class Rook extends Piece {

    public Rook(PlayerColor color) {

        super(color);
    }

    @Override
    public boolean isValidMove(
            int sr,
            int sc,
            int er,
            int ec,
            Board board
    ) {

        if (sr != er && sc != ec) {

            return false;
        }

        int rowDirection = Integer.compare(er, sr);
        int colDirection = Integer.compare(ec, sc);

        int currentRow = sr + rowDirection;
        int currentCol = sc + colDirection;

        while (currentRow != er || currentCol != ec) {

            Box box = board.getBox(currentRow, currentCol);

            if (box.getPiece() != null) {

                return false;
            }

            currentRow += rowDirection;
            currentCol += colDirection;
        }

        return true;
    }
}