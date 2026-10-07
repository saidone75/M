# Mostly Harmless (formerly "M - The monster of Màsera")

A standalone Java chess engine with no runtime dependencies.
Requires JDK 8 or later and Maven.

## Build

```sh
mvn clean verify
```

The executable JAR is generated at
`target/m-1.0-SNAPSHOT.jar`.

## XBoard / WinBoard

The CECP adapter is the default mode:

```sh
java -jar target/m-1.0-SNAPSHOT.jar
```

Launch it through XBoard from the project directory:

The adapter follows the [Chess Engine Communication Protocol](https://www.gnu.org/software/xboard/engine-intf.html).

The engine reports checkmate and stalemate; it does not yet
adjudicate repetition, the 50-move rule, or insufficient material.

## Interactive play

```sh
java -jar target/m-1.0-SNAPSHOT.jar --interactive
java -jar target/m-1.0-SNAPSHOT.jar --black
java -jar target/m-1.0-SNAPSHOT.jar --two-players
```

With `--interactive`, you play White against the computer. `--black` and
`--two-players` also select interactive play. Enter moves such as `e2-e4`,
`moves` to list legal moves, or `quit` to exit. Promotion requires a separate
choice (`Q`, `R`, `B`, `N`). Castle by moving the king (`e1-g1` or `e1-c1`).

## Configuration

Search parameters are read from `etc/properties.xml`, relative to  the working
directory. Run the engine from the project directory, or provide an
`etc/properties.xml` under your chosen working directory.

## Validation

`mvn clean package` compiles the engine and packages the executable JAR.
