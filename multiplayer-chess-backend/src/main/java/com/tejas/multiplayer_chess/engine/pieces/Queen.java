package com.tejas.multiplayer_chess.engine.pieces;

import com.tejas.multiplayer_chess.engine.Board;
import com.tejas.multiplayer_chess.util.BoardUtil;
import com.tejas.multiplayer_chess.model.PlayerColor;

public class Queen extends Piece {

    public Queen(PlayerColor color) {

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

        boolean diagonal =
                Math.abs(sr - er) ==
                Math.abs(sc - ec);

        boolean straight =
                sr == er || sc == ec;

        if (!diagonal && !straight) {

            return false;
        }

        return BoardUtil.isPathClear(
                board,
                sr,
                sc,
                er,
                ec
        );
    }
}