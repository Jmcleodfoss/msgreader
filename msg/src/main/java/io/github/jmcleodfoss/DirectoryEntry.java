package io.github.jmcleodfoss.msg;

/** The Directory Entry object contains a Compound File Directory Sector
*
* 	MS-CFB Section 2.6, Compound File Directory Sectors
*/
public class DirectoryEntry {

	private static String nm_DirectoryEntryName = "DirectoryEntryName";
	private static String nm_DirectoryEntryNameLength = "DirectoryEntryNameLength";
	private static String nm_ObjectType = "ObjectType";
	private static String nm_ColorFlag = "ColorFlag";
	private static String nm_LeftSiblingId = "LeftSiblingId";
	private static String nm_RightSiblingId = "RightSiblingId";
	private static String nm_ChildId = "ChildId";
	private static String nm_CLSID = "CLSID";
	private static String nm_CreationTime = "CreationTime";
	private static String nm_ModifiedTime = "ModifiedTime";
	private static String nm_StartingSectorLocation = "StartingSectorLocation";
	private static String nm_StreamSize = "StreamSize";

	private static final DataDefinition[] fields = {
		new DataDefinition(nm_DirectoryEntryName, new DataType.UnicodeString(64), true),
		new DataDefinition(nm_DirectoryEntryNameLength, DataType.integer16Reader, true),
		new DataDefinition(nm_ObjectType, DataType.integer8Reader, true),
		new DataDefinition("ColorFlag", DataType.integer8Reader),
		new DataDefinition(nm_LeftSiblingId, DataType.integer32Reader, true),
		new DataDefinition(nm_RightSiblingId, DataType.integer32Reader, true),
		new DataDefinition(nm_ChildId, DataType.integer32Reader, true),
		new DataDefinition(nm_CLSID, DataType.classIdReader),
		new DataDefinition("StateBits", DataType.integer32Reader),
		new DataDefinition(nm_CreationTime, DataType.timeReader, true),
		new DataDefinition(nm_ModifiedTime, DataType.timeReader, true),
		new DataDefinition(nm_StartingSectorLocation, DataType.integer32Reader, true),
		new DataDefinition(nm_StreamSize, DataType.integer64Reader, true)
	};

	/** Size of the directory entry */
	private static final int SIZE = DataDefinition.size(fields);

	final String directoryEntryName;
	final ObjectType objectType;
	final int leftSiblingId;
	final int rightSiblingId;
	final int childId;
	final byte[] clsid;
	final java.util.Date creationTime;
	final java.util.Date modifiedTime;
	final int startingSectorLocation;
	final long streamSize;

	DirectoryEntry(java.nio.ByteBuffer byteBuffer)
	throws
		java.io.IOException
	{
		DataContainer dc = new DataContainer();
		dc.read(byteBuffer, fields);

		/* The name length returned includes the terminating null. */
		int directoryEntryNameLength = (Short)dc.get(nm_DirectoryEntryNameLength) - 1;
		directoryEntryName = ((String)dc.get(nm_DirectoryEntryName)).substring(0, directoryEntryNameLength/2);
		objectType = new ObjectType((Byte)dc.get(nm_ObjectType));
		leftSiblingId = (Integer)dc.get(nm_LeftSiblingId);	
		rightSiblingId = (Integer)dc.get(nm_RightSiblingId);	
		childId = (Integer)dc.get(nm_ChildId);	
		clsid = (byte[])dc.get(nm_CLSID);
		creationTime = (java.util.Date)dc.get(nm_CreationTime);
		modifiedTime = (java.util.Date)dc.get(nm_ModifiedTime);
		startingSectorLocation = (Integer)dc.get(nm_StartingSectorLocation);
		streamSize = (Long)dc.get(nm_StreamSize);
	}

	public String toString()
	{
		return String.format("name %s\n" +
			"starting sector %d (0x%08x) size %d\n" +
			"object type %s\n" +
			"left sibling 0x%08x right sibling 0x%08x child 0x%08x\n" +
			"class ID %s\n" +
			"created %s modified %s\n",
		directoryEntryName,
		startingSectorLocation, startingSectorLocation, streamSize,
		objectType.toString(),
		leftSiblingId, rightSiblingId, childId,
		clsid,
		creationTime.toString(), modifiedTime.toString()
		);
	}

	/** Return the size of a directory entry.
	*
	*	@return	The size of the directory entry.
	*/
	static public int size()
	{
		return SIZE;
	}
}
