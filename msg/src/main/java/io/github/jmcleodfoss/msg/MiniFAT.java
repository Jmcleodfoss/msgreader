package io.github.jmcleodfoss.msg;

/** The Mini File Allocation Table */
class MiniFAT {

	/** Iterator for Mini FAT index entry chains. This returns the offset of the next mini sector to read. */
	class ChainIterator implements java.util.Iterator<Integer> {

		/** The next entry to be returned. */
		private int entry;

		/** Initialize the mini chain iterator
		*	@param	firstMiniSector	The first sector in the mini chain
		*/
		ChainIterator(int firstMiniSector)
		{
			entry = firstMiniSector;
		}

		/** Is there a new entry to return?
 		*	@return	true if there is another entry, false if there is not
 		*/
		public boolean hasNext()
		{
			return entry != Sector.ENDOFCHAIN;
		}

		/** Return the next mini FAT index entry
		*	@return	The next entry in the mini FAT sector chain
		*/
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
	private final int numEntries;

	/** The mini FAT data */
	private final int[] miniFATSectors;

	/** The mini stream sectors. */
	private java.util.ArrayList<Integer> miniSectors = new java.util.ArrayList<Integer>();

	/** Read the Mini FAT
	* 	@param	mbb	The data stream
	* 	@param	header	The CBF header structure
	* 	@param	fat	The file allocation table structure
	* 	@param	directory	The directory for this file
	*/
	MiniFAT(java.nio.MappedByteBuffer mbb, Header header, FAT fat, Directory directory)
	{
		sectorSize = header.sectorSize;
		miniSectorsPerFullSector = sectorSize / MINI_SECTOR_SIZE;
		numEntries = header.numberOfMiniFATSectors * header.sectorSize / DataType.SIZEOF_INT;
		miniFATSectors = new int[numEntries];
		java.util.Iterator<Integer> iter = fat.chainIterator(header.firstMiniFATSectorLocation);
		int destIndex = 0;
		while (iter.hasNext()){
			mbb.position(header.offset(iter.next()));
			java.nio.IntBuffer al = mbb.asIntBuffer();
			al.get(miniFATSectors, destIndex, header.intsPerSector());
			destIndex += header.intsPerSector();
		}

		java.util.Iterator<Integer> miniSectorIterator = fat.chainIterator(directory.entries.get(0).startingSectorLocation);
		while(miniSectorIterator.hasNext()) {
			miniSectors.add(miniSectorIterator.next());
		}
	}

	/** Get the physical file offset for the given mini sector entry
	*	@param	miniSectorEntry	The mini sector entry to retrieve the file offset of
	*	@return	A file offset suitable for use in ByteBuffer.position
	*/
	int fileOffset(int miniSectorEntry)
	{
		int fullSectorIndex = miniSectorEntry / miniSectorsPerFullSector;
		int fullSector = miniSectors.get(fullSectorIndex);
		int sectorFileOffset = (fullSector+1) * sectorSize;
		int miniSectorIndexThisSector = miniSectorEntry % miniSectorsPerFullSector;
		int miniSectorOffsetIntoThisSector = miniSectorIndexThisSector * MINI_SECTOR_SIZE;
		return sectorFileOffset + miniSectorOffsetIntoThisSector;
	}

	/** Get all the mini sector chains
	*	@return	An ArrayList of ArrayLists containing the mini sector chains
	*/
	java.util.ArrayList<java.util.ArrayList<Integer>> getAllChains()
	{
		java.util.ArrayList<java.util.ArrayList<Integer>> chains = new java.util.ArrayList<java.util.ArrayList<Integer>>();

		boolean[] shown = new boolean[numEntries];
		for (int i = 0; i < numEntries; ++i){
			if (shown[i])
				continue;

			if (miniFATSectors[i] == Sector.FREESECT){
				shown[i] = true;
				continue;
			}

			/* Found a new chain */
			java.util.ArrayList<Integer> thisChain = new java.util.ArrayList<Integer>();

			int sector = i;
			do {
				thisChain.add(sector);
				shown[sector] = true;
				sector = miniFATSectors[sector];
			} while (sector != Sector.ENDOFCHAIN);

			chains.add(thisChain);
		}

		return chains;
	}

	/** Create an iterator through a mini sector chain given the first sector
	*	@param	firstSector	The first sector of the chain to return
	*	@return	An iterator which will return all the mini FAT sector indices in
	*		the chain
	*/
	java.util.Iterator<Integer> getChainIterator(int firstSector)
	{
		return new ChainIterator(firstSector);
	}

	/** Get the chains of mini sectors defined in the mini FAT.
	*   @return	A string containing all the chains in the mini FAT, one per line.
	*/
	String getChains()
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

	/** Read the contents of a mini FAT sector chain in the mini stream
	*	@param	startingSector	The starting sector in the mini stream
	*	@param	size	The number of bytes in the mini FAT sector chain
	*	@param	mbb	The file to read
	*	@return	An array of bytes consisting of the contents of the
	*		requested mini FAT sector chain
	*/
	byte[] read(int startingSector, long size, java.nio.MappedByteBuffer mbb)
	{
		int nRemaining = (int)size;
		byte[] data = new byte[nRemaining];
		java.util.Iterator<Integer> iter = getChainIterator(startingSector);
		int destOffset = 0;
		while (iter.hasNext()){
			int miniFATSector = iter.next();
			mbb.position(fileOffset(miniFATSector));
			int nToRead = Math.min(nRemaining, MINI_SECTOR_SIZE);
			mbb.get(data, destOffset, nToRead);
			destOffset += nToRead;
			nRemaining -= nToRead;
		}
		return data;
	}

	/** Test this class by reading in the mini FAT index table and printing it out.
	*	@param	args	The command line arguments to the test application; this is expected to be a MSG file to processed and a log level.
	*/
	public static void main(final String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.MiniFAT msg-file [log-level]");
			System.exit(1);
		}
		try {
			java.io.File file = new java.io.File(args[0]);
			java.io.FileInputStream stream = new java.io.FileInputStream(file);
			java.nio.channels.FileChannel fc = stream.getChannel();
			java.nio.MappedByteBuffer mbb = fc.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, fc.size());
			mbb.order(java.nio.ByteOrder.LITTLE_ENDIAN);

			Header header = new Header(mbb, fc.size());
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
