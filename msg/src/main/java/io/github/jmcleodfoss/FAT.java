package io.github.jmcleodfoss.msg;

/** The File Allocation Table */
class FAT {
	/** The number of FAT entries
	*	@see	Header.numberOfFATSectors
	*/
	final private int numEntries;

	/** The list of FAT index entries. */
	final private int[] fat;

	/** Iterator for FAT index entry chains */
	class ChainIterator implements java.util.Iterator<Integer> {

		/** The next entry to be returned. */
		private int entry;

		/** Initialize the iterator through the FAT sector chains */
		ChainIterator(int firstSector)
		{
			entry = firstSector;
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
			entry = fat[entry];
			return retval;
		}
	}

	/** Iterator for free FAT entries */
	class FreeSectorIterator implements java.util.Iterator<Integer> {

		/** The next entry to be returned. */
		private int entry;

		/** Create an iterator over the free sectors */
		FreeSectorIterator()
		{
			while (entry < numEntries && fat[entry] != Sector.FREESECT)
				entry++;
		}

		/** Is there a new free entry to return? */
		public boolean hasNext()
		{
			return entry < numEntries && fat[entry] == Sector.FREESECT;
		}

		/** Return the next FAT free entry */
		public Integer next()
		{
			int retval = entry;
			do {
				++entry;
			} while (entry < numEntries && fat[entry] != Sector.FREESECT);
			return retval;
		}
	}

	/** Get an iterator for this FAT */
	java.util.Iterator<Integer> chainIterator(int firstSector)
	{
		return new ChainIterator(firstSector);
	}

	/** Get an iterator for free sectors in this FAT */
	java.util.Iterator<Integer> freeSectorIterator()
	{
		return new FreeSectorIterator();
	}

	/** Read in the entire FAT
	* 	@param	mbb	The data stream
	* 	@param	header	The CBF header structure
	* 	@param	difat	The double-indirect file allocation table structure.
	*/ 
	FAT(java.nio.MappedByteBuffer mbb, Header header, DIFAT difat)
	{
		// First index in a FAT sector is the FAT signature
		// and the last is either the index to the next sector, or the empty sector flag, 0xffffffff
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

	/** Get all the sector chains
	*	@return	An ArrayList of ArrayLists containing the sector chains
	*/
	java.util.ArrayList<java.util.ArrayList<Integer>> getAllChains()
	{
		java.util.ArrayList<java.util.ArrayList<Integer>> chains = new java.util.ArrayList<java.util.ArrayList<Integer>>();

		boolean[] shown = new boolean[numEntries];
		for (int i = 0; i < numEntries; ++i){
			if (shown[i])
				continue;

			/* FAT sector chains are defined in the DIFAT.
			*  DIFAT sector chains are defined in the DIFAT.
			*  Free sectors are not chained.
			*/
			if (fat[i] == Sector.FATSECT || fat[i] == Sector.DIFSECT || fat[i] == Sector.FREESECT){
				shown[i] = true;
				continue;
			}

			/* Found a new chain */
			java.util.ArrayList<Integer> thisChain = new java.util.ArrayList<Integer>();

			int sector = i;
			do {
				thisChain.add(sector);
				shown[sector] = true;
				sector = fat[sector];
			} while (sector != Sector.ENDOFCHAIN);

			chains.add(thisChain);
		}

		return chains;
	}

	/** Get a String representation of all the sector chains in the FAT,
	*   one chain per line.
	*/
	public String getChains()
	{
		java.util.Iterator<java.util.ArrayList<Integer>> chainsIterator = getAllChains().iterator();

		StringBuilder s = new StringBuilder();
		while(chainsIterator.hasNext()){
			if (s.length() > 0)
				s.append("\n");
			java.util.Iterator<Integer> thisChain = chainsIterator.next().iterator();
			boolean first = true;
			while (thisChain.hasNext()){
				if (first)
					first = false;
				else
					s.append(" ");
				s.append(thisChain.next());
			}
		}
		return s.toString();
	}

	/**	Test this class by reading in the FAT index table and printing it out.
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

			Header header = new Header(mbb, fc.size());
			DIFAT difat = new DIFAT(mbb, header);
			FAT fat = new FAT(mbb, header, difat);

			System.out.println("FAT contents");
			for (int i = 0; i < fat.numEntries; ++i)
				System.out.printf("%d: %s\n", i, Sector.getDescription(fat.fat[i]));

			System.out.println("\nFAT sector chains");
			System.out.println(fat.getChains());

			System.out.println("\nFAT free sectors");
			StringBuilder s = new StringBuilder();
			java.util.Iterator<Integer> iter = fat.freeSectorIterator();
			while (iter.hasNext()){
				if (s.length() > 0)
					s.append(" ");
				s.append(iter.next());
			}
			System.out.println(s);
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
