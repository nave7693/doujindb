Requirements
--
* [Cayenne] - Object Relational Mapping (ORM) framework
* [Ashwood] - ObjectStyle Ashwood Graph Library (required by Cayenne)
* [Velocity] - Apache Velocity Template Engine (required by Cayenne)
* [Commons Collections] - Apache Commons Collections (required by Cayenne)
* [Commons Logging] - Apache Commons Logging (required by Cayenne)
* JDBC drivers - [MySQL], [SQLite], [Derby] ...

Download Cayenne binaries (3.0.2 is the supported version, 3.1+ won't work) which also contain required libraries (Ashwood, Velocity, Commons) in lib/third-party

Jar files should not contain version numbers in the file name or DoujinDB won't load them (you can change this in the Ant buildfile)

Your lib directory should look like this

```
ashwood.jar
cayenne-server.jar
commons-collections.jar
commons-logging.jar
README.md
velocity.jar
```

And should also contain a valid JDBC driver (SQLite is good for starters; it doesn't require installation whatsoever and it's self-contained in a single file)

```
derby.jar
hsqldb.jar
mysql.jar
sqlite.jar
```

  [cayenne]: http://cayenne.apache.org/
  [ashwood]: http://objectstyle.org/ashwood/
  [velocity]: http://jakarta.apache.org/velocity/
  [commons collections]: http://jakarta.apache.org/commons/collections
  [commons logging]: http://jakarta.apache.org/commons/logging/
  [mysql]: http://dev.mysql.com/downloads/connector/j/
  [sqlite]: https://bitbucket.org/xerial/sqlite-jdbc
  [Derby]: http://db.apache.org/derby/

