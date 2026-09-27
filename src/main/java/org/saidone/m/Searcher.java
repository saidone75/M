package org.saidone.m;

import org.saidone.m.moves.MoveUtils;
import org.saidone.m.moves.generators.MoveGenerator;

import java.util.LinkedList;

public class Searcher {

    public byte[] search (byte[] board) {
        LinkedList<byte[]> moves = MoveGenerator.genMoves(board);
        moves = MoveUtils.capturesFirst(moves);

        if (board[120] == 0) {
            // black turn


        } else {

        }




    }


}
