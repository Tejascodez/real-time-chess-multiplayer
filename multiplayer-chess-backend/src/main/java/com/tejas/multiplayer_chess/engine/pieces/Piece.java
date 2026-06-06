package com.tejas.multiplayer_chess.engine.pieces;

import com.tejas.multiplayer_chess.engine.Board;
import com.tejas.multiplayer_chess.model.PlayerColor;
import lombok.Getter;

@Getter
public abstract class Piece {

    protected PlayerColor color;

    public Piece(PlayerColor color) {   // uppercase P — actual constructor
        this.color = color;
    }

    public abstract boolean isValidMove(
        int startRow, int startCol,
        int endRow,   int endCol,
        Board board
    );
}