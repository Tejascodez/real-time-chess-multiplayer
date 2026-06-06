package com.tejas.multiplayer_chess.engine.pieces;

import com.tejas.multiplayer_chess.engine.Board;
import com.tejas.multiplayer_chess.model.PlayerColor;

public class Knight extends Piece {

    public Knight(PlayerColor color) {

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

        int rowDiff = Math.abs(sr - er);
        int colDiff = Math.abs(sc - ec);

        return (rowDiff == 2 && colDiff == 1) ||
               (rowDiff == 1 && colDiff == 2);
    }
}