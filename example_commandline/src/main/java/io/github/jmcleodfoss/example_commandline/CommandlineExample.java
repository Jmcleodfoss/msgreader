package io.github.jmcleodfoss.example_commandline;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;
import io.github.jmcleodfoss.msg.Property;

import java.util.ArrayList;
import java.util.Iterator;

public class CommandlineExample
{
	// Property names to print (see PropertyTags.java)
	static final int TIME_SENT = 0x00390040;
	static final int SENDER_NAME = 0x0c1a001f;
	static final int SENDER_EMAIL = 0x1f001f;
	static final int SUBJECT = 0x0037001f;
	static final int BODY = 0x1000001f;

	private static void showProperty(MSG msg, java.util.HashMap<Integer, Property> properties, int propertyTag, String propertyName)
	{
		if (properties.keySet().contains(propertyTag))
			System.out.printf("%s: %s\n", propertyName, msg.getPropertyValue(properties.get(propertyTag)));
		else
			System.out.printf("%s: not found\n", propertyName);
	}

	// args[0] is the path and filename to open
	public static void main(String[] args)
	{
		if (args.length < 1) {
			System.out.println("use: javac -cp \"example_commandline-0.0-SNAPSHOT.jar;msg-0.0-SNAPSHOT.jar\" msg-filename");
			System.exit(0);
		}

		MSG msg;
		try {
			msg = new MSG(args[0]);

			DirectoryEntryData root = msg.getDirectoryTree();
			java.util.HashMap<Integer, Property> properties = msg.getPropertiesForParentAsHashMap(root);

			showProperty(msg, properties, TIME_SENT, "Date sent");
			showProperty(msg, properties, SENDER_NAME, "From");
			showProperty(msg, properties, SENDER_EMAIL, "Email");
			showProperty(msg, properties, SUBJECT, "Subject");
			showProperty(msg, properties, BODY, "Bodey");
		} catch (java.io.FileNotFoundException e) {
			System.out.printf("Error: %s not found\n", args[0]);
			System.exit(1);
		} catch (NotCFBFileException e) {
			System.out.printf("Error: %s is not a compound binary file format or msg file\n", args[0]);
			System.exit(1);
		} catch (java.io.IOException e) {
			System.out.printf("Error: %s is not a compound binary file format or msg file\n", args[0]);
			System.exit(1);
		}
	}
}
