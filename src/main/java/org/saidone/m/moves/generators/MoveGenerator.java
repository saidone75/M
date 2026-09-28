package org.saidone.m.moves.generators;

import org.saidone.m.BoardUtils;
import org.saidone.m.Pieces;

import java.util.LinkedList;

public class MoveGenerator {

    // like genMoves but remove moves that leave own king in check
    public static LinkedList<byte[]> genUserMoves(byte[] board) {
        LinkedList<byte[]> userMoves = new LinkedList<>();
        for (byte[] move : MoveGenerator.genMoves(board)) {
            if (board[move[1]] != Pieces.WK && board[move[1]] != Pieces.BK
                    && !BoardUtils.isKingInCheck(move, board)) {
                userMoves.add(move);
            }
        }
        return userMoves;
    }

    public static LinkedList<byte[]> genMoves(byte[] board) {
        LinkedList<byte[]> moves = new LinkedList<>();
        for (byte i = 0; i < 120; i++) {
            if ((i & 0x88) != 0)
                i += 8;
            if (board[120] == 1) {
                // white pieces
                switch (board[i]) {
                    case Pieces.WP:
                        moves.addAll(WhitePawnMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.WR:
                        moves.addAll(RookMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.WN:
                        moves.addAll(KnightMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.WB:
                        moves.addAll(BishopMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.WQ:
                        moves.addAll(QueenMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.WK:
                        moves.addAll(KingMoveGenerator.genMoves(i, board));
                        break;
                }

            } else {
                // black pieces
                switch (board[i]) {
                    case Pieces.BP:
                        moves.addAll(BlackPawnMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.BR:
                        moves.addAll(RookMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.BN:
                        moves.addAll(KnightMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.BB:
                        moves.addAll(BishopMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.BQ:
                        moves.addAll(QueenMoveGenerator.genMoves(i, board));
                        break;
                    case Pieces.BK:
                        moves.addAll(KingMoveGenerator.genMoves(i, board));
                        break;
                }
            }
        }
        return moves;
    }

}