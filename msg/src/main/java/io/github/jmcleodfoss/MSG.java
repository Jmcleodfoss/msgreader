package io.github.jmcleodfoss.msg;

/** Consolidated interface for reading MSG files (this will work for other CFB
 * files but has special handling for some information found only in MSG files).
 */
public class MSG
{
	/**	The data stream for the file. */
	private java.io.FileInputStream stream;

	/**	The FileChannel of the data stream, used to jump around the file. */
	private java.nio.channels.FileChannel fc;

	/**	The file, as a memory-mapped byte file. */
	private java.nio.MappedByteBuffer mbb;

	/**	The header */
	private Header header;

	/**	The DIFAT */
	private DIFAT difat;

	/**	The DAT */
	private FAT fat;

	/**	The directory */
	private Directory directory;

	/**	The Mini FAT */
	private MiniFAT miniFAT;

	/**	The named properties */
	private NamedProperties namedProperties;

	/**	Create a FileChannel for the given filename and read in the
 	*	header, DIFAT, etc.
	*	@param	fn	The name of the file to read.
	*	@throws NotCFBFileException	The input stream does not contain a PST file.
	* 	@throws java.io.IOException	There was an I/O error reading the input stream.
	*/
	public MSG(String fn)
	throws
		NotCFBFileException,
		java.io.IOException
	{
		stream = new java.io.FileInputStream(fn);
		fc = stream.getChannel();

		mbb = fc.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, fc.size());
		mbb.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		header = new Header(mbb, fc.size());
		difat = new DIFAT(mbb, header);
		fat = new FAT(mbb, header, difat);
		directory = new Directory(mbb, header, fat);	
		miniFAT = new MiniFAT(mbb, header, fat, directory);
		namedProperties = new NamedProperties(mbb, header, fat, directory, miniFAT);
	}

	public KVPArray<String, String> headerData()
	{
		return header.data();
	}

	public KVPArray<Integer, Integer> difatData()
	{
		return difat.data();
	}

	String getFATChainString(java.util.Iterator<Integer> iterator)
	{
		java.lang.StringBuilder chain = new java.lang.StringBuilder();
		while (iterator.hasNext()){
			if (chain.length() > 0)
				chain.append(" ");
			chain.append(iterator.next());
		}
		return chain.toString();
	}

	/** Make FAT data available to client applications
	*	@return	An array of key-value pairs consisting of the stream names and the corresponding sector chains
	*/
	public KVPArray<String, String> fatData()
	{
		final String DIRECTORY_STREAM = "DirectoryStream";
		final String MINI_FAT_STREAM = "MiniFATStream";
		final String DIFAT_STREAM = "DIFATStream";
		final String APPLICATION_STREAM_FORMAT = "ApplicationDefinedStream%d";
		final String FREE_SECTORS = "FreeSectors";

		java.util.Map<String, Boolean> mandatoryEntries = new java.util.HashMap<String, Boolean>();
		mandatoryEntries.put(DIRECTORY_STREAM, false);
		mandatoryEntries.put(MINI_FAT_STREAM, false);
		mandatoryEntries.put(DIFAT_STREAM, false);

		KVPArray<String, String> l = new KVPArray<String, String>();

		java.util.Iterator<java.util.ArrayList<Integer>> chains = fat.getAllChains().iterator();
		int applicationChainIndex = 0;
		while (chains.hasNext()){
			java.util.ArrayList<Integer> chain = chains.next();
			int firstSector = chain.get(0);

			String entryName;
			if (firstSector == header.firstDirectorySectorLocation){
				entryName = DIRECTORY_STREAM;
				mandatoryEntries.put(entryName, true);
			} else if (firstSector == header.firstMiniFATSectorLocation){
				entryName = MINI_FAT_STREAM;
				mandatoryEntries.put(entryName, true);
			} else if (firstSector == header.firstDIFATSectorLocation){
				entryName = DIFAT_STREAM;
				mandatoryEntries.put(entryName, true);
			} else {
				entryName = String.format(APPLICATION_STREAM_FORMAT, applicationChainIndex++);
			}

			l.add(new KVPEntry<String, String>(entryName, getFATChainString(chain.iterator())));
		}

		java.util.Iterator<String> iter = mandatoryEntries.keySet().iterator();
		while (iter.hasNext()){
			String entryName = iter.next();
			if (!mandatoryEntries.get(entryName))
				l.add(new KVPEntry<String, String>(entryName, ""));
		}

		l.add(new KVPEntry<String, String>("FreeSectors", getFATChainString(fat.freeSectorIterator())));
		return l;
	}

	/** Get information for the requested directory entry
	*	@param	entry	The entry to retreive data for
	*	@return	A DirectoryEntryData structure providing information about this entry
	*/
	public DirectoryEntryData getDirectoryEntryData(int entry)
	{
		return new DirectoryEntryData(directory, entry);
	}

	/** Get the directory entry keys (this allows a table for display to
	*   be set up with the correct number of entries before we have any data)
	*	@return	A list of keys and values in the same order as
	*		getDirectoryEntryData but with empty strings for the values
	*/
	public static KVPArray<String, String> getDirectoryEntryKeys()
	{
		return DirectoryEntry.keys();
	}

	/** Get the raw bytes for the requested directory entry
	*	@param	entry	The entry to retreive data for
	*	@return	An array of the bytes in the directory entry.
	*/
	public byte[] getRawDirectoryEntry(int entry)
	{
		mbb.position(directory.entries.get(entry).directoryEntryPosition);
		byte[] data = new byte[DirectoryEntry.SIZE];
		mbb.get(data);
		return data;
	}

	/** Get the file pointed to by the given directory entry index
	*	@param	entry	The entry to retrieve the file for
	*	@return	An array of the bytes in the file.
	*/
	public byte[] getFile(int entry)
	{
		return directory.entries.get(entry).getContent(mbb, header, fat, miniFAT);
	}

	/** Create a string representation of the given bytes, assumed to be
	*   file content
	*	@param	data	The file contents
	*	@return	A string showing the file contents. This will be hex
	*		bytes if the field is not text.
	*/
	public String convertFileToString(int entry, byte[] data)
	{
		return directory.entries.get(entry).createString(data);
	}

	/** Is there a text representation of the "file" for a given directory,
	*   or is it binary?
	*	@param	entry	The directory entry to check the data type of
	*	@return	true if the file is text, false if it is binary
	*/
	public boolean isTextData(int entry)
	{
		return directory.entries.get(entry).isTextData();
	}

	/** Retrieve the contents of the requested sector.
	*	@param	i	The 0-based sector to retrieve. Note that this
	*			is not a sector number (sector #0 is physical
	*			sector 1, etc).
	*	@return	An array of bytes holding the stream contents
	*/
	public byte[] getSector(int i)
	{
		mbb.position(i*header.sectorSize);
		byte[] data = new byte[header.sectorSize];
		mbb.get(data);
		return data;
	}

	/** Get the number of sectors in the file
	*	@return	The number of sectors in the file
	*/
	public int numberOfSectors()
	{
		return header.numberOfSectors();
	}

	/**	Close the file.
	* 	@throws java.io.IOException	There was a problem closing the file.
	*/
	public void close()
	throws
		java.io.IOException
	{
		fc.close();
	}
}
