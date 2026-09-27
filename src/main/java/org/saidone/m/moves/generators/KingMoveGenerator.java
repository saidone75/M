package org.saidone.m.moves.generators;

import org.saidone.m.BoardUtils;
import org.saidone.m.moves.MoveUtils;

import java.util.LinkedList;

public class KingMoveGenerator {

    public static final byte[] DIRECTIONS = {16, -16, 1, -1, 15, -15, 17, -17};

    public static LinkedList<byte[]> genMoves(byte i, byte[] board) {
        LinkedList<byte[]> moves = new LinkedList<>();

        for (byte j : DIRECTIONS) {
            byte k = (byte)(i + j);
            if ((k & 0x88) == 0) {
                if (board[120] != 0) {
                    if (board[k] == 0) {
                        if (!BoardUtils.isAttackedByBlack(k, board))
                            moves.add(new byte[] {i, k, 0});
                    } else if ((board[k] & 0x08) == 8) {
                        if (!BoardUtils.isAttackedByBlack(k, board))
                            moves.add(new byte[] {i, k, MoveUtils.CAPTURE});
                    }
                } else {
                    if (board[k] == 0) {
                        if (!BoardUtils.isAttackedByWhite(k, board))
                            moves.add(new byte[] {i, k, 0});
                    } else if (board[k] != 0 && (board[k] & 0x08) == 0) {
                        if (!BoardUtils.isAttackedByWhite(k, board))
                            moves.add(new byte[] {i, k, MoveUtils.CAPTURE});
                    }
                }
            }
        }
        // castling
        if (board[120] != 0) {
            // king side
            if (board[121] != 0 && board[5] == 0 && board[6] == 0) {
                if (!BoardUtils.isAttackedByBlack(4, board) &&
                        !BoardUtils.isAttackedByBlack(5, board) &&
                        !BoardUtils.isAttackedByBlack(6, board)) {
                    moves.add(new byte[] {i, 6, MoveUtils.SHORT_CASTLE});
                }
            }
            // queen side
            if (board[122] != 0 && board[1] == 0 && board[2] == 0 && board[3] == 0) {
                if (!BoardUtils.isAttackedByBlack(2, board) &&
                        !BoardUtils.isAttackedByBlack(3, board) &&
                        !BoardUtils.isAttackedByBlack(4, board)) {
                    moves.add(new byte[] {i, 2, MoveUtils.LONG_CASTLE});
                }
            }
        }
        if (board[120] == 0) {
            // king side
            if (board[123] != 0 && board[117] == 0 && board[118] == 0) {
                if (!BoardUtils.isAttackedByWhite(116, board) &&
                        !BoardUtils.isAttackedByWhite(117, board) &&
                        !BoardUtils.isAttackedByWhite(118, board)) {
                    moves.add(new byte[] {i, 118, MoveUtils.SHORT_CASTLE});
                }
            }
            // queen side
            if (board[124] != 0 && board[113] == 0 && board[114] == 0 && board[115] == 0) {
                if (!BoardUtils.isAttackedByWhite(114, board) &&
                        !BoardUtils.isAttackedByWhite(115, board) &&
                        !BoardUtils.isAttackedByWhite(116, board)) {
                    moves.add(new byte[] {i, 114, MoveUtils.LONG_CASTLE});
                }
            }
        }

        return moves;
    }

}