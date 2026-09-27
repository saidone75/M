package org.saidone.m.moves.generators;

import java.util.LinkedList;

public class BishopMoveGenerator {

    public static final byte[] DIRECTIONS = {15, -15, 17, -17};

    public static LinkedList<byte[]> genMoves(byte i, byte[] board) {
        return SlidingPieceMoveGenerator.genMoves(i, board, DIRECTIONS);
    }

}
