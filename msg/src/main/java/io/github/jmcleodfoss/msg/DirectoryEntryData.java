package io.github.jmcleodfoss.msg;

/** This class is used to publish directory entry info to a client application
*	@see DirectoryEntry
*/
public class DirectoryEntryData {

	/** The directory entry index */
	public final int entry;

	/** The directory entry name */
	public final String name;

	/** The entry's children */
	public final java.util.ArrayList<Integer> children;

	/** The entry's size */
	public final int size;

	/** The entry's starting sector */
	public final int startingSector;

	/** All directory information for this entry */
	public final KVPArray<String, String> kvps;

	/** Create the external data object for the given directory entry
	*	@param	directory	The Directory object the entry is from
	*	@param	entry	The index of the directory entry to retreive
	*	@param	namedProperties	The file's NamedProperties object to look up non-standard properties
	*/
	DirectoryEntryData(Directory directory, int entry, NamedProperties namedProperties)
	{
		this.entry = entry;
		final DirectoryEntry de = directory.entries.get(entry);
		name = de.directoryEntryName;
		children = directory.getChildren(entry);
		size = (int)de.streamSize;
		startingSector = de.startingSectorLocation;

		kvps = de.data(namedProperties, directory.parents);
	}

	/** Create a string representing this directory entry
	*	@return	The name of the entry
	*/
	public String toString()
	{
		return name;
	}
}
