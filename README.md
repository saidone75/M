# M

Motore scacchistico Java. Richiede JDK 11 o successivo e Maven 3.6.3 o successivo.

## Compilazione

```sh
mvn clean verify
```

Il JAR viene generato in `target/m-1.0-SNAPSHOT.jar`.

## Avvio

Dalla directory principale del progetto:

```sh
mvn exec:java
```

Prima del primo avvio eseguire la compilazione. L'applicazione legge
`etc/properties.xml` e `etc/log4j.xml` dalla directory corrente.
Le dipendenze di logging sono SLF4J e reload4j, compatibile con l'API Log4j
utilizzata dai sorgenti e dalla configurazione esistenti.

In IntelliJ IDEA aprire il `pom.xml` come progetto Maven.

## Giocare

Di default giochi con il Bianco contro il computer. Inserisci mosse come `e2-e4`,
`moves` per elencare le mosse legali, `quit` per uscire. La promozione richiede
una scelta separata (`Q`, `R`, `B`, `N`). L'arrocco si indica con la mossa del re
(`e1-g1` oppure `e1-c1` per il Bianco).

```sh
mvn exec:java -Dexec.args="--black"
mvn exec:java -Dexec.args="--two-players"
```

La ricerca usa alpha-beta fino a tre semimosse, con il limite massimo in secondi
`timeForMove` di `etc/properties.xml`; può rispondere prima del limite.
Riconosce scacco matto e stallo. Questa versione di base non gestisce ancora
le patte per ripetizione, regola delle 50 mosse o materiale insufficiente.
