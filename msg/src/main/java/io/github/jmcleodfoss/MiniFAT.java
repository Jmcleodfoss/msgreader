package io.github.jmcleodfoss.msg;

/** The Mini File Allocation Table */
class MiniFAT {

	/** The number of bytes in a mini sector. */
	static final int MINI_SECTOR_SIZE = 64;

	/** The number of mini FAT sectors */
	final int numEntries;

	/** The mini FAT data */
	final int[] miniFATSectors;

	/** Read the Mini FAT
	*
	* 	@param	mbb	The data stream
	* 	@param	header	The CBF header structure
	* 	@param	fat	The file allocation table structure
	*/ 
	MiniFAT(java.nio.MappedByteBuffer mbb, Header header, FAT fat)
	{
		numEntries = header.numberOfMiniFATSectors * header.sectorSize / header.SIZEOF_INT;
		miniFATSectors = new int[numEntries];
		java.util.Iterator<Integer> iter = fat.chainIterator(header.firstMiniFATSectorLocation);
		int destIndex = 0;
		while (iter.hasNext()){
			mbb.position(Sector.offset(iter.next(), header));
			java.nio.IntBuffer al = mbb.asIntBuffer();
			al.get(miniFATSectors, destIndex, header.intsPerSector());
			destIndex += header.intsPerSector();
		}

	}

	/** Get the chains of mini sectors defined in the mini FAT.
	*   @return	A string containing all the chains in the mini FAT, one per line.
	*/
	public String getChains()
	{
		StringBuilder s = new StringBuilder();
		boolean[] shown = new boolean[numEntries];
		for (int i = 0; i < numEntries; ++i){
			if (shown[i])
				continue;
			if (miniFATSectors[i] == Sector.FREESECT){
				shown[i] = true;
				continue;
			}
			if (s.length() > 0)
				s.append("\n");

			int sector = i;
			do {
				if (sector != i)
					s.append(" ");
				s.append(sector);
				shown[sector] = true;
				sector = miniFATSectors[sector];
			} while (sector != Sector.ENDOFCHAIN);
		}
		return s.toString();
	}

	/**	Test this class by reading in the mini FAT index table and printing it out.
	*
	*	@param	args	The command line arguments to the test application; this is expected to be a MSG file to processed and a log level.
	*/
	public static void main(final String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.MiniFAT msg-file [log-level]");
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
			MiniFAT minifat = new MiniFAT(mbb, header, fat);

			System.out.println("Mini FAT contents");
			for (int i = 0; i < minifat.miniFATSectors.length; ++i)
				System.out.printf("%d: 0x%08x\n", i, minifat.miniFATSectors[i]);
			System.out.println("\nMini FAT sector chains");
			System.out.printf(minifat.getChains());
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
