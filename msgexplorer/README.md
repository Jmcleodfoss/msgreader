# msgexplorer
The msgexplorer application allows you to view a Microsoft Outlook .msg file at any level of detail:
* The file header
* The raw sectors
* The doubly-indirect file allocation table (DIFAT)
* The File Allocation Table (FAT)
* The chains of mini sectors in the mini stream
* The directory (which contains all the fields in the .msg file)
  * The entry metadata
  * The entry contents in bytes
  * The entry contents in text, if the stored data for that entry is a string

This is a JavaFX application written to use Java 11. The generic JavaFX code (as well as the msg library and the voluminouspaginationskin library) are
included in the jar file, but the platform-specific JavaFX libraries need to be installed and in the classpath for it to work.

The published Javadoc covers only public classes, functions, and members, of which there are very few. More interesting Javadoc can be generated via
'''
mvn -P doc-dev javadoc:javadoc
'''
This will put the Javadoc for all (private, package-private, protected, and public) entities in the directory target/site/javadoc-dev.

## Running the software
Caveat: This is a JavaFX application, which complicates things. In time, I will release jar files for each major platform (Windows, Mac, Linux) which include the relevant JFX modules, but for this first release, I have not done so, which makes running the software complicated.
1 Install the required parts of JavaFX, by one of the following mechanisms:
  * Install the [JavaFX SDK](https://gluonhq.com/products/javafx/)
  * Download the JavaFX jars for your platform from Maven Central:
    * [javafx-base-14-win.jar / javafx-base-mac.jar / javafx-base-14-linux.jar](https://mvnrepository.com/artifact/org.openjfx/javafx-base)
    * [javafx-controls-14-win.jar / javafx-controls-14-mac.jar / javafx-controls-14-linux.jar](https://repo1.maven.org/maven2/org/openjfx/javafx-controls/14/)
    * [javafx-graphics-14-win.jar / javafx-graphics-14-mac.jar / javafx-graphics-14-linux.jar](https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/14/)
2 The location of the javafx jar files is the "<module-path>" in the command below
3 Add msgexplorer to the class path (the msgexplorer jar file already includes the non-JavaFX dependencies)
4 Run via
    java --module-path <module-path> --add-modules javafx.controls io.github.jmcleodfoss.msgexplorer.MsgExplorer [msg-file]

## Version 1.0.0
* View the Javadoc (link TBD)
* [Download from Sonatype OSS Maven Repository](https://repo1.maven.org/maven2/io/github/jmcleodfoss/msgexplorer/1.0.0/msgexplorer-1.0.0.jar)
