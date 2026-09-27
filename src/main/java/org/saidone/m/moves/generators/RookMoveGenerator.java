package org.saidone.m.moves.generators;

import java.util.LinkedList;

public class RookMoveGenerator {

    public static final byte[] DIRECTIONS = {16, -16, 1, -1};

    public static LinkedList<byte[]> genMoves(byte i, byte[] board) {
        return SlidingPieceMoveGenerator.genMoves(i, board, DIRECTIONS);
    }

}
