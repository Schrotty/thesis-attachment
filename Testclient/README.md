# PriceIRC Redux
Zum kompilieren des Projektes wird ```sbt``` benötigt.
- https://www.scala-sbt.org/download.html (Windows)
- https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html (Linux)

Im Verzeichnis ```Testclient/Sources``` den Befehl ``sbt`` ausführen.
Kompiliert wird mittels ``compile``, ausgeführt mittels ``run`` und eine Executable wird über ``universal:stage`` erzeugt.

## Ausführen
Für die Ausführung wird ein IRC-Server benötigt. Getestet wurde der Client mit der Referenzlösung des chirc-Projekts. Diese muss zunächst kompiliert und anschließend im ``.chirc`` Ordner plaziert werden.
Der Quellcode ist nicht Teil dieses Paketes!
Über das ``run.sh`` Skript kann der Testclient gestartet werden.

### Verfügbare Tests
Derzeit ist der Client so eingestellt dass alle Tests ausgeführt werden. Dies kann über den Parameter ``-Dprice.tests=`` gesteuert werden. Verfügbare Tests sind:
- ALL
- CHANNEL
- CONNECTION
- MOTD
- PING
- PRIVMSG
- ROBUSTNESS
- UNKNOWN
- WHOIS

### Nach dem Testen
Nachdem alle Tests durchlaufen wurden findet sich im ``logs`` Ordner ein neuer Unterordner mit der Runtime-ID die beim Start des Testclients angezeigt wurde. Die Logs zeigen sowohl die Aktivität des Servers, als auch die des Testclients. Für Demonstrationszwecke existiert ein ``_demo`` Ordner.