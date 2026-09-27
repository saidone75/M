package org.saidone.m.moves.generators;

import org.saidone.m.moves.MoveUtils;

import java.util.LinkedList;

public class SlidingPieceMoveGenerator {

    public static LinkedList<byte[]> genMoves(byte i, byte[] board, byte[] d) {
        LinkedList<byte[]> moves = new LinkedList<>();

        for (byte j : d) {
            byte k = (byte) (i + j);
            if (board[120] != 0) {
                while ((k & 0x88) == 0 && board[k] == 0) {
                    moves.add(new byte[]{i, k, 0});
                    k += j;
                }
                if ((k & 0x88) == 0 && (board[k] & 0x08) == 8) {
                    moves.add(new byte[]{i, k, MoveUtils.CAPTURE});
                }
            } else {
                while ((k & 0x88) == 0 && board[k] == 0) {
                    moves.add(new byte[]{i, k, 0});
                    k += j;
                }
                if ((k & 0x88) == 0 && (board[k] != 0 && (board[k] & 0x08) == 0)) {
                    moves.add(new byte[]{i, k, MoveUtils.CAPTURE});
                }
            }
        }

        return moves;
    }

}