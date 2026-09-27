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
