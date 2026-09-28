package org.saidone.m;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.xml.DOMConfigurator;
import org.saidone.m.moves.generators.MoveGenerator;
import org.saidone.m.moves.MoveMaker;
import org.saidone.m.moves.MoveUtils;
import org.saidone.m.moves.UserMoveParser;
import org.saidone.utils.KProperties;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;

@Slf4j
public class M {
    public static void main(String[] args) {
        DOMConfigurator.configure("etc/log4j.xml");
        log.info("Application started");
        boolean twoPlayers = args.length > 0 && "--two-players".equals(args[0]);
        boolean humanWhite = args.length == 0 || !"--black".equals(args[0]);
        if (args.length > 1 || (args.length == 1 && !twoPlayers && humanWhite)) {
            System.out.println("Usage: [--black | --two-players]");
            return;
        }
        long timeMillis = 2000;
        try {
            long seconds = Long.parseLong(KProperties.INSTANCE.getProperty("timeForMove"));
            if (seconds > 0 && seconds <= 3600) timeMillis = seconds * 1000;
        } catch (NumberFormatException ignored) {
            log.warn("Invalid timeForMove; using 2 seconds");
        }
        Searcher searcher = new Searcher(3, timeMillis);
        play(BoardUtils.newBoard(), new Scanner(System.in), searcher, humanWhite, twoPlayers);
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
