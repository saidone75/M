# Mostly Harmless (formerly "M - The monster of Màsera")

A Java chess engine running as a Spring Boot 4.1.1 console application.
Requires JDK 17 or later and Maven 3.6.3 or later.

## Build

```sh
mvn clean verify
```

The executable Spring Boot JAR, including runtime dependencies, is generated at
`target/m-1.0-SNAPSHOT.jar`.

## XBoard / WinBoard

Run the CECP adapter explicitly:

```sh
java -jar target/m-1.0-SNAPSHOT.jar --xboard
```

Launch it through XBoard from the project directory:

```sh
xboard -fcp "java -jar target/m-1.0-SNAPSHOT.jar --xboard" -fUCI false
```

The adapter follows the [Chess Engine Communication Protocol](https://www.gnu.org/software/xboard/engine-intf.html).
It negotiates protocol v2 with `protover 2` and supports normal chess:

- `new`, `force`, `go`, `playother`, `result`, `quit`.
- `usermove e2e4` and bare coordinate moves, including promotions such as `a7a8n`.
- `setboard` with six-field FEN, including castling and en passant state.
- `undo` (one ply), `remove` (two plies), `ping` / `pong`.
- `sd` (depth 1..10), `st` (seconds per move), `level`, `time` (centiseconds).
- `?` to return the current best move immediately. `force`, `new`, `setboard`,
  `result`, and `quit` cancel the current search without emitting a stale move.

Search runs in a separate worker so commands remain responsive. Protocol output
is flushed to stdout; Spring logging goes to stderr and the banner is disabled.
`ping` received during search is answered after the engine move, or after cancellation.
EOF or `quit` shuts down the worker and closes the Spring context.
Unsupported commands produce a CECP `Error` response; illegal moves leave the
position unchanged. Invalid FEN leaves the board unchanged and enters force mode.

Pondering, analysis mode, SAN, and chess variants are not implemented.
`hard`, `easy`, `post`, and `nopost` are accepted without enabling pondering or
thinking output. The engine reports checkmate and stalemate; it does not yet
adjudicate repetition, the 50-move rule, or insufficient material.

## Interactive play

```sh
java -jar target/m-1.0-SNAPSHOT.jar
java -jar target/m-1.0-SNAPSHOT.jar --black
java -jar target/m-1.0-SNAPSHOT.jar --two-players
```

By default, you play White against the computer. Enter moves such as `e2-e4`,
`moves` to list legal moves, or `quit` to exit. Promotion requires a separate
choice (`Q`, `R`, `B`, `N`). Castle by moving the king (`e1-g1` or `e1-c1`).

Alternatively:

```sh
mvn spring-boot:run -Dspring-boot.run.arguments="--xboard"
```

In IntelliJ IDEA, import `pom.xml` as a Maven project and run `org.saidone.m.M`.
Pass `--xboard` as a program argument when connecting an external chess GUI.

## Configuration

Defaults are in `src/main/resources/application.properties`. Override them using
Spring Boot command-line properties or environment variables:

```sh
java -jar target/m-1.0-SNAPSHOT.jar --xboard --m.search.depth=4 --m.search.time-millis=1500
```

`M_SEARCH_DEPTH` and `M_SEARCH_TIME_MILLIS` provide the equivalent environment
settings. Valid depth is 1..10 and the time limit is 1..3600000 milliseconds.
The legacy `etc/properties.xml` and `etc/log4j.xml` are no longer loaded.
Search uses iterative deepening with alpha-beta pruning and may finish before
its time limit. XBoard clock allocation is basic: `level` estimates a per-move
budget, while `time` limits it using approximately 1/30 of the remaining clock.

## Validation

`mvn verify` runs tests for FEN parsing, handshake, legal engine replies,
promotions, castling, en passant, mate, undo/remove, invalid input, ping ordering,
move-now, and search cancellation.
