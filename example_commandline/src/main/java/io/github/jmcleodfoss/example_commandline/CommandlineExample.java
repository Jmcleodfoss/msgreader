package io.github.jmcleodfoss.example_commandline;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;
import io.github.jmcleodfoss.msg.Property;
import io.github.jmcleodfoss.msg.PropertyTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CommandlineExample
{
	private static String getPropertyValue(MSG msg, HashMap<Integer, Property> properties, int propertyTag)
	{
		if (properties.keySet().contains(propertyTag))
			return msg.getPropertyValue(properties.get(propertyTag));
		return "not found";
	}

	private static void showProperty(MSG msg, java.util.HashMap<Integer, Property> properties, int propertyTag, String propertyName)
	{
		System.out.printf("%s: %s\n", propertyName, getPropertyValue(msg, properties, propertyTag));
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

			showProperty(msg, properties, PropertyTags.PidTagClientSubmitTime, "Date sent");
			showProperty(msg, properties, PropertyTags.PidTagSenderName, "From");
			showProperty(msg, properties, PropertyTags.PidTagSenderEmailAddress, "Email");
			showProperty(msg, properties, PropertyTags.PidTagSubject, "Subject");
			showProperty(msg, properties, PropertyTags.PidTagBody, "Body");
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
