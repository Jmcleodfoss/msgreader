package io.github.jmcleodfoss.msg;

/** The directory structure in the CFB. */
class Directory {

	/** The directory entries. */
	java.util.ArrayList<DirectoryEntry> entries;

	/** The index to the named properties directory entry */
	int namedPropertiesMappingIndex;

	/** Construct a directory object.
	*	@param	bytebuffer	The CFB file
	*	@param	header		The CFB header
	*	@param	FAT		The CFB file allocation table
	*	@throws	java.io.IOException	An error was encountered reading the directory structure.
	*/
	Directory(java.nio.ByteBuffer byteBuffer, Header header, FAT fat)
	throws
		java.io.IOException
	{
		entries = new java.util.ArrayList<DirectoryEntry>();

		java.util.Iterator<Integer> chain = fat.chainIterator(header.firstDirectorySectorLocation);
		while(chain.hasNext()){
			int dirSector = chain.next();
			byteBuffer.position((dirSector+1)*header.sectorSize);
			for (int i = 0; i < header.sectorSize / DirectoryEntry.SIZE; ++i) {
				DirectoryEntry de = DirectoryEntry.factory(byteBuffer);
				entries.add(de);
				if (de instanceof DirectoryEntry.NamedPropertiesMapping)
					namedPropertiesMappingIndex = entries.indexOf(de);
			}
		}
	}

	/** Collect all siblings and self for the given childIndex.
	*	@param	siblings	The list of children of childIndex's parent
	*	@param	childIndex	The given child for the parent we are collecting the children of.
	*/
	void addSiblings(java.util.ArrayList<Integer> siblings, int childIndex)
	{
		DirectoryEntry child = entries.get(childIndex);
		if (child.leftSiblingId != Sector.FREESECT)
			addSiblings(siblings, child.leftSiblingId);
		siblings.add(childIndex);
		if (child.rightSiblingId != Sector.FREESECT)
			addSiblings(siblings, child.rightSiblingId);
	}

	/** Get the children for a given node.
	*	@param parentIndex	The directory entry index of the parent we want to find the children of, if any.
	*	@return	The (possibly entry) list of children of the directory entry for parentIndex.
	*/
	java.util.ArrayList<Integer> getChildren(int parentIndex)
	{
		java.util.ArrayList<Integer> children = new java.util.ArrayList<Integer>();
		int childIndex = entries.get(parentIndex).childId;
		if (childIndex != Sector.FREESECT){
			addSiblings(children, childIndex);
		}
		return children;
	}

	/** Get an iterator through the directory entries. */
	java.util.Iterator<DirectoryEntry> iterator()
	{
		return entries.iterator();
	}

	/**	Test this class by printing out the directory and the list of children for each node.
	*
	*	@param	args	The command line arguments to the test application; this is expected to be a MSG file to be processed and a log level.
	*/
	public static void main(String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.Directory msg-file [log-level]");
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

			Header header = new Header(mbb);
			DIFAT difat = new DIFAT(mbb, header);
			FAT fat = new FAT(mbb, header, difat);
			Directory directory = new Directory(mbb, header, fat);	

			java.util.Iterator<DirectoryEntry> iterator = directory.iterator();
			int i = 0;
			while (iterator.hasNext())
				System.out.printf("0x%02x: %s\n", i++, iterator.next().toString());

			System.out.println("\n");
			for (i = 0; i < directory.entries.size(); ++i){
				java.util.ArrayList<Integer> children = directory.getChildren(i);
				if (children.size() > 0){
					System.out.printf("Children of 0x%02x:\n", i);
					java.util.Iterator<Integer> childIterator = children.iterator();
					while (childIterator.hasNext())
						System.out.println("\t" + childIterator.next());
				}
			}
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
