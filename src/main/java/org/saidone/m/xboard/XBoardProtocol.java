package org.saidone.m.xboard;

import org.saidone.m.BoardUtils;
import org.saidone.m.Searcher;
import org.saidone.m.moves.MoveMaker;
import org.saidone.m.moves.MoveUtils;
import org.saidone.m.moves.UserMoveParser;
import org.saidone.m.moves.generators.MoveGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/** CECP v2 adapter. All position changes and protocol output share one monitor. */
public final class XBoardProtocol implements AutoCloseable {
    private final PrintWriter output;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Deque<byte[]> history = new ArrayDeque<>();
    private final int defaultDepth;
    private final long defaultTime;
    private byte[] board = BoardUtils.newBoard();
    private int engineSide = 0; // -1 = force, 0 = black, 1 = white
    private int depth;
    private long timeMillis;
    private boolean fixedTime;
    private long generation;
    private Future<?> search;
    private AtomicBoolean moveNow = new AtomicBoolean();
    private final Deque<String> pendingPings = new ArrayDeque<>();
    private boolean closed;

    public XBoardProtocol(PrintWriter output, int depth, long timeMillis) {
        new Searcher(depth, timeMillis); // Validate configuration before starting a worker.
        this.output = output;
        this.defaultDepth = this.depth = depth;
        this.defaultTime = this.timeMillis = timeMillis;
    }

    public void run(Reader input) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        String line;
        while ((line = reader.readLine()) != null && accept(line)) { }
    }

    public synchronized boolean accept(String line) {
        if (closed) return false;
        line = line.trim();
        if (line.isEmpty()) return true;
        String[] parts = line.split("\\s+", 2);
        String command = parts[0];
        String argument = parts.length == 2 ? parts[1] : "";
        try {
            switch (command) {
                case "xboard" -> { }
                case "protover" -> emit("feature myname=\"Mostly Harmless\" ping=1 setboard=1 usermove=1 san=0 colors=0 sigint=0 sigterm=0 reuse=1 analyze=0 variants=\"normal\" done=1");
                case "new" -> {
                    cancelSearch(); board = BoardUtils.newBoard(); history.clear();
                    engineSide = 0; depth = defaultDepth; timeMillis = defaultTime; fixedTime = false;
                }
                case "force", "result" -> { cancelSearch(); engineSide = -1; }
                case "go" -> { cancelSearch(); engineSide = board[120]; startSearch(); }
                case "playother" -> { cancelSearch(); engineSide = 1 - board[120]; }
                case "ping" -> {
                    if (search != null) pendingPings.addLast(argument);
                    else emit("pong " + argument);
                }
                case "?" -> moveNow.set(true);
                case "quit" -> { close(); return false; }
                case "setboard" -> {
                    cancelSearch(); engineSide = -1;
                    board = BoardUtils.fromFen(argument); history.clear();
                }
                case "undo", "remove" -> {
                    cancelSearch(); int count = command.equals("remove") ? 2 : 1;
                    if (history.size() < count) throw new IllegalArgumentException("Not enough moves to undo");
                    while (count-- > 0) board = history.pop();
                }
                case "sd" -> {
                    int value = Integer.parseInt(argument);
                    if (value < 1 || value > 10) throw new IllegalArgumentException("Depth must be 1..10");
                    depth = value;
                }
                case "st" -> {
                    double seconds = Double.parseDouble(argument);
                    if (!Double.isFinite(seconds) || seconds <= 0 || seconds > 3600)
                        throw new IllegalArgumentException("Time must be >0 and <=3600 seconds");
                    timeMillis = Math.max(1, (long) (seconds * 1000));
                    fixedTime = true;
                }
                case "time" -> {
                    long centiseconds = Long.parseLong(argument);
                    if (centiseconds < 0) throw new IllegalArgumentException("Negative clock");
                    if (!fixedTime) timeMillis = Math.max(1, Math.min(defaultTime, centiseconds / 30 * 10));
                }
                case "level" -> {
                    String[] control = argument.split("\\s+");
                    if (control.length != 3) throw new IllegalArgumentException("Expected moves base increment");
                    int moves = Integer.parseInt(control[0]);
                    String[] base = control[1].split(":");
                    double seconds = Double.parseDouble(base[0]) * 60;
                    if (base.length == 2) seconds += Double.parseDouble(base[1]);
                    double increment = Double.parseDouble(control[2]);
                    if (moves < 0 || seconds <= 0 || increment < 0 || !Double.isFinite(seconds + increment))
                        throw new IllegalArgumentException("Invalid time control");
                    fixedTime = false;
                    timeMillis = Math.max(1, Math.min(3600000, (long) ((seconds / (moves == 0 ? 30 : moves) + increment) * 1000)));
                }
                case "usermove" -> userMove(argument);
                case "variant" -> { if (!argument.equals("normal")) emit("Error (unsupported variant): " + argument); }
                case "accepted", "rejected", "random", "hard", "easy", "post", "nopost",
                     "otim", "computer", "name", "rating", "ics", "bk", "draw" -> { }
                default -> {
                    if (command.matches("[a-h][1-8][a-h][1-8][qrbn]?")) userMove(command);
                    else emit("Error (unknown command): " + line);
                }
            }
        } catch (IllegalArgumentException exception) {
            emit("Error (" + exception.getMessage() + "): " + line);
        }
        return true;
    }

    private void userMove(String text) {
        if (!text.matches("[a-h][1-8][a-h][1-8][qrbn]?")) {
            emit("Illegal move: " + text); return;
        }
        byte from = BoardUtils.stringToIndex(text.substring(0, 2));
        byte to = BoardUtils.stringToIndex(text.substring(2, 4));
        byte promotion = text.length() == 5
                ? UserMoveParser.parsePromotionInput(text.substring(4).toUpperCase(Locale.ROOT)) : 0;
        for (byte[] move : MoveGenerator.genUserMoves(board)) {
            int flags = move[2] & 60;
            if (move[0] == from && move[1] == to && flags == promotion) {
                cancelSearch(); apply(move);
                if (!reportResult()) startSearch();
                return;
            }
        }
        emit("Illegal move: " + text);
    }

    private void apply(byte[] move) {
        history.push(board.clone());
        board = MoveMaker.makeMove(board, move);
    }

    private boolean reportResult() {
        if (!MoveGenerator.genUserMoves(board).isEmpty()) return false;
        if (BoardUtils.isKingInCheck(board))
            emit(board[120] == 1 ? "0-1 {Black mates}" : "1-0 {White mates}");
        else emit("1/2-1/2 {Stalemate}");
        engineSide = -1;
        return true;
    }

    private void startSearch() {
        if (engineSide != board[120] || closed || reportResult()) return;
        byte[] position = board.clone();
        long token = generation;
        Searcher searcher = new Searcher(depth, timeMillis);
        AtomicBoolean stop = moveNow = new AtomicBoolean();
        search = executor.submit(() -> {
            synchronized (this) {
                if (token != generation || closed) return;
            }
            byte[] move = searcher.search(position, stop::get);
            synchronized (this) {
                if (token != generation || closed) return;
                search = null;
                if (move != null) { apply(move); emit("move " + formatMove(move)); }
                reportResult();
                flushPings();
            }
            Thread.interrupted();
        });
    }

    public static String formatMove(byte[] move) {
        String suffix = "";
        if ((move[2] & MoveUtils.PROMOTE_QUEEN) != 0) suffix = "q";
        else if ((move[2] & MoveUtils.PROMOTE_ROOK) != 0) suffix = "r";
        else if ((move[2] & MoveUtils.PROMOTE_BISHOP) != 0) suffix = "b";
        else if ((move[2] & MoveUtils.PROMOTE_KNIGHT) != 0) suffix = "n";
        return BoardUtils.indexToString(move[0]) + BoardUtils.indexToString(move[1]) + suffix;
    }

    private void cancelSearch() {
        generation++;
        if (search != null) search.cancel(true);
        search = null;
        flushPings();
    }

    private void flushPings() {
        while (!pendingPings.isEmpty()) emit("pong " + pendingPings.removeFirst());
    }

    private void emit(String line) { output.println(line); output.flush(); }

    @Override public synchronized void close() {
        if (closed) return;
        closed = true; cancelSearch(); executor.shutdownNow();
    }
}
