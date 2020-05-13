package io.github.jmcleodfoss.msg_example;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;
import io.github.jmcleodfoss.msg.Property;
import io.github.jmcleodfoss.msg.PropertyTags;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** Simple example command line application using the msg library.
*   <p>
*   <pre>
*Use (assuming the jars for msg_example and msg are in the classpath):
*	java io.github.jmcleodfoss.msg_example.CommandlineExample [-s] msg-file-1 [msg-file 2 ...]
*Options
*   	-s: Save all attachments found in the main message (does not save anything for embedded msg entities)
*   </pre>
*/
public class CommandlineExample
{
	/** Command line option to save attachments */
	private static final String OPTION_SAVE_ATTACHMENTS = "-s";

	/** Format for displaying attachments and the description thereof */
	private static final String ATTACHMENT_INFO_FORMAT = "%-25s %-30s %-10s\n";

	/* Retrieve the value of a propertyTag
	*	@param	msg	The msg file object
	*	@param	properties	The list of properties for the message in the message file
	*	@param	propertyTag	The tag of the property to retrieve
	*	@return	A String containing the value of the property
	*/
	private static String getPropertyValue(MSG msg, HashMap<Integer, Property> properties, int propertyTag)
	{
		if (properties.keySet().contains(propertyTag))
			return msg.getPropertyValue(properties.get(propertyTag));
		return "not found";
	}

	/* Show the selected information about the given file, saving attachments if requested
	*	@param	filename	The name of the .msg file to open
	*	@param	fSaveAttachments	Whether to save the attachments found in the file
	*/
	private static void showMsgFile(String filename, boolean fSaveAttachments)
	throws
		FileNotFoundException,
		IOException,
		NotCFBFileException
	{
		// Get the message information we need
		MSG msg = new MSG(filename);
		HashMap<Integer, Property> properties = msg.getPropertiesAsHashMap(msg.getDirectoryTree());

		// Show selected properties
		System.out.printf("%s: %s\n", "Date sent", getPropertyValue(msg, properties, PropertyTags.PidTagClientSubmitTime));
		System.out.printf("From %s (%s)\n", getPropertyValue(msg, properties, PropertyTags.PidTagSenderName), getPropertyValue(msg, properties, PropertyTags.PidTagSenderEmailAddress));
		System.out.printf("%s: %s\n", "Subject", getPropertyValue(msg, properties, PropertyTags.PidTagSubject));
		System.out.printf("%s: %s\n", "Message", getPropertyValue(msg, properties, PropertyTags.PidTagBody));

		// Show recipients
		Iterator<DirectoryEntryData> recipients = msg.recipients();
		if (recipients.hasNext()) {
			System.out.println();
			System.out.println("Recipients");
			System.out.println("----------");
		}
		while (recipients.hasNext()){
			HashMap<Integer, Property> m = msg.getPropertiesAsHashMap(recipients.next());
			System.out.printf("To: %s (%s)\n", getPropertyValue(msg, m, PropertyTags.PidTagDisplayName), getPropertyValue(msg, m, PropertyTags.PidTagEmailAddress));
		}

		// Show attachment data
		Iterator<DirectoryEntryData> attachments = msg.attachments();
		if (attachments.hasNext()) {
			System.out.println();
			System.out.println("Attachment Info");
			System.out.println("---------------");
			System.out.printf(ATTACHMENT_INFO_FORMAT, "Long filename", "Mime type", "Size (bytes)");
			System.out.printf(ATTACHMENT_INFO_FORMAT, "-------------------------", "-----------------------------", "---------------");
		}
		while (attachments.hasNext()){
			DirectoryEntryData a = attachments.next();
			HashMap<Integer, Property> m = msg.getPropertiesAsHashMap(a);
			String name = getPropertyValue(msg, m, PropertyTags.PidTagAttachLongFilename);
			String mimeType = getPropertyValue(msg, m, PropertyTags.PidTagAttachMimeTag);
			String size = m.get(PropertyTags.PidTagAttachDataBinary).value();
			System.out.printf(ATTACHMENT_INFO_FORMAT, name, mimeType, PropertyTags.PidTagAttachDataBinary);

			// Save attachment if requested
			if (fSaveAttachments) {
				// Look for the entry which holds the attachment
				Iterator<DirectoryEntryData> attachmentChildren = msg.getChildIterator(a);
				while (attachmentChildren.hasNext()) {
					DirectoryEntryData c = attachmentChildren.next();
					if (c.propertyTag == PropertyTags.PidTagAttachDataBinary) {
						File attachment = new File(name);
						// Ensure we don't overwrite anything (use Windows style of changing filenames to add a number to guarantee uniqueness)
						if (attachment.exists()){
							String absPath = attachment.getAbsolutePath();
							int extensionIndex = absPath.lastIndexOf('.');
							String baseName = absPath.substring(0, extensionIndex);
							String extension = absPath.substring(extensionIndex);

							int i = 0;
							do {
								attachment = new File(String.format("%s (%d)%s", baseName, i++, extension));
							} while (attachment.exists());
						}

						FileChannel fc = new FileOutputStream(attachment).getChannel();
						fc.write(ByteBuffer.wrap(msg.getFile(c)));
						fc.close();
						System.out.printf("Saved attachment %s as %s\n", name, attachment.getAbsolutePath());
						break;
					}
				}
			}
		}
	}

	/** Process the given files
	*	@param	args	The command line arguments giving the file(s) to process and whether to save attachment data	
	*/
	public static void main(String[] args)
	{
		// Validate command line arguments
		boolean fSaveAttachments = false;
		int numFiles = 0;
		for (String a: args){
			if (OPTION_SAVE_ATTACHMENTS.equals(a))
				fSaveAttachments = true;
			else
				++numFiles;
		}

		// Print usage info if no valid arguments encountered
		if (numFiles == 0) {
			System.out.println("use (assuming the jar files for msg_example and msg are in the classpath):");
			System.out.println();
			System.out.println("\tjava io.github.jmcleodfoss.msg_example.CommandlineExample [-s] msg-file1 msg-file2 ...");
			System.out.println();
			System.out.println("Option:");
			System.out.println("\t-s: save all attachments found");
			System.exit(0);
		}

		// Process file(s)
		boolean first = false;
		for(String f: args){
			if (OPTION_SAVE_ATTACHMENTS.equals(f))
				continue;

			if (first)
				System.out.println("--------------------\n");
			else
				first = true;

			try {
				showMsgFile(f, fSaveAttachments);
			} catch (FileNotFoundException e) {
				System.out.printf("Error: %s not found\n", f);
			} catch (IOException e) {
				System.out.printf("Error reading %s or writing one of its attachments (if save requested)\n", f);
			} catch (NotCFBFileException e) {
				System.out.printf("Error: %s is not a compound binary file format or msg file\n", f);
			}
		}
	}
}
