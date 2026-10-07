package org.saidone.m.moves;

import org.saidone.m.BoardUtils;

public class UserMoveParser {

    public static byte[] parseInput(String input) {
        if (input.matches("[a-h]{1}[1-8]{1}-[a-h]{1}[1-8]{1}")) {
            String[] plys = input.split("-");
            return new byte[]{BoardUtils.stringToIndex(plys[0]), BoardUtils.stringToIndex(plys[1]), 0};
        } else return null;
    }

    public static byte parsePromotionInput(String input) {
        switch (input) {
            case "Q":
                return MoveUtils.PROMOTE_QUEEN;
            case "N":
                return MoveUtils.PROMOTE_KNIGHT;
            case "R":
                return MoveUtils.PROMOTE_ROOK;
            case "B":
                return MoveUtils.PROMOTE_BISHOP;
            default:
                return 0;
        }
    }

}
