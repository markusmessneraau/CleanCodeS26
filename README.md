Hinweis zur KI-Nutzung: Gemini wurde genutzt, um verschiedene Implementierungsstrategien und Testfälle zu diskutieren. Die finale Implementierung wurde eigenständig von den Studierenden durchgeführt.
# CleanCodeS26 - Web Crawler

Ein Tool zum rekursiven Durchsuchen von Webseiten und zur Erstellung von structured Markdown-Berichten.

## Funktionsweise
Das Programm verarbeitet Webseiten nach folgendem Prinzip:

* **Breitensuche (Ebene für Ebene):** Ausgehend von einer Start-URL wird die Struktur Ebene für Ebene (Breadth-First-Search) abgearbeitet. Erst wenn eine Ebene komplett abgeschlossen ist, geht der Crawler zur nächsten über.
* **Multithreading (Parallele Verarbeitung):** Jede Crawl-Ebene wird von einem festen Pool aus **8 parallelen Threads** verarbeitet. Jede URL einer Ebene wird als eigenständige Aufgabe an einen Worker-Thread (`HtmlParser`) übergeben.
* **Filterung:** Ein Domain-Filter stellt sicher, dass nur Links innerhalb der erlaubten Domains (Whitelist) besucht werden.
* **Extraktion:** Das Tool extrahiert HTML-Überschriften (H1-H6) und wandelt diese in Markdown-Syntax um.
* **Fehlerbehandlung:** Nicht erreichbare Seiten werden abgefangen und im Log dokumentiert, ohne dass Threads abstürzen oder der gesamte Prozess abbricht.

## Voraussetzungen
* Java 21
* Maven
* Python 3 (für lokalen Test-Server)

## Lokale Testumgebung starten
Um den Crawler sicher zu testen, wird die Verwendung der mitgelieferten HTML-Dateien empfohlen.

1. Speichern Sie `index.html`, `seite1.html` und `seite2.html` in einem Ordner namens `website`.
2. Starten Sie im Terminal in diesem Ordner den Server:

   **Linux / macOS:**
   `python3 -m http.server 8000`

   **Windows:**
   `python -m http.server 8000` (oder `py -m http.server 8000`)

3. Die Seite ist nun unter `http://localhost:8000` erreichbar.



### Die Argumente im Detail

Das Programm erwartet die Parameter in einer festen Reihenfolge:

1. **Start-URL** Die vollständige Adresse inklusive Protokoll.  
   *Beispiel:* `http://localhost:8000`


2. **Tiefe** Eine Ganzzahl für die maximalen Link-Ebenen.  
   *Beispiel:* `3` (besucht Startseite + 3 weitere Ebenen)


3. **Whitelist (Variable Liste)** Alle weiteren Wörter nach der Tiefe werden als erlaubte Domains gewertet.
   * **Funktion:** Nur Links mit diesen Domains im Hostnamen werden gecrawlt.
   * **Format:** Beliebig viele Domains, getrennt durch Leerzeichen.
   * **Beispiel:** `localhost google.com aau.at`

### Lokaler Test-Server:

```bash
mvn -f web-crawler/pom.xml exec:java -Dexec.mainClass="org.example.WebCrawlerApp" -Dexec.args="http://localhost:8000/index.html 3 localhost google.com"

```
### Internet-Seite:

```bash
mvn -f web-crawler/pom.xml exec:java -Dexec.mainClass="org.example.WebCrawlerApp" -Dexec.args="http://quotes.toscrape.com/ 3 quotes.toscrape.com"