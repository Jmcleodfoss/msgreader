package io.github.jmcleodfoss.msg;

/** This class is used to publish directory entry info to a client application
*	@see DirectoryEntry
* 	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/a94d7445-c4be-49cd-b6b9-2f4abc663817">MS-CFB Section 2.6: Compound File Directory Sectors</a>
*/
public class DirectoryEntryData {

	/** The directory entry index */
	public final int entry;

	/** The directory entry name
	*	@see DirectoryEntry#directoryEntryName
	* 	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/a94d7445-c4be-49cd-b6b9-2f4abc663817">MS-CFB Section 2.6: Compound File Directory Sectors</a>
	*/
	public final String name;

	/** The directory entry indexes of this entry's children
	*	@see Directory#getChildren
	*/
	public final java.util.ArrayList<Integer> children;

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

	/** Create the external data object for the given directory entry
	*	@param	directory	The Directory object the entry is from
	*	@param	entry		The index of the directory entry to retreive
	*	@param	namedProperties	The file's NamedProperties object to look up non-standard properties
	*	@see DirectoryEntry
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
