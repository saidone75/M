package org.saidone.m.moves;

import org.saidone.m.Pieces;

public class MoveMaker {

    public static byte[] makeMove(byte[] board, byte[] move) {

        // move piece
        board[move[1]] = board[move[0]];

        // free starting square
        board[move[0]] = 0;

        // if king moves then reset both castling flags
        if (move[0] == 4) {
            board[121] = 0;
            board[122] = 0;
        }
        if (move[0] == 116) {
            board[123] = 0;
            board[124] = 0;
        }

        // Moving or capturing a rook on its starting square revokes castling rights.
        if (move[0] == 0 || move[1] == 0) board[122] = 0;
        if (move[0] == 7 || move[1] == 7) board[121] = 0;
        if (move[0] == 112 || move[1] == 112) board[124] = 0;
        if (move[0] == 119 || move[1] == 119) board[123] = 0;

        // castling
        if ((move[2] & MoveUtils.SHORT_CASTLE) == MoveUtils.SHORT_CASTLE) {
            if (board[120] == 0) {
                board[117] = board[119];
                board[119] = 0;
            } else {
                board[5] = board[7];
                board[7] = 0;
            }
        }
        if ((move[2] & MoveUtils.LONG_CASTLE) == MoveUtils.LONG_CASTLE) {
            if (board[120] == 0) {
                board[115] = board[112];
                board[112] = 0;
            } else {
                board[3] = board[0];
                board[0] = 0;
            }
        }

        // if en passant flag is set on move then add it to board
        if ((move[2] & MoveUtils.EN_PASSANT) == MoveUtils.EN_PASSANT) {
            if ((move[2] & MoveUtils.CAPTURE) == MoveUtils.CAPTURE) {
                if (board[120] == 0) {
                    board[move[1] + 16] = 0;
                } else {
                    board[move[1] - 16] = 0;
                }
                board[125] = 0;
            } else if (board[120] == 0) {
                board[125] = (byte) (move[1] + 16);
            } else {
                board[125] = (byte) (move[1] - 16);
            }
        } else {
            board[125] = 0;
        }

        // promotions
        if ((move[2] & MoveUtils.PROMOTE_BISHOP) == MoveUtils.PROMOTE_BISHOP) {
            if (board[120] == 0) {
                board[move[1]] = Pieces.BB;
            } else {
                board[move[1]] = Pieces.WB;
            }
        }
        if ((move[2] & MoveUtils.PROMOTE_KNIGHT) == MoveUtils.PROMOTE_KNIGHT) {
            if (board[120] == 0) {
                board[move[1]] = Pieces.BN;
            } else {
                board[move[1]] = Pieces.WN;
            }
        }
        if ((move[2] & MoveUtils.PROMOTE_ROOK) == MoveUtils.PROMOTE_ROOK) {
            if (board[120] == 0) {
                board[move[1]] = Pieces.BR;
            } else {
                board[move[1]] = Pieces.WR;
            }
        }
        if ((move[2] & MoveUtils.PROMOTE_QUEEN) == MoveUtils.PROMOTE_QUEEN) {
            if (board[120] == 0) {
                board[move[1]] = Pieces.BQ;
            } else {
                board[move[1]] = Pieces.WQ;
            }
        }

        // switch side
        board[120] = (board[120] == 0) ? (byte) 1 : 0;

        return board;
    }

}
