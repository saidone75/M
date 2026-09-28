package org.saidone.m;

public class Evaluator {
    private static final int[] VALUES = {0, 100, 320, 0, 0, 330, 500, 900};

    /** Positive scores favour White; values are in centipawns. */
    public static int evaluate(byte[] board) {
        int score = 0;
        for (int i = 0; i < 120; i++) {
            if ((i & 0x88) != 0 || board[i] == 0) continue;
            int piece = board[i] & 7;
            boolean white = (board[i] & 8) == 0;
            int bonus = 0;
            if (piece == Pieces.WP) bonus = (white ? Pieces.whitePawnBonus : Pieces.blackPawnBonus)[i];
            if (piece == Pieces.WN) bonus = (white ? Pieces.whiteKnightBonus : Pieces.blackKnightBonus)[i];
            score += (white ? 1 : -1) * (VALUES[piece] + bonus);
        }
        return score;
    }
}
