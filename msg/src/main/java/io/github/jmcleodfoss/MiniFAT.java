package io.github.jmcleodfoss.msg;

/** The Mini File Allocation Table */
class MiniFAT {

	/** Iterator for Mini FAT index entry chains. This returns the offset of the next mini sector to read. */
	class ChainIterator implements java.util.Iterator<Integer> {

		/** The next entry to be returned. */
		private int entry;

		ChainIterator(int firstMiniSector, Header header, FAT fat)
		{
			entry = firstMiniSector;
		}

		/** Is there a new entry to return? */
		public boolean hasNext()
		{
			return entry != Sector.ENDOFCHAIN;
		}

		/** Return the next FAT index entry */
		public Integer next()
		{
			int retval = entry;
			entry = miniFATSectors[entry];
			return retval;
		}
	}

	/** The number of bytes in a mini sector. */
	static final int MINI_SECTOR_SIZE = 64;

	/** The sector size (from the file header */
	private final int sectorSize;

	/** The number of mini sectors in a full sector */
	private final int miniSectorsPerFullSector;

	/** The number of mini FAT sectors */
	final int numEntries;

	/** The mini FAT data */
	final int[] miniFATSectors;

	/** The mini stream sectors. */
	private java.util.ArrayList<Integer> miniSectors = new java.util.ArrayList<Integer>();

	/** Read the Mini FAT
	*
	* 	@param	mbb	The data stream
	* 	@param	header	The CBF header structure
	* 	@param	fat	The file allocation table structure
	*/ 
	MiniFAT(java.nio.MappedByteBuffer mbb, Header header, FAT fat, Directory directory)
	{
		sectorSize = header.sectorSize;
		miniSectorsPerFullSector = sectorSize / MINI_SECTOR_SIZE;
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

		java.util.Iterator<Integer> miniSectorIterator = fat.chainIterator(directory.entries.get(0).startingSectorLocation);
		while(miniSectorIterator.hasNext()) {
			miniSectors.add(miniSectorIterator.next());
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

	int fileOffset(int miniSectorEntry)
	{
		int fullSectorIndex = miniSectorEntry / miniSectorsPerFullSector;
		int fullSector = miniSectors.get(fullSectorIndex);
		int sectorFileOffset = (fullSector+1) * sectorSize;
		int miniSectorIndexThisSector = miniSectorEntry % miniSectorsPerFullSector;
		int miniSectorOffsetIntoThisSector = miniSectorIndexThisSector * MINI_SECTOR_SIZE;
		return sectorFileOffset + miniSectorOffsetIntoThisSector;
	}
	java.util.Iterator<Integer> getChainIterator(int firstSector, Header header, FAT fat)
	{
		return new ChainIterator(firstSector, header, fat);
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
			Directory directory = new Directory(mbb, header, fat);
			MiniFAT minifat = new MiniFAT(mbb, header, fat, directory);

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
