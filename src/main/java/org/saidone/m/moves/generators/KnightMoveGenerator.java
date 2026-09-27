package org.saidone.m.moves.generators;

import org.saidone.m.moves.MoveUtils;

import java.util.LinkedList;

public class KnightMoveGenerator {

    public static final byte[] KNIGHT_MOVES = {14, 31, 33, 18, -14, -31, -33, -18};

    public static LinkedList<byte[]> genMoves(byte i, byte[] board) {
        LinkedList<byte[]> moves = new LinkedList<>();

        for (byte j : KNIGHT_MOVES) {
            byte k = (byte) (i + j);
            if ((k & 0x88) == 0) {
                if (board[120] != 0) {
                    if (board[k] == 0) {
                        moves.add(new byte[]{i, k, 0});
                    } else if ((board[k] & 0x08) == 8) {
                        moves.add(new byte[]{i, k, MoveUtils.CAPTURE});
                    }
                } else {
                    if (board[k] == 0) {
                        moves.add(new byte[]{i, k, 0});
                    } else if (board[k] != 0 && (board[k] & 0x08) == 0) {
                        moves.add(new byte[]{i, k, MoveUtils.CAPTURE});
                    }
                }
            }
        }

        return moves;
    }

}