# Mostly Harmless (formerly "M - The monster of Màsera")

A Java chess engine. Requires JDK 11 or later and Maven 3.6.3 or later.

## Build

```sh
mvn clean verify
```

The JAR is generated at `target/m-1.0-SNAPSHOT.jar`.

## Run

From the project's root directory:

```sh
mvn exec:java
```

Build the project before running it for the first time. The application reads
`etc/properties.xml` and `etc/log4j.xml` from the current directory.
The logging dependencies are SLF4J and reload4j, which is compatible with the Log4j API
used by the existing source code and configuration.

In IntelliJ IDEA, open `pom.xml` as a Maven project.

## Play

By default, you play White against the computer. Enter moves such as `e2-e4`,
`moves` to list legal moves, or `quit` to exit. Pawn promotion requires
a separate choice (`Q`, `R`, `B`, `N`). To castle, enter the king's move
(`e1-g1` or `e1-c1` for White).

```sh
mvn exec:java -Dexec.args="--black"
mvn exec:java -Dexec.args="--two-players"
```

The search uses alpha-beta pruning to a depth of up to three plies, with a time limit
in seconds set by `timeForMove` in `etc/properties.xml`; it may respond before the limit.
It detects checkmate and stalemate. This basic version does not yet handle
draws by repetition, the 50-move rule, or insufficient material.
