# Introduction
msgreader is a low-level java library for reading Microsoft Outlook .msg files. It is "low-level" because it is provides very few amenities beyond exposing the file contents via java classes, so you need to understand the file formats and details to use it effectively:
*   [MS-CFB: Compound Binary File Format](https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/53989ce4-7b05-4f8d-829b-d08d6148375b)
*   [MS-OXMSG: Outlook Item (.msg) File Format](https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxmsg/b046868c-9fbf-41ae-9ffb-8de2bd4eec82)

Additional useful material can be found in:
*   [MS-OXPROPS: Exchange Server Protocols Master Property List](https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxprops/f6ab1613-aefe-447d-a49c-18217230b148)
*   [MS-OXCDATA: Data Structures](https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxcdata/1afa0cd9-b1a0-4520-b623-bf15030af5d8)

## .msg File Structure
This is not intended to serve as an introduction to .msg files, but provides a brief overview of what a .msg file is and how it is stored.

The .msg file format is built on top of a more generic format called the Compound File Binary Format, which describes a basic file system and directory structure which is stored within a single file. .msg files are stored using this file system, but other types of file can be as well. This library exposes some .msg-specific functionality, but should work for any CFB files if used carefully.

As a file system, the includes a few of the standard components one might expect, as well as some less-common components
*   The CFB is composed of sectors
*   As everything is stored within a file, the initial sector contains a file header, magic number, etc.
*   The header file and potentially other sectors contain the "double-indirect file allocation table+ (DIFAT) which indicates which sectors contain the File ALlocation Table (FAT)
*   There is a File Allocation Table (FAT) to indicate which sectors are in use and how sectors combine to form entries
*   There is a directory with a root containing entries, folders, and possibly sub-folders.
*   The entries store application-specific data using application-specific names

.nsg files consist of a list of the .msg properties. These are stored as follows:
*   The root directory object contains
    *   Information about properties which are not in the [Exchange Server Protocols Master Property List](https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxprops/f6ab1613-aefe-447d-a49c-18217230b148)
    *   The file message properties
    *   A folder for each recipient
    *   A folder for each attachment
*   Properties for both the main message and for each attachment and each recipient folder are stored im two ways:
    *   Short (\< 8 byte) fixed-width properties are stored in a file named "__properties_version1.0" which exists in each folder
    *   Fixed-width properties longer than 8 bytes and variable-length properties are stored on per entry in entries named like "__substg1.0_########" where ######## is the property tag, encoding the property ID and the property type.

The public interface to .msg file properties is through a class called MSG (q.v.). For examples, please see the following projects:
*   [io.github.jmcleodfoss.msg_example](https://github.com/Jmcleodfoss/msgreader/blob/master/msg_example/README.md).CommandlineExample, a very simple console-based example which displays the date, sender, subject, message body, recipients, and attachment info for a .msg file, optionally saving the attachments
*   [io.github.jmcleodfoss.msg_example](https://github.com/Jmcleodfoss/msgreader/blob/master/msg_example/README.md).SwingExample, a relatively simple Swing application to display the same information as the CommandlineExample application
*   [io.github.jmcleodfoss.msgexplorer](https://github.com/Jmcleodfoss/msgreader/blob/master/msgexplorer/README.md), a full-featured JavaFX application which allows viewing any part of a .msg file.

## Versions #
### 1.0.0 ##
Initial version
*   [View Javadoc](https://javadoc.io/doc/io.github.jmcleodfoss/msg/1.0.0/io.github.jmcleodfoss.msg/module-summary.html)
*   [Download from Sonatype OSS Maven Repository](https://repo1.maven.org/maven2/io/github/jmcleodfoss/msg/1.0.0/msg-1.0.0.jar)
