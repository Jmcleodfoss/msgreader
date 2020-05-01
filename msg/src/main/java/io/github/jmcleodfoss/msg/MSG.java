package io.github.jmcleodfoss.msg;

/** Consolidated public interface for reading MSG files (this will probably work for other CFB
*   files but has special handling for some information found only in MSG files).
*/
public class MSG
{
	/** The data stream for the file. */
	private java.io.FileInputStream stream;

	/** The FileChannel of the data stream, used to jump around the file. */
	private java.nio.channels.FileChannel fc;

	/** The file, as a memory-mapped byte file. */
	private java.nio.MappedByteBuffer mbb;

	/** The header */
	private Header header;

	/** The DIFAT */
	private DIFAT difat;

	/** The DAT */
	private FAT fat;

	/** The directory */
	private Directory directory;

	/** The Mini FAT */
	private MiniFAT miniFAT;

	/** The named properties */
	private NamedProperties namedProperties;

	/** Create a FileChannel for the given filename and read in the
	*	header, DIFAT, etc.
	*	@param	fn	The name of the file to read.
	*	@throws	NotCFBFileException	The input stream does not contain a PST file.
	* 	@throws	java.io.IOException	There was an I/O error reading the input stream.
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

	/** Close the file.
	* 	@throws	java.io.IOException	There was a problem closing the file.
	*/
	public void close()
	throws
		java.io.IOException
	{
		fc.close();
	}

	/** Create a string representation of the given bytes, assumed to be
	*   file content
	*	@param	ded	The entry to convert the data for.
	*	@param	data	The file contents
	*	@return	A string showing the file contents. This will be hex
	*		bytes if the field is not text.
	*/
	public String convertFileToString(DirectoryEntryData ded, byte[] data)
	{
		return directory.entries.get(ded.entry).createString(data);
	}

	/** Get the data from the DIFAT, as an array of key-value pairs.
	*	@return	A KVP array of the DIFAT entries, with the DIFAT index
	*		as the key and the corresponding FAT sector as the value
	*/
	public KVPArray<Integer, Integer> difatData()
	{
		return difat.data();
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

	/** Get the attachment name for the given attachment stream storage object.
	*	@param	ded	The entry to find the attachment name of.
	*	@return	The attachment name, if one was found; null if no name was found
	*/
	public String getAttachmentName(DirectoryEntryData ded)
	{
		// Return the long name.
		DirectoryEntry sibling = directory.getSiblingByName(directory.entries.get(ded.entry), "__substg1.0_3707001F");
		if (sibling == null)
			return null;
		return DataType.createString(sibling.getContent(mbb, header, fat, miniFAT));
	}

	/** Get information for the requested directory entry
	*	@param	entry	The entry to retrieve data for
	*	@return	A DirectoryEntryData structure providing information about this entry
	*/
	public DirectoryEntryData getDirectoryEntryData(int entry)
	{
		return new DirectoryEntryData(directory, entry, namedProperties);
	}

	/** Get the header for a property entry. The interpretation of the header changes depending on the type of the entry's parent.
	*	@param	ded	The directory entry to retrieve the header from
	*	@param	data	The bytes to read the header from
	*	@return	A KVPArray of header property field names and values, which will be empty for recipient objects and all attachment objects except embedded msg files.
	*/
	public KVPArray<String, Integer> getPropertiesHeader(DirectoryEntryData ded, byte[] data)
	{
		return directory.parents.get(directory.entries.get(ded.entry)).getChildPropertiesHeader(data);
	}

	/** Get the data for a property entry.
	*	@param	ded	The entry to retrieve the data from
	*	@param	data	The bytes to read the data from
	*	@return	An ArrayList of {@link Property property values} read from the entry.
	*/
	public java.util.ArrayList<Property> getProperties(DirectoryEntryData ded, byte[] data)
	{
		DirectoryEntry de = directory.entries.get(ded.entry);
		return de.properties(data, directory.parents.get(de), namedProperties);
	}

	/** Is the directory entry for the given index a Root Storage Object?
	*	@param	index	The directory entry index.
	*	@return	true if this entry is a Root Storage Object, false otherwise.
	*/
	public boolean isRootStorageObject(int index)
	{
		return directory.entries.get(index).objectType.isRootStorage();
	}

	/** Is the directory entry for the given index a Storage Object?
	*	@param	index	The directory entry index.
	*	@return	true if this entry is a Storage Object, false otherwise.
	*/
	public boolean isStorageObject(int index)
	{
		return directory.entries.get(index).objectType.isStorage();
	}

	/** Is the directory entry for the given index a Stream Object?
	*	@param	ded	The directory entry to check.
	*	@return	true if this entry is a Stream Object, false otherwise.
	*/
	public boolean isStreamObject(DirectoryEntryData ded)
	{
		return directory.entries.get(ded.entry).objectType.isStream();
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

	/** Get the FAT sector chain for the given iterator as a String
	*	@param	iterator	The iterator to create the chain
	*				description for
	*	@return	A String listing the sectors in the chain
	*/
	private String getFATChainString(java.util.Iterator<Integer> iterator)
	{
		StringBuilder chain = new StringBuilder();
		while (iterator.hasNext()){
			if (chain.length() > 0)
				chain.append(" ");
			chain.append(iterator.next());
		}
		return chain.toString();
	}

	/** Get the file pointed to by the given directory entry index
	*	@param	ded	The entry to retrieve the file for
	*	@return	An array of the bytes in the file.
	*/
	public byte[] getFile(DirectoryEntryData ded)
	{
		return directory.entries.get(ded.entry).getContent(mbb, header, fat, miniFAT);
	}

	/** Get the mini FAT data as a table consisting of the mini FAT sectors
	*   in the first column, and the data in the second.
	*	@return	An array of the mini FAT chains and data
	*/
	public KVPArray<java.util.ArrayList<Integer>, byte[]> miniFATData()
	{
		KVPArray<java.util.ArrayList<Integer>, byte[]> l = new KVPArray<java.util.ArrayList<Integer>, byte[]>();

		java.util.Iterator<java.util.ArrayList<Integer>> chains = miniFAT.getAllChains().iterator();
		while (chains.hasNext()){
			java.util.ArrayList<Integer> chain = chains.next();
			java.util.Iterator<Integer> iter = chain.iterator();
			int destOffset = 0;
			byte[] data = new byte[chain.size()*header.miniSectorSize];
			while (iter.hasNext()){
				mbb.position(miniFAT.fileOffset(iter.next()));
				mbb.get(data, destOffset, header.miniSectorSize);
				destOffset += header.miniSectorSize;
			}

			l.add(new KVPEntry<java.util.ArrayList<Integer>, byte[]>(chain, data));
		}

		return l;
	}

	/** Is there a text representation of the "file" for a given directory,
	/** Get the raw bytes for the requested directory entry
	*	@param	ded	The entry to retrieve data for
	*	@return	An array of the bytes in the directory entry.
	*/
	public byte[] getRawDirectoryEntry(DirectoryEntryData ded)
	{
		mbb.position(directory.entries.get(ded.entry).directoryEntryPosition);
		byte[] data = new byte[DirectoryEntry.SIZE];
		mbb.get(data);
		return data;
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

	/** Get the data from the header, as an array of key-value pairs.
	*	@return	A KVP array of the header field names and values
	*/
	public KVPArray<String, String> headerData()
	{
		return header.data();
	}

	/** Is the given entry a Property entry?
	*	@param	ded	The directory entry to check the type of
	*	@return	true if the entry is a Properties entry, false otherwise
	*/
	public boolean isProperty(DirectoryEntryData ded)
	{
		return directory.entries.get(ded.entry).isPropertiesEntry();
	}

	/** Is there a text representation of the "file" for a given directory,
	*   or is it binary?
	*	@param	ded	The directory entry to check the data type of
	*	@return	true if the file is text, false if it is binary
	*/
	public boolean isTextData(DirectoryEntryData ded)
	{
		return directory.entries.get(ded.entry).isTextData();
	}

	/** Get a Named Property entry
	*	@param	mappingIndex	The index to the named property entry to retrieve
	*	@return	A KVP array of the information for the requested entry
	*/
	public KVPArray<String, String> namedPropertyEntry(int mappingIndex)
	{
		return namedProperties.getPropertyIdToPropertyNameMapping(mappingIndex);
	}

	/** Get the list of Named Properties GUIDs
	*	@return	The array of GUIDs as Strings
	*/
	public String[] namedPropertiesGUIDs()
	{
		String[] guidStrings = new String[namedProperties.guids.length];
		for (int i = 0; i < namedProperties.guids.length; ++i)
			guidStrings[i] = namedProperties.guids[i].toString();
		return guidStrings;
	}

	/** Get the numeric named properties entries
	*	@return	An ArrayList containing the named properties' numeric entries
	*/
	public java.util.ArrayList<EntryStreamEntryData> namedPropertiesNumericalEntries()
	{
		return namedProperties.getEntryStreamEntries(EntryStreamEntry.PropertyType.NUMERICAL_NAMED_PROPERTY);
	}

	/** Get the string named properties entries
	*	@return	An ArrayList containing the named properties' string entries
	*/
	public java.util.ArrayList<EntryStreamEntryData> namedPropertiesStringEntries()
	{
		return namedProperties.getEntryStreamEntries(EntryStreamEntry.PropertyType.STRING_NAMED_PROPERTY);
	}

	/** Get the named properties string stream as an array of key-value pairs.
	*	@return	A KVP array of the named property string stream entries as
	*/
	public KVPArray<Integer, String> namedPropertiesStrings()
	{
		KVPArray<Integer, String> a = new KVPArray<Integer, String>();
		for (java.util.Iterator<java.util.Map.Entry<Integer, String>> iter = namedProperties.stringsByOffset.entrySet().iterator(); iter.hasNext(); ){
			java.util.Map.Entry<Integer, String> entry = iter.next();
			a.add(entry.getKey(), entry.getValue());
		}
		return a;
	}

	/** Get the number of sectors in the file
	*	@return	The number of sectors in the file
	*/
	public int numberOfSectors()
	{
		return header.numberOfSectors();
	}
}
