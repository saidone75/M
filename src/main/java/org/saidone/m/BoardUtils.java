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

    /** Parses the six standard FEN fields into the engine's 0x88 board. */
    public static byte[] fromFen(String fen) {
        String[] fields = fen.trim().split("\\s+");
        if (fields.length != 6) throw new IllegalArgumentException("Expected six FEN fields");
        byte[] board = new byte[126];
        String[] ranks = fields[0].split("/", -1);
        if (ranks.length != 8) throw new IllegalArgumentException("Expected eight ranks");
        int whiteKings = 0, blackKings = 0;
        String symbols = "PNKBRQpnkbrq";
        byte[] pieces = {Pieces.WP, Pieces.WN, Pieces.WK, Pieces.WB, Pieces.WR, Pieces.WQ,
                Pieces.BP, Pieces.BN, Pieces.BK, Pieces.BB, Pieces.BR, Pieces.BQ};
        for (int rank = 0; rank < 8; rank++) {
            int file = 0;
            for (char c : ranks[rank].toCharArray()) {
                if (c >= '1' && c <= '8') file += c - '0';
                else {
                    int piece = symbols.indexOf(c);
                    if (piece < 0 || file >= 8) throw new IllegalArgumentException("Invalid piece or rank");
                    if (c == 'K') whiteKings++;
                    if (c == 'k') blackKings++;
                    if ((c == 'P' || c == 'p') && (rank == 0 || rank == 7))
                        throw new IllegalArgumentException("Pawn on promotion rank");
                    board[(7 - rank) * 16 + file++] = pieces[piece];
                }
                if (file > 8) throw new IllegalArgumentException("Rank too long");
            }
            if (file != 8) throw new IllegalArgumentException("Rank too short");
        }
        if (whiteKings != 1 || blackKings != 1) throw new IllegalArgumentException("Expected one king per side");
        if (!fields[1].matches("[wb]")) throw new IllegalArgumentException("Invalid side");
        board[120] = (byte) (fields[1].equals("w") ? 1 : 0);
        if (!fields[2].matches("-|K?Q?k?q?" ) || fields[2].isEmpty())
            throw new IllegalArgumentException("Invalid castling rights");
        for (int n = 0; n < 4; n++) board[121 + n] = (byte) (fields[2].indexOf("KQkq".charAt(n)) >= 0 ? 1 : 0);
        if (!fields[3].equals("-")) {
            if (!fields[3].matches(board[120] == 1 ? "[a-h]6" : "[a-h]3"))
                throw new IllegalArgumentException("Invalid en passant square");
            board[125] = stringToIndex(fields[3]);
        }
        if (!fields[4].matches("[0-9]+")
                || !fields[5].matches("[0-9]+")
                || Long.parseLong(fields[5]) < 1)
            throw new IllegalArgumentException("Invalid move counters");
        // the side that just moved cannot have left its king in check
        board[120] ^= 1;
        boolean invalid = isKingInCheck(board);
        board[120] ^= 1;
        if (invalid) throw new IllegalArgumentException("Inactive king is in check");
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

    public static boolean isKingInCheck(byte[] board) {
        return isKingInCheck(board, board[120] != 0);
    }

    private static boolean isKingInCheck(byte[] board, boolean white) {
        int king = white ? Pieces.WK : Pieces.BK;
        for (int i = 0; i < 120; i++) {
            if ((i & 0x88) == 0 && board[i] == king)
                return white ? isAttackedByBlack(i, board) : isAttackedByWhite(i, board);
        }
        throw new IllegalArgumentException("Missing king");
    }

    public static boolean isKingInCheck(byte[] move, byte[] board) {
        return isKingInCheck(MoveMaker.makeMove(board.clone(), move), board[120] != 0);
    }

}
