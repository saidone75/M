package org.saidone.m.moves.generators;

import org.saidone.m.moves.MoveUtils;

import java.util.LinkedList;

public class WhitePawnMoveGenerator {

    public static LinkedList<byte[]> genMoves(byte i, byte[] board) {
        LinkedList<byte[]> moves = new LinkedList<>();

        if (((i + 16) & 0x88) == 0 && board[i + 16] == 0) {
            // check if we are going on 7th rank
            if ((i >> 4) == 6) {
                moves.add(new byte[]{i, (byte) (i + 16), MoveUtils.PROMOTE_KNIGHT});
                moves.add(new byte[]{i, (byte) (i + 16), MoveUtils.PROMOTE_BISHOP});
                moves.add(new byte[]{i, (byte) (i + 16), MoveUtils.PROMOTE_ROOK});
                moves.add(new byte[]{i, (byte) (i + 16), MoveUtils.PROMOTE_QUEEN});
            } else {
                // add normal move
                moves.add(new byte[]{i, (byte) (i + 16), 0});
            }
        }
        if ((i >> 4) == 1 && board[i + 16] == 0 && board[i + 32] == 0) {
            // advance by 2 and set en passant "flag"
            moves.add(new byte[]{i, (byte) (i + 32), MoveUtils.EN_PASSANT});
        }
        // captures
        if (((i + 15) & 0x88) == 0 && ((board[i + 15] & 0x08) == 8)) {
            if ((i >> 4) == 6) {
                // capture and promote
                moves.add(new byte[]{i, (byte) (i + 15), MoveUtils.CAPTURE | MoveUtils.PROMOTE_KNIGHT});
                moves.add(new byte[]{i, (byte) (i + 15), MoveUtils.CAPTURE | MoveUtils.PROMOTE_BISHOP});
                moves.add(new byte[]{i, (byte) (i + 15), MoveUtils.CAPTURE | MoveUtils.PROMOTE_ROOK});
                moves.add(new byte[]{i, (byte) (i + 15), MoveUtils.CAPTURE | MoveUtils.PROMOTE_QUEEN});
            } else {
                // capture only
                moves.add(new byte[]{i, (byte) (i + 15), MoveUtils.CAPTURE});
            }
        }
        if (((i + 17) & 0x88) == 0 && ((board[i + 17] & 0x08) == 8)) {
            if ((i >> 4) == 6) {
                // capture and promote
                moves.add(new byte[]{i, (byte) (i + 17), MoveUtils.CAPTURE | MoveUtils.PROMOTE_KNIGHT});
                moves.add(new byte[]{i, (byte) (i + 17), MoveUtils.CAPTURE | MoveUtils.PROMOTE_BISHOP});
                moves.add(new byte[]{i, (byte) (i + 17), MoveUtils.CAPTURE | MoveUtils.PROMOTE_ROOK});
                moves.add(new byte[]{i, (byte) (i + 17), MoveUtils.CAPTURE | MoveUtils.PROMOTE_QUEEN});
            } else {
                // capture only
                moves.add(new byte[]{i, (byte) (i + 17), MoveUtils.CAPTURE});
            }
        }
        // en passant capture
        if (board[125] != 0) {
            if (i == board[125] - 15) {
                moves.add(new byte[]{i, board[125], MoveUtils.CAPTURE | MoveUtils.EN_PASSANT});
            }
            if (i == board[125] - 17) {
                moves.add(new byte[]{i, board[125], MoveUtils.CAPTURE | MoveUtils.EN_PASSANT});
            }
        }

        return moves;
    }

}
