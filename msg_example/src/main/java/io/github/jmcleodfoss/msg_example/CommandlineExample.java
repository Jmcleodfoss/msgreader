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

public class CommandlineExample
{
	private static final String OPTION_SAVE_ATTACHMENTS = "-s";

	private static final String ATTACHMENT_INFO_FORMAT = "%-25s %-30s %-10s\n";
	private static String getPropertyValue(MSG msg, HashMap<Integer, Property> properties, int propertyTag)
	{
		if (properties.keySet().contains(propertyTag))
			return msg.getPropertyValue(properties.get(propertyTag));
		return "not found";
	}

	private static void showProperty(MSG msg, HashMap<Integer, Property> properties, int propertyTag, String propertyName)
	{
		System.out.printf("%s: %s\n", propertyName, getPropertyValue(msg, properties, propertyTag));
	}

	private static void showMsgFile(String file, boolean fSaveAttachments)
	throws
		FileNotFoundException,
		IOException,
		NotCFBFileException
	{
		MSG msg = new MSG(file);

		DirectoryEntryData root = msg.getDirectoryTree();
		HashMap<Integer, Property> properties = msg.getPropertiesAsHashMap(root);

		showProperty(msg, properties, PropertyTags.PidTagClientSubmitTime, "Date sent");
		System.out.printf("From %s (%s)\n", getPropertyValue(msg, properties, PropertyTags.PidTagSenderName), getPropertyValue(msg, properties, PropertyTags.PidTagSenderEmailAddress));
		showProperty(msg, properties, PropertyTags.PidTagSubject, "Subject");
		showProperty(msg, properties, PropertyTags.PidTagBody, "Body");

		Iterator<DirectoryEntryData> recipients = msg.recipients();
		if (recipients.hasNext()) {
			System.out.println();
			System.out.println("Recipients");
			System.out.println("----------");
		}
		while (recipients.hasNext()){
			HashMap<Integer, Property> m = msg.getPropertiesAsHashMap(recipients.next());
			String name = getPropertyValue(msg, m, PropertyTags.PidTagDisplayName);
			String email = getPropertyValue(msg, m, PropertyTags.PidTagEmailAddress);
			System.out.printf("To: %s (%s)\n", name, email);
		}

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
			String size = getPropertyValue(msg, m, PropertyTags.PidTagAttachDataBinary);
			System.out.printf(ATTACHMENT_INFO_FORMAT, name, mimeType, PropertyTags.PidTagAttachDataBinary);

			if (fSaveAttachments) {
				// Look for attachment data
				Iterator<DirectoryEntryData> attachmentChildren = msg.getChildIterator(a);
				while (attachmentChildren.hasNext()) {
					DirectoryEntryData c = attachmentChildren.next();
					if (c.propertyTag == PropertyTags.PidTagAttachDataBinary) {
						File attachment = new File(name);
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
						break;
					}
				}
			}
		}
	}

	// args[0] is the path and filename to open
	public static void main(String[] args)
	{
		boolean fSaveAttachments = false;
		int numFiles = 0;
		for (String a: args){
			if (OPTION_SAVE_ATTACHMENTS.equals(a))
				fSaveAttachments = true;
			else
				++numFiles;
		}

		if (numFiles == 0) {
			System.out.println("use (assuming the jar files for example_commandline and msg are in the classpath):");
			System.out.println();
			System.out.println("\tjava io.github.jmcleodfoss.msg_example.CommandlineExample [-s] msg-file1 msg-file2 ...");
			System.out.println();
			System.out.println("Option:");
			System.out.println("\t-s: save all attachments found");
			System.exit(0);
		}

		try {
			boolean first = false;
			for(String f: args){
				if (OPTION_SAVE_ATTACHMENTS.equals(f))
					continue;

				if (first)
					System.out.println("--------------------\n");
				else
					first = true;

				showMsgFile(f, fSaveAttachments);
			}
		} catch (FileNotFoundException e) {
			System.out.printf("Error: %s not found\n", args[0]);
			System.exit(1);
		} catch (NotCFBFileException e) {
			System.out.printf("Error: %s is not a compound binary file format or msg file\n", args[0]);
			System.exit(1);
		} catch (IOException e) {
			System.out.printf("Error: %s is not a compound binary file format or msg file\n", args[0]);
			System.exit(1);
		}
	}
}
