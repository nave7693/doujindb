DoujinDB
===

A program used to manage a digital collection of comic books

Status
---

DoujinDB it's mostly stable and has been tested with MySQL and SQLite.

I'm using it to catalog ~150GB on disk (data: image files) and 4MB on a vanilla MySQL install (metadata: 11K books, 4K artists and 3K circles) without problems.

Build
---

Make sure you have [Ant] installed, then simply run ```ant``` from the root folder ot the project: this will perform a clean build and package a .jar file

Usage
---

Run it from a command promp as ```java -jar doujindb.jar```

*(NOTE: doujindb will not run on a headless system)*

Requirements
---

* [Java] - Java Runtime Environment 1.7+ (Java Development Kit is needed to build)
* [Ant] - Software tool for automating software build processes (needed to build)
* [Cayenne] - Object Relational Mapping Framework
* JDBC drivers (see README in lib folder)

TODO
---

* Documentation

Screenshots
---
<p align="center">
<img src="http://loli10k.github.io/doujindb/screenshots/screenshot-01.png" alt="Screenshot"/>
<img src="http://loli10k.github.io/doujindb/screenshots/screenshot-02.png" alt="Screenshot"/>
<img src="http://loli10k.github.io/doujindb/screenshots/screenshot-03.png" alt="Screenshot"/>
<img src="http://loli10k.github.io/doujindb/screenshots/screenshot-04.png" alt="Screenshot"/>
<img src="http://loli10k.github.io/doujindb/screenshots/screenshot-05.png" alt="Screenshot"/>
<img src="http://loli10k.github.io/doujindb/screenshots/screenshot-06.png" alt="Screenshot"/>
</p>
License
---

EPL – Eclipse Public License

  [java]: http://www.java.com/
  [ant]: http://ant.apache.org/
  [cayenne]: http://cayenne.apache.org/