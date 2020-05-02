package io.github.jmcleodfoss.msg;

/** This class is used to publish directory entry info to a client application
*	@see DirectoryEntry
* 	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/a94d7445-c4be-49cd-b6b9-2f4abc663817">MS-CFB Section 2.6: Compound File Directory Sectors</a>
*/
public class DirectoryEntryData {

	/** The directory entry index */
	final DirectoryEntry entry;

	/** The directory entry name
	*	@see DirectoryEntry#directoryEntryName
	* 	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/a94d7445-c4be-49cd-b6b9-2f4abc663817">MS-CFB Section 2.6: Compound File Directory Sectors</a>
	*/
	public final String name;

	/** The directory entry indexes of this entry's children
	*	@see Directory#getChildren
	*/
	final java.util.ArrayList<DirectoryEntry> children;

	/** The entry's size
	*	@see DirectoryEntry#streamSize
	* 	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/a94d7445-c4be-49cd-b6b9-2f4abc663817">MS-CFB Section 2.6: Compound File Directory Sectors</a>
	*/
	public final int size;

	/** The entry's starting sector
	*	@see DirectoryEntry#startingSectorLocation
	* 	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/a94d7445-c4be-49cd-b6b9-2f4abc663817">MS-CFB Section 2.6: Compound File Directory Sectors</a>
	*/
	public final int startingSector;

	/** All directory information for this entry
	*	@see DirectoryEntry#nm_DirectoryEntryName
	*	@see DirectoryEntry#nm_DirectoryEntryNameLength
	*	@see DirectoryEntry#nm_ObjectType
	*	@see DirectoryEntry#nm_ColorFlag
	*	@see DirectoryEntry#nm_LeftSiblingId
	*	@see DirectoryEntry#nm_RightSiblingId
	*	@see DirectoryEntry#nm_ChildId
	*	@see DirectoryEntry#nm_CLSID
	*	@see DirectoryEntry#nm_StateBits
	*	@see DirectoryEntry#nm_CreationTime
	*	@see DirectoryEntry#nm_ModifiedTime
	*	@see DirectoryEntry#nm_StartingSectorLocation
	*	@see DirectoryEntry#nm_StreamSize
	*	@see DirectoryEntry#nm_PropertyName
	*	@see DirectoryEntry#nm_PropertyId
	*	@see DirectoryEntry#nm_PropertyType
	*	@see DirectoryEntry#data
	*/
	public final KVPArray<String, String> kvps;

	/** An iterator through this entry's children, returning the children as DirectoryEntryData objects. */
	private class ChildIterator implements java.util.Iterator<DirectoryEntryData>
	{
		/** The iterator through the entry's children */
		private java.util.Iterator<DirectoryEntry> childIterator;

		/** The directory this entry is in */
		private Directory directory;

		/** The file's named properties list */
		NamedProperties namedProperties;

		/** Create an iterator through the entry's children by setting up the local iterator to shadow. */
		private ChildIterator(Directory directory, NamedProperties namedProperties)
		{
			this.directory = directory;
			this.namedProperties = namedProperties;
			childIterator = children.iterator();
		}

		/** Is there another entry in the list of children?
		*	@return	true if there is another entry, false otherwise
		*/
		public boolean hasNext()
		{
			return childIterator.hasNext();
		}

		/** Get the next child object.
		*	@return	A DirectoryEntryData object for the next child.
		*/
		public DirectoryEntryData next()
		{
			return new DirectoryEntryData(childIterator.next(), directory, namedProperties);
		}

	}

	/** Create the external data object for the given directory entry
	*	@param	de		The directory entry to shadow
	*	@param	directory	The Directory object the entry is from
	*	@param	namedProperties	The file's NamedProperties object to look up non-standard properties
	*	@see DirectoryEntry
	*/
	DirectoryEntryData(DirectoryEntry de, Directory directory, NamedProperties namedProperties)
	{
		entry = de;
		name = de.directoryEntryName;
		children = directory.getChildren(de);
		size = (int)de.streamSize;
		startingSector = de.startingSectorLocation;

		kvps = de.data(namedProperties, directory.parents);
	}

	/** Create an iterator through this entry's children
	* 	@param	directory	The directory this entry is in
	*	@param	namedProperties	The file's named properties list
	*	@return	An iterator through the entry's children as DirectoryEntryData objects
	*/
	java.util.Iterator<DirectoryEntryData> childIterator(Directory directory, NamedProperties namedProperties)
	{
		return new ChildIterator(directory, namedProperties);
	}

	/** Create a string representing this directory entry
	*	@return	The name of the entry
	*/
	public String toString()
	{
		return name;
	}
}
