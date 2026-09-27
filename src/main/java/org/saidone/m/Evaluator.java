package org.saidone.m;

public class Evaluator {

    private static int[] pieceValues = {
            0,  1,  3,  999,  0,  3,  5,  9,
            0, -1, -3, -999, -0, -3, -5, -9
    };

    public static int evaluate(byte[] board) {
        int score = 0;
        for (int i = 0; i < 120; i++) {
            score += Evaluator.pieceValues[board[i]];
        }
        return score;
    }

}
