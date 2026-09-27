package org.saidone.m;

import org.saidone.m.moves.generators.BishopMoveGenerator;
import org.saidone.m.moves.generators.KnightMoveGenerator;
import org.saidone.m.moves.generators.KingMoveGenerator;
import org.saidone.m.moves.MoveMaker;
import org.saidone.m.moves.generators.RookMoveGenerator;

import java.util.Stack;

public class BoardUtils {

    /**
     * Returns a new board populated with starting position.
     *
     * @return a new board populated with starting position
     */
    public static byte[] newBoard() {
        byte[] board = new byte[126];
        // white pieces
        board[0]   = Pieces.WR;
        board[1]   = Pieces.WN;
        board[2]   = Pieces.WB;
        board[3]   = Pieces.WQ;
        board[4]   = Pieces.WK;
        board[5]   = Pieces.WB;
        board[6]   = Pieces.WN;
        board[7]   = Pieces.WR;
        board[16]  = Pieces.WP;
        board[17]  = Pieces.WP;
        board[18]  = Pieces.WP;
        board[19]  = Pieces.WP;
        board[20]  = Pieces.WP;
        board[21]  = Pieces.WP;
        board[22]  = Pieces.WP;
        board[23]  = Pieces.WP;
        // black pieces
        board[112] = Pieces.BR;
        board[113] = Pieces.BN;
        board[114] = Pieces.BB;
        board[115] = Pieces.BQ;
        board[116] = Pieces.BK;
        board[117] = Pieces.BB;
        board[118] = Pieces.BN;
        board[119] = Pieces.BR;
        board[96]  = Pieces.BP;
        board[97]  = Pieces.BP;
        board[98]  = Pieces.BP;
        board[99]  = Pieces.BP;
        board[100] = Pieces.BP;
        board[101] = Pieces.BP;
        board[102] = Pieces.BP;
        board[103] = Pieces.BP;
        // additional data
        // board[120] --> white to move (0 --> false, otherwise true)
        // board[121] --> white can castle on the king side
        // board[122] --> white can castle on the queen side
        // board[123] --> black can castle on the king side
        // board[124] --> black can castle on the queen side
        // board[125] --> en passant index (0 --> none)
        board[120] = 1;
        board[121] = 1;
        board[122] = 1;
        board[123] = 1;
        board[124] = 1;
        board[125] = 0;
        return board;
    }

    private static String pieceToString(byte piece, String out) {
        switch (piece % 8) {
            case 1:
                out = ((piece & 0x08) == 0) ? "P" : "p";
                break;
            case 2:
                out = ((piece & 0x08) == 0) ? "N" : "n";
                break;
            case 3:
                out = ((piece & 0x08) == 0) ? "K" : "k";
                break;
            case 5:
                out = ((piece & 0x08) == 0) ? "B" : "b";
                break;
            case 6:
                out = ((piece & 0x08) == 0) ? "R" : "r";
                break;
            case 7:
                out = ((piece & 0x08) == 0) ? "Q" : "q";
        }
        return out;
    }

    /**
     * Returns a string coordinate from board array index.
     * The format is [a-h]{1}[1-8]{1} (e.g. e2).
     *
     * @param i board array index
     * @return string coordinate
     */
    public static String indexToString(int i) {
        return "" + (char) ((i & 7) + 97) + ((i >> 4) + 1);
    }

    // return a board array index
    public static byte stringToIndex(String s) {
        byte[] b = s.getBytes();
        return (byte) (b[0] - 97 + 16 * (b[1] - 49));
    }

    public static void printBoard(byte[] board) {
        printBoard(board, false);
    }

    public static void printBoard(byte[] board, boolean GNUStyle) {
        if (GNUStyle) System.out.println(toStringGNU(board));
        else System.out.println(toStringBig(board));
    }

    private static String toStringBig(byte[] board) {
        Stack<String> out = new Stack<String>();
        int i = 0;
        String l = "";
        while (i < 120) {
            out.push("+---+---+---+---+---+---+---+---+\n");
            for (int j = 0; j < 8; j++) {
                l += "| " + pieceToString(board[i + j], " ") + " ";
            }
            i += 16;
            out.push(l + "|\n");
            l = "";
        }
        out.push("+---+---+---+---+---+---+---+---+\n");
        String o = "";
        while (!out.isEmpty()) {
            o += out.pop();
        }
        return o;
    }

    private static String toStringGNU(byte[] board) {
        Stack<String> out = new Stack<String>();
        int i = 0;
        String l = "";
        while (i < 120) {
            for (int j = 0; j < 8; j++) {
                l += pieceToString(board[i + j], "" + (char) 0x00B7);
            }
            i += 16;
            out.push(l + "\n");
            l = "";
        }
        String o = "";
        while (!out.isEmpty()) {
            o += out.pop();
        }
        return o;
    }

    public static boolean isAttackedByBlack(int i, byte[] board) {
        for (int direction : KingMoveGenerator.DIRECTIONS) {
            int square = i + direction;
            if ((square & 0x88) == 0 && board[square] == Pieces.BK)
                return true;
        }

        // check if square is attacked by a knight
        for (int j : KnightMoveGenerator.KNIGHT_MOVES) {
            int k = i + j;
            if ((k & 0x88) == 0 && board[k] == Pieces.BN)
                return true;
        }

        // check if square is attacked by a rook or a queen
        for (int j : RookMoveGenerator.DIRECTIONS) {
            int k = i + j;
            while ((k & 0x88) == 0 && board[k] == 0) {
                k += j;
            }
            if ((k & 0x88) == 0 && (board[k] == Pieces.BQ || board[k] == Pieces.BR))
                return true;
        }

        // check if square is attacked by a bishop or a queen
        for (int j : BishopMoveGenerator.DIRECTIONS) {
            int k = i + j;
            while ((k & 0x88) == 0 && board[k] == 0) {
                k += j;
            }
            if ((k & 0x88) == 0 && (board[k] == Pieces.BQ || board[k] == Pieces.BB))
                return true;
        }
        // check if square is attacked by a pawn
        if (((i + 15) & 0x88) == 0 && board[i + 15] == Pieces.BP)
            return true;
        if (((i + 17) & 0x88) == 0 && board[i + 17] == Pieces.BP)
            return true;
        return false;
    }

    public static boolean isAttackedByWhite(int i, byte[] board) {
        for (int direction : KingMoveGenerator.DIRECTIONS) {
            int square = i + direction;
            if ((square & 0x88) == 0 && board[square] == Pieces.WK)
                return true;
        }

        // check if square is attacked by a knight
        for (int j : KnightMoveGenerator.KNIGHT_MOVES) {
            int k = i + j;
            if ((k & 0x88) == 0 && board[k] == Pieces.WN)
                return true;
        }

        // check if square is attacked by a rook or a queen
        for (int j : RookMoveGenerator.DIRECTIONS) {
            int k = i + j;
            while ((k & 0x88) == 0 && board[k] == 0) {
                k += j;
            }
            if ((k & 0x88) == 0 && (board[k] == Pieces.WQ || board[k] == Pieces.WR))
                return true;
        }

        // check if square is attacked by a bishop or a queen
        for (int j : BishopMoveGenerator.DIRECTIONS) {
            int k = i + j;
            while ((k & 0x88) == 0 && board[k] == 0) {
                k += j;
            }
            if ((k & 0x88) == 0 && (board[k] == Pieces.WQ || board[k] == Pieces.WB))
                return true;
        }
        // check if square is attacked by a pawn
        if (((i - 15) & 0x88) == 0 && board[i - 15] == Pieces.WP)
            return true;
        if (((i - 17) & 0x88) == 0 && board[i - 17] == Pieces.WP)
            return true;
        return false;
    }

    public static boolean isPromoting(byte[] move, byte[] board) {
        if ((board[move[0]] == Pieces.BP && (move[1] >> 4) == 0) ||
                (board[move[0]] == Pieces.WP && (move[1] >> 4) == 7)) return true;
        else return false;
    }

    public static boolean isKingInCheck(byte[] move, byte[] board) {
        board = MoveMaker.makeMove(board.clone(), move);
        int king = (board[120] == 0) ? Pieces.WK : Pieces.BK;
        int i;
        for (i = 0; i < 120; i++) {
            if (board[i] == king) break;
        }
        if (king == Pieces.WK) {
            return isAttackedByBlack(i, board);
        } else {
            return isAttackedByWhite(i, board);
        }
    }

}
