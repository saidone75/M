package org.saidone.m;

import org.saidone.m.xboard.XBoardProtocol;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

import org.saidone.m.moves.generators.MoveGenerator;
import org.saidone.m.moves.MoveMaker;
import org.saidone.m.moves.MoveUtils;
import org.saidone.m.moves.UserMoveParser;


import org.saidone.utils.KProperties;
import java.io.IOException;

public class M {

    public static void main(String[] args) throws IOException {
        int depth = Integer.parseInt(configuration("searchDepth"));
        long millis = Long.parseLong(configuration("searchTimeMillis"));
        List<String> options = Arrays.asList(args);
        boolean interactive = options.contains("--interactive") || options.contains("--black") || options.contains("--two-players");
        if (options.contains("--xboard") || !interactive) {
            try (XBoardProtocol protocol = new XBoardProtocol(new PrintWriter(System.out, true), depth, millis)) {
                protocol.run(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            }
        } else {
            boolean twoPlayers = options.contains("--two-players");
            boolean humanWhite = !options.contains("--black");
            try (Scanner reader = new Scanner(System.in)) {
                play(BoardUtils.newBoard(), reader, new Searcher(depth, millis), humanWhite, twoPlayers);
            }
        }
    }

    private static String configuration(String key) {
        String value = KProperties.INSTANCE.getProperty(key);
        if (value == null) throw new IllegalArgumentException("Missing configuration property: " + key);
        return value;
    }

    static void play(byte[] board, Scanner reader, Searcher searcher, boolean humanWhite, boolean twoPlayers) {
        System.out.println("Moves: e2-e4. Commands: moves, quit. Uppercase pieces are White.");
        while (true) {
            BoardUtils.printBoard(board);
            System.out.println("  a   b   c   d   e   f   g   h  (rank 8 at top, 1 at bottom)");
            boolean white = board[120] != 0;
            LinkedList<byte[]> allowed = MoveGenerator.genUserMoves(board);
            boolean check = BoardUtils.isKingInCheck(board);
            if (allowed.isEmpty()) {
                System.out.println(check ? "Checkmate. " + (white ? "Black" : "White") + " wins!" : "Stalemate. Draw.");
                return;
            }
            if (check) System.out.println("Check!");
            byte[] selected;
            if (!twoPlayers && white != humanWhite) {
                selected = searcher.search(board);
                System.out.println("Computer: " + formatMove(selected));
            } else {
                selected = readMove(board, reader, allowed);
                if (selected == null) return;
            }
            board = MoveMaker.makeMove(board, selected);
        }
    }

    private static byte[] readMove(byte[] board, Scanner reader, LinkedList<byte[]> allowed) {
        while (true) {
            System.out.println((board[120] != 0 ? "White" : "Black") + ", enter your move:");
            if (!reader.hasNextLine()) return null;
            String line = reader.nextLine().trim().toLowerCase(Locale.ROOT);
            if ("quit".equals(line) || "exit".equals(line)) return null;
            if ("moves".equals(line)) {
                for (byte[] move : allowed) System.out.println(formatMove(move));
                continue;
            }
            byte[] parsed = UserMoveParser.parseInput(line);
            byte[] selected = parsed == null ? null : MoveUtils.getMove(parsed, allowed);
            if (selected == null) { System.out.println("Invalid move. Use e2-e4 or moves."); continue; }
            if (!BoardUtils.isPromoting(selected, board)) return selected;
            while (true) {
                System.out.println("Promote to (Q R B or N), or quit:");
                if (!reader.hasNextLine()) return null;
                String choice = reader.nextLine().trim().toUpperCase(Locale.ROOT);
                if ("QUIT".equals(choice) || "EXIT".equals(choice)) return null;
                byte flag = UserMoveParser.parsePromotionInput(choice);
                if (flag == 0) continue;
                for (byte[] move : allowed) {
                    if (move[0] == selected[0] && move[1] == selected[1] && (move[2] & flag) != 0)
                        return move;
                }
            }
        }
    }

    private static String formatMove(byte[] move) {
        String promotion = "";
        if ((move[2] & MoveUtils.PROMOTE_QUEEN) != 0) promotion = "=Q";
        if ((move[2] & MoveUtils.PROMOTE_ROOK) != 0) promotion = "=R";
        if ((move[2] & MoveUtils.PROMOTE_BISHOP) != 0) promotion = "=B";
        if ((move[2] & MoveUtils.PROMOTE_KNIGHT) != 0) promotion = "=N";
        return BoardUtils.indexToString(move[0]) + "-" + BoardUtils.indexToString(move[1]) + promotion;
    }

}
