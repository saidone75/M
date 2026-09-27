package org.saidone.m.moves;

import java.util.LinkedList;

public class MoveUtils {

    public static final byte CAPTURE        = (byte)0b00000001;
    public static final byte EN_PASSANT     = (byte)0b00000010;
    public static final byte PROMOTE_QUEEN  = (byte)0b00000100;
    public static final byte PROMOTE_ROOK   = (byte)0b00001000;
    public static final byte PROMOTE_BISHOP = (byte)0b00010000;
    public static final byte PROMOTE_KNIGHT = (byte)0b00100000;
    public static final byte SHORT_CASTLE   = (byte)0b01000000;
    public static final byte LONG_CASTLE    = (byte)0b10000000;

    public static byte[] getMove(byte[] move, LinkedList<byte[]> allowedMoves) {
        for (byte[] allowedMove : allowedMoves) {
            if (allowedMove[0] == move[0] && allowedMove[1] == move[1]) return allowedMove;
        }
        return null;
    }

    private static boolean isCapture(byte[] move) {
        if ((move[2] & MoveUtils.CAPTURE) == MoveUtils.CAPTURE) {
            return true;
        } else return false;
    }

    public static LinkedList<byte[]> capturesFirst(LinkedList<byte[]> moves) {
        LinkedList capturesFirst = new LinkedList<byte[]>();
        for (byte[] move : moves) {
            if (isCapture(move)) {
                capturesFirst.addFirst(move);
            } else {
                capturesFirst.addLast(move);
            }
        }
        return capturesFirst;
    }

}