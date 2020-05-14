# Example msg library applications
## CommandlineExample
Provide some information about a .msg file form the command line.

Use (assuming the jar files for msg_example and msg are in the classpath):

        java io.github.jmcleodfoss.msg_example.CommandlineExample [-s] msg-file1 [msg-file2 ...]

Option:
        -s: save all attachments found

## SwingExample
A Java Swing application to show basic information from a msg file.

Use (assumng the jar files for msg_example and msg are in the classpath):

        java io.github.jmcleodfoss.msg_example.SwingExample msg-file

The application permits saving attachments (if any), and opening a new file.

### Version 1.0.0
* Download from Maven Central (link TBD)
* Javadoc (link TBD)
