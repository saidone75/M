package org.saidone.m;

public class Evaluator {

    private static final int[] VALUES = {0, 100, 320, 0, 0, 330, 500, 900};
    private static final int[] PHASE_WEIGHTS = {0, 0, 1, 0, 0, 1, 2, 4};
    private static final int INITIAL_PHASE = 24;

    /** Positive scores favour White; values are in centipawns. */
    public static int evaluate(byte[] board) {
        int phase = 0;
        for (int i = 0; i < 120; i++) {
            if ((i & 0x88) == 0) phase += PHASE_WEIGHTS[board[i] & 7];
        }
        // Promotions may increase the material beyond the initial position
        phase = Math.min(phase, INITIAL_PHASE);
        int score = 0;
        for (int i = 0; i < 120; i++) {
            if ((i & 0x88) != 0 || board[i] == 0) continue;
            int piece = board[i] & 7;
            boolean white = (board[i] & 8) == 0;
            int bonus;
            switch (piece) {
                case Pieces.WP:
                    bonus = (white ? Pieces.whitePawnBonus : Pieces.blackPawnBonus)[i];
                    break;
                case Pieces.WN:
                    bonus = (white ? Pieces.whiteKnightBonus : Pieces.blackKnightBonus)[i];
                    break;
                case Pieces.WB:
                    bonus = (white ? Pieces.whiteBishopBonus : Pieces.blackBishopBonus)[i];
                    break;
                case Pieces.WR:
                    bonus = (white ? Pieces.whiteRookBonus : Pieces.blackRookBonus)[i];
                    break;
                case Pieces.WQ:
                    bonus = (white ? Pieces.whiteQueenBonus : Pieces.blackQueenBonus)[i];
                    break;
                case Pieces.WK:
                    int middleGame = (white ? Pieces.whiteKingMiddleGameBonus : Pieces.blackKingMiddleGameBonus)[i];
                    int endGame = (white ? Pieces.whiteKingEndGameBonus : Pieces.blackKingEndGameBonus)[i];
                    bonus = (middleGame * phase + endGame * (INITIAL_PHASE - phase)) / INITIAL_PHASE;
                    break;
                default:
                    bonus = 0;
            }
            score += (white ? 1 : -1) * (VALUES[piece] + bonus);
        }
        return score;
    }

}
