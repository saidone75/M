package org.saidone.m;

import org.saidone.m.moves.MoveMaker;
import org.saidone.m.moves.MoveUtils;
import org.saidone.m.moves.generators.MoveGenerator;
import java.util.LinkedList;

/** A small iterative-deepening negamax engine with alpha-beta pruning. */
public class Searcher {
    private static final int MATE = 100000;
    private final int depth;
    private final long timeMillis;

    public Searcher() { this(3, 2000); }

    public Searcher(int depth, long timeMillis) {
        if (depth < 1 || depth > 10 || timeMillis < 1 || timeMillis > 3600000)
            throw new IllegalArgumentException("Depth must be 1..10 and time 1..3600000 ms");
        this.depth = depth;
        this.timeMillis = timeMillis;
    }

    /** Returns a legal move, or null at mate/stalemate; never modifies board. */
    public byte[] search(byte[] board) {
        LinkedList<byte[]> moves = MoveUtils.capturesFirst(MoveGenerator.genUserMoves(board));
        if (moves.isEmpty()) return null;
        byte[] best = moves.getFirst();
        long deadline = System.nanoTime() + timeMillis * 1000000L;
        for (int iteration = 1; iteration <= depth; iteration++) {
            byte[] candidate = best;
            int alpha = -MATE;
            try {
                for (byte[] move : moves) {
                    int score = -score(MoveMaker.makeMove(board.clone(), move),
                            iteration - 1, -MATE, -alpha, 1, deadline);
                    if (score > alpha) { alpha = score; candidate = move; }
                }
                best = candidate;
                moves.remove(best);
                moves.addFirst(best);
            } catch (SearchTimeout timeout) {
                break; // Keep the result of the last completed iteration.
            }
        }
        return best.clone();
    }

    private int score(byte[] board, int remaining, int alpha, int beta, int ply, long deadline) {
        if (System.nanoTime() - deadline >= 0 || Thread.currentThread().isInterrupted())
            throw new SearchTimeout();
        LinkedList<byte[]> moves = MoveGenerator.genUserMoves(board);
        if (moves.isEmpty()) return BoardUtils.isKingInCheck(board) ? -MATE + ply : 0;
        if (remaining == 0) return (board[120] == 1 ? 1 : -1) * Evaluator.evaluate(board);
        for (byte[] move : MoveUtils.capturesFirst(moves)) {
            int value = -score(MoveMaker.makeMove(board.clone(), move), remaining - 1,
                    -beta, -alpha, ply + 1, deadline);
            if (value >= beta) return value;
            alpha = Math.max(alpha, value);
        }
        return alpha;
    }

    private static class SearchTimeout extends RuntimeException {
        SearchTimeout() { super(null, null, false, false); }
    }
}
