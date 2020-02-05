package io.github.jmcleodfoss.msg;

/** The File Allocation Table */
class FAT {
	/** The number of FAT entries
	*
	*	@see	Header.numberOfFATSectors
	*/
	final private int numEntries;

	/** The list of FAT index entries. */
	final private int[] fat;

	/** Create a FAT
	*
	* 	@param	mbb	The data stream
	* 	@param	header	The CBF header structur
	*/ 
	FAT(java.nio.MappedByteBuffer mbb, Header header, DIFAT difat)
	{
		// First index in a FAT sector is the FAT signature
		// //and the last is either the index to the next sector, or the empty sector flag, 0xffffffff
		numEntries = header.numberOfFATSectors * header.intsPerSector();
		fat = new int[numEntries];

		mbb.rewind();
		java.nio.IntBuffer al = mbb.asIntBuffer();

		int destIndex = 0;

		java.util.Iterator<Integer> difatIterator = difat.iterator();
		while (difatIterator.hasNext()){
			int currentSector = difatIterator.next();
			if (currentSector == Sector.FREESECT)
				continue;
			int readOffset = (currentSector + 1) * header.intsPerSector();
			al.position(readOffset);
			al.get(fat, destIndex, header.intsPerSector());
			destIndex += header.intsPerSector();
		}
	}

	/**	Test this class by reading in the FAT index table and printing it out.
	*
	*	@param	args	The command line arguments to the test application; this is expected to be a MSG file to processed and a log level.
	*/
	public static void main(final String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.FAT msg-file [log-level]");
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

			for (int i = 0; i < FAT.numEntries; ++i)
				System.out.printf("%d: %s\n", i, Sector.getDescription(i));
			}
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}

