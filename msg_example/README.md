# Example msg library applications
## CommandlineExample
Provide some information about a .msg file from the command line.

Use (assuming the jar files for msg_example and msg are in the classpath):

        java io.github.jmcleodfoss.msg_example.CommandlineExample [-s] msg-file1 [msg-file2 ...]

Option:
        -s: save all attachments found

## SwingExample
A Java Swing application to show basic information from a msg file.

Use (assuming the jar files for msg_example and msg are in the classpath):

        java io.github.jmcleodfoss.msg_example.SwingExample msg-file

The application permits saving attachments (if any), and opening a new file.

### Version 1.0.0
*   [View Javadoc](https://javadoc.io/doc/io.github.jmcleodfoss/msg_example/1.0.0/io.github.jmcleodfoss.msg_example/module-summary.html)
*   [Download from Sonatype OSS Maven Repository](https://repo1.maven.org/maven2/io/github/jmcleodfoss/msg_example/1.0.0/msg_example-1.0.0.jar)

### 1.0.1
Addresses security and, resource leaks, and code quality issues found by static analysis tools (Codacy and Xanitizer, primarily)
*   [View Javadoc](https://javadoc.io/doc/io.github.jmcleodfoss/msg_example/1.0.1/io.github.jmcleodfoss.msg_example/module-summary.html)
*   [Download from Sonatype OSS Maven Repository](https://repo1.maven.org/maven2/io/github/jmcleodfoss/msg_example/1.0.1/msg_example-1.0.1.jar)
