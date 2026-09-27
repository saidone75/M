package org.saidone.m;

import org.apache.log4j.xml.DOMConfigurator;
import org.saidone.m.moves.generators.MoveGenerator;
import org.saidone.m.moves.MoveMaker;
import org.saidone.m.moves.MoveUtils;
import org.saidone.m.moves.UserMoveParser;
import org.saidone.utils.KProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

public class M {

    private static Logger logger = LoggerFactory.getLogger(M.class);

    public static void main(String[] args) {

        DOMConfigurator.configure("etc/log4j.xml");
        logger.info("Application started on --> {}", new Date());

        // properties
        String timeForMove = KProperties.INSTANCE.getProperty("timeForMove");

        byte[] board = BoardUtils.newBoard();

        BoardUtils.printBoard(board);
        LinkedList<byte[]> allowedMoves = MoveGenerator.genUserMoves(board);
        for (byte[] allowedMove : allowedMoves) {
            System.out.println(BoardUtils.indexToString(allowedMove[0]) + "-" + BoardUtils.indexToString(allowedMove[1]) + " " + (allowedMove[2] & MoveUtils.CAPTURE));
        }

        Scanner reader = new Scanner(System.in);
        byte[] move;
        while (true) {
            System.out.println(((board[120] == 0) ? "Black" : "White") + " player, enter your move please: ");
            String line = reader.nextLine();
            move = UserMoveParser.parseInput(line);
            if (move != null && MoveUtils.getMove(move, allowedMoves) != null) {
                // TODO make move
                // TODO if a pawn is promoting, ask first
                if (BoardUtils.isPromoting(move, board)) {
                    byte promotionFlag;
                    do {
                        System.out.println("Promote to (Q R B or N): ");
                        Scanner promotionScanner = new Scanner(System.in);
                        String p = promotionScanner.nextLine();
                        promotionFlag = UserMoveParser.parsePromotionInput(p);
                    } while (promotionFlag == 0);
                    byte[] promotionMove = MoveUtils.getMove(move, allowedMoves);
                    promotionMove = new byte[]{promotionMove[0], promotionMove[1], promotionFlag};
                    board = MoveMaker.makeMove(board, promotionMove);
                } else {
                    board = MoveMaker.makeMove(board, MoveUtils.getMove(move, allowedMoves));
                }
            } else {
                System.out.println("Invalid move!!!");
            }
            BoardUtils.printBoard(board);
            allowedMoves = MoveGenerator.genUserMoves(board);
            if (allowedMoves.size() == 0) {
                System.out.printf("Checkmate, game over!");
                System.exit(0);
            }
            for (byte[] allowedMove : allowedMoves) {
                System.out.println(BoardUtils.indexToString(allowedMove[0]) + "-" + BoardUtils.indexToString(allowedMove[1]) + " " + (allowedMove[2] & MoveUtils.CAPTURE));
            }

        }

    }

}