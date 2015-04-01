DoujinDB
===

Cross-platform comic books manager

Manage all your digitalized comics through a desktop-like user interface.

DoujinDB also provides different plugins:

  * ImageSearch : search the database for matching cover images
  * DataImport : fetch comic books metadata using the doujinshidb API system (other systems may be added later on)

DoujinDB makes use of [Cayenne] ORM framework for its backend. 

Status
---

DoujinDB it's mostly stable: it has been tested on ~450GB of data on disk (image files) and ~20MB for the index (MySQL) without problems.

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
