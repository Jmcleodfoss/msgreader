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

## Version 1.0
* Download the jar (link TBD)
* View the Javadoc (link TBD)
