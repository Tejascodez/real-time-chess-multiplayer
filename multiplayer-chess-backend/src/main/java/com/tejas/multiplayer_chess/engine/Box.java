package com.tejas.multiplayer_chess.engine;


import lombok.Data;
import lombok.AllArgsConstructor;
import com.tejas.multiplayer_chess.engine.pieces.Piece;
@Data
@AllArgsConstructor
public class Box {

    private int row;
    private int col;
    private Piece piece;
}