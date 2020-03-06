package io.github.jmcleodfoss.msg;

/** The named properties from an MSG file */
class NamedProperties
{
	/** The entry name for the GUID stream */
	private static final String GUID_STREAM_NAME = "__substg1.0_00020102";

	/** The list of GUIDs */
	private GUID[] guids;

	/** The entry name for the entry stream */
	private static final String ENTRY_STREAM_NAME = "__substg1.0_00030102";

	/** The list of entries */
	private DataWithIndexAndKind[] entries;

	/** The entry name for the string stream */
	private static final String STRING_STREAM_NAME = "__substg1.0_00040102";

	/** The list of strings in the string stream */
	private java.util.ArrayList<String> strings;

	/** The property ID to name mapping array */
	private DataWithIndexAndKind[] propertyNameMappings;

	/** Read in the named properties information
	*	@param	mbb	The CFB file to read from
	*	@param	header	The CFB header information
	*	@param	fat	The file allocation table
	*	@param	directory	The directory
	*	@param	miniFAT	The mini sector file allocation table.
	*/
	NamedProperties(java.nio.MappedByteBuffer mbb, Header header, FAT fat, Directory directory, MiniFAT miniFAT)
	{
		java.util.ArrayList<Integer> children = directory.getChildren(directory.namedPropertiesMappingIndex);
		java.util.Iterator<Integer> iter = children.iterator();

		// After accounting for the GUID, Entry, and String streams, the
		// remaining entries are for the property name / property ID mappings.
		int numPropertyNameMappings = children.size() - 3;
		propertyNameMappings = new DataWithIndexAndKind[numPropertyNameMappings];
		int pnmIndex = 0;

		while(iter.hasNext()){
			DirectoryEntry.StringStream de = (DirectoryEntry.StringStream)directory.entries.get(iter.next());

			// Read in all the data at once. This is overkill for the simple
			// case where all the data fits into one mini sector, but makes
			// it much easier to deal with an Entry stream which spans multiple
			// mini and non-mini sectors.
			byte[] data = new byte[(int)de.streamSize];
			data = (byte[])de.getContent(mbb, header, fat, miniFAT);

			if (GUID_STREAM_NAME.equals(de.directoryEntryName)){
				setGUIDS(de, data);
			} else if (ENTRY_STREAM_NAME.equals(de.directoryEntryName)){
				setEntries(de, data);
			} else if (STRING_STREAM_NAME.equals(de.directoryEntryName)){
				setStrings(de, data);
			} else {
				propertyNameMappings[pnmIndex] = new DataWithIndexAndKind(data);
				++pnmIndex;
			}
		}
	}

	/** Get the GUID from the GUID index
	*	@param	index	The GUID index
	*	@return	The GUID corresponding to the GUID index
	*/
	GUID indexToGUID(int index)
	{
		if (index == 1)
			return GUID.PS_MAPI;
		if (index == 2)
			return GUID.PS_PUBLIC_STRINGS;
		return guids[index-3];
	}

	/** Set the entries from the entry stream.
	*	@param	de	The String Stream containing the entries
	*	@param	data	The data for this entrym
	*/
	private void setEntries(DirectoryEntry.StringStream de, byte[] data)
	{
		int numEntries = (int)de.streamSize / DataType.SIZEOF_LONG;
		entries = new DataWithIndexAndKind[numEntries];
		for (int i = 0; i < numEntries; ++i)
			entries[i] = new DataWithIndexAndKind(java.util.Arrays.copyOfRange(data, i*DataType.SIZEOF_LONG, (i+1)*DataType.SIZEOF_LONG));
	}

	/** Set the GUIDs from the GUID stream
	*	@param	de	The String Stream containing the GUIDs.
	*	@param	data	The data for this entrym
	*/
	private void setGUIDS(DirectoryEntry.StringStream de, byte[] data)
	{
		int numGUIDS = (int)de.streamSize / GUID.SIZE;
		guids = new GUID[numGUIDS];
		for (int i = 0; i < numGUIDS; ++i)
			guids[i] = new GUID(java.util.Arrays.copyOfRange(data, i*GUID.SIZE, (i+1)*GUID.SIZE));
	}

	/** Set the strings from the string stream.
	*	@param	de	The String Stream containing the entries
	*	@param	data	The data for this entry
	*/
	private void setStrings(DirectoryEntry.StringStream de, byte[] data)
	{
		java.nio.ByteBuffer thisStream = java.nio.ByteBuffer.wrap(data);
		thisStream.order(java.nio.ByteOrder.LITTLE_ENDIAN);
		strings = new java.util.ArrayList<String>();
		int nRemaining = (int)de.streamSize;
		while (nRemaining > 0) {
			// Retrieving UTF-16 characters
			int stringLen = thisStream.getInt();
			nRemaining -= 4;
			byte[] stringData = new byte[stringLen];
			thisStream.get(stringData);
			nRemaining -= stringLen;
			strings.add(DataType.createString(stringData));
			for (int i = 0; i < stringLen % 4 && nRemaining > 0; ++i){
				thisStream.get();
				--nRemaining;
			}
		}
	}

	/**	Test this class by printing out the GUID, entries, and strings.
	*	@param	args	The command line arguments to the test application; this is expected to be a MSG file to be processed and a log level.
	*/
	public static void main(String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.NamedProperties msg-file [log-level]");
			System.exit(1);
		}
		try {
			java.util.logging.Level logLevel = args.length >= 2 ? Debug.getLogLevel(args[1]) : java.util.logging.Level.OFF;
			java.util.logging.Logger logger = java.util.logging.Logger.getLogger("io.github.jmcleodfoss.msg");
			logger.setLevel(logLevel);

			java.io.File file = new java.io.File(args[0]);
			java.io.FileInputStream stream = new java.io.FileInputStream(file);
			java.nio.channels.FileChannel fc = stream.getChannel();
			java.nio.MappedByteBuffer mbb = fc.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, fc.size());
			mbb.order(java.nio.ByteOrder.LITTLE_ENDIAN);

			Header header = new Header(mbb, fc.size());
			DIFAT difat = new DIFAT(mbb, header);
			FAT fat = new FAT(mbb, header, difat);
			Directory directory = new Directory(mbb, header, fat);	
			MiniFAT miniFAT = new MiniFAT(mbb, header, fat, directory);
			NamedProperties namedPropertiesMapping = new NamedProperties(mbb, header, fat, directory, miniFAT);

			System.out.println("GUID stream");
			for (int i = 0; i < namedPropertiesMapping.guids.length; ++i)
				System.out.println(namedPropertiesMapping.guids[i]);

			System.out.println();
			System.out.println("Entry stream");
			for (int i = 0; i < namedPropertiesMapping.entries.length; ++i)
				System.out.println(namedPropertiesMapping.entries[i]);

			System.out.println();
			System.out.println("String stream");
			java.util.Iterator<String> iter = namedPropertiesMapping.strings.iterator();
			while (iter.hasNext())
				System.out.println(iter.next());

			System.out.println();
			System.out.println("Entries");
			for (int i = 0; i < namedPropertiesMapping.propertyNameMappings.length; ++i)
				System.out.printf("%s GUID %s\n", 
					namedPropertiesMapping.propertyNameMappings[i],
					namedPropertiesMapping.indexToGUID(namedPropertiesMapping.propertyNameMappings[i].guidIndex)
					);
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
