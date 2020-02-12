package io.github.jmcleodfoss.msg;

/** The Directory Entry object contains a Compound File Directory Sector
*
* 	MS-CFB Section 2.6, Compound File Directory Sectors
*/
public class DirectoryEntry {

	static final String ROOT_ENTRY = "Root Entry";
	static final String NAMEID = "__nameid_version1.0";
	static final java.util.regex.Pattern STRING_STREAM_PATTERN = java.util.regex.Pattern.compile("__substg1.0_(\\p{XDigit}{4})(\\p{XDigit}{4})");
	static final String PROPERTIES = "__properties_version1.0";
	static final java.util.regex.Pattern RECIP_PATTERN = java.util.regex.Pattern.compile("__recip_version1.0_#\\p{XDigit}{8}");
	static final java.util.regex.Pattern ATTACH_PATTERN = java.util.regex.Pattern.compile("__attach_version1.0_#\\p{XDigit}{8}");
	static final String UNALLOCATED = "";

	final String directoryEntryName;
	final ObjectType objectType;
	final int leftSiblingId;
	final int rightSiblingId;
	final int childId;
	final ClassId clsid;
	final java.util.Date creationTime;
	final java.util.Date modifiedTime;
	final int startingSectorLocation;
	final long streamSize;

	protected DirectoryEntry(String directoryEntryName, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, ClassId clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize)
	{
		this.directoryEntryName = directoryEntryName;
		this.objectType = objectType;
		this.leftSiblingId = leftSiblingId;
		this.rightSiblingId = rightSiblingId;
		this.childId = childId;
		this.clsid = clsid;
		this.creationTime = creationTime;
		this.modifiedTime = modifiedTime;
		this.startingSectorLocation = startingSectorLocation;
		this.streamSize = streamSize;
	}

	Object getContent(java.nio.MappedByteBuffer mbb, Header header, FAT fat, MiniFAT miniFAT)
	{
		return null;
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
		clsid.toString(),
		creationTime.toString(), modifiedTime.toString()
		);
	}

	static class RootEntry extends DirectoryEntry {
		RootEntry(String directoryEntryName, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, ClassId clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize)
		{
			super(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize);
		}

		public String toString()
		{
			return String.format("%s %s %s child ID 0x%08x modified %s mini sector index %d size %d", directoryEntryName, objectType.toString(), clsid, childId, modifiedTime.toString(), startingSectorLocation, streamSize);
		}
	}

	/** NamedPropertiesMapping entries have no siblings and no storage and null Class IDs. */
	static class NamedPropertiesMapping extends DirectoryEntry {
		NamedPropertiesMapping(String directoryEntryName, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, ClassId clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize)
		{
			super(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize);
		}

		public String toString()
		{
			return String.format("Named Properties Mapping %s child ID 0x%08x, created %s modified %s",  objectType.toString(), childId, creationTime.toString(), modifiedTime.toString());
		}
	}

	static class StringStream extends DirectoryEntry {
		int propertyId;
		int propertyType;

		StringStream(String directoryEntryName, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, ClassId clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, String propertyId, String propertyType)
		{
			super(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize);
			this.propertyId = Integer.decode("0x"+ propertyId);
			this.propertyType = Integer.decode("0x"+ propertyType);
		}

		private byte[] getMiniStreamContent(java.nio.MappedByteBuffer mbb, Header header, FAT fat, MiniFAT miniFAT)
		{
			byte[] data = new byte[(int)streamSize];
			java.util.Iterator<Integer> iter = miniFAT.getChainIterator(startingSectorLocation, header, fat);
			int destOffset = 0;
			int nRemaining = (int)streamSize;
			while (iter.hasNext()){
				int miniFATSector = iter.next();
				mbb.position(miniFAT.fileOffset(miniFATSector));
				int nToRead = Math.min(nRemaining, MiniFAT.MINI_SECTOR_SIZE);
				mbb.get(data, destOffset, nToRead);
				destOffset += nToRead;
				nRemaining -= nToRead;
			}
			return data;
		}

		private byte[] getStreamContent(java.nio.MappedByteBuffer mbb, Header header, FAT fat)
		{
			byte[] data = new byte[(int)streamSize];
			java.util.Iterator<Integer> iter = fat.chainIterator(startingSectorLocation);
			int destOffset = 0;
			int nRemaining = (int)streamSize;
			while (iter.hasNext()){
				int sector = iter.next();
				mbb.position((sector+1)*header.sectorSize);
				int nToRead = Math.min(nRemaining, header.sectorSize);
				mbb.get(data, destOffset, nToRead);
				destOffset += nToRead;
				nRemaining -= nToRead;
			}
			return data;
		}

		Object getContent(java.nio.MappedByteBuffer mbb, Header header, FAT fat, MiniFAT miniFAT)
		{
			// Check for tag
			String tagName = PropertyTags.tags.get(propertyId);
			if (tagName != null)
				System.out.printf("\ntag 0x%04x found %s type 0x%04x size %d\n", propertyId, tagName, propertyType, streamSize);
			else
				System.out.printf("\ntag 0x%04x not found in tag list type 0x%04x size %d\n", propertyId, propertyType, streamSize);

			byte[] data;
			if (streamSize < header.miniStreamCutoffSize)
				data = getMiniStreamContent(mbb, header, fat, miniFAT);
			else {
				data = getStreamContent(mbb, header, fat);
			}

			switch(propertyType){
			case 0x01f:
				return DataType.createString(data);

			case 0x0102:
				return data;

			default:
				System.out.printf("unrecognized data type 0x%04x\n", propertyType);
			}

			return "Not implemented";
		}

		public String toString()
		{
			return String.format("String Stream %s 0x%04x 0x%04x starting sector %d size %d", objectType.toString(), propertyId, propertyType, startingSectorLocation, streamSize);
		}
	}

	/** Properties have no siblings or children; Class ID and dates are always null, and Object Type is always Stream Object. */
	static class Properties extends DirectoryEntry {
		Properties(String directoryEntryName, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, ClassId clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize)
		{
			super(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize);
		}

		public String toString()
		{
			return String.format("Properties %s starting sector %d size %d", objectType.toString(), startingSectorLocation, streamSize);
		}
	}

	static class Recipient extends DirectoryEntry {
		Recipient(String directoryEntryName, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, ClassId clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize)
		{
			super(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize);
		}

		public String toString()
		{
			return String.format("Recipient %s child Id 0x%04x", objectType.toString(), childId);
		}
	}

	static class Attachment extends DirectoryEntry {
		Attachment(String directoryEntryName, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, ClassId clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize)
		{
			super(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize);
		}

		public String toString()
		{
			return String.format("Attachment %s child Id 0x%04x", objectType.toString(), childId);
		}
	}

	static class Unallocated extends DirectoryEntry {
		Unallocated(String directoryEntryName, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, ClassId clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize)
		{
			super(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize);
		}

		public String toString()
		{
			return String.format("Unallocated %s", objectType.toString());
		}
	}
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
		new DataDefinition(nm_CLSID, DataType.classIdReader, true),
		new DataDefinition("StateBits", DataType.integer32Reader),
		new DataDefinition(nm_CreationTime, DataType.timeReader, true),
		new DataDefinition(nm_ModifiedTime, DataType.timeReader, true),
		new DataDefinition(nm_StartingSectorLocation, DataType.integer32Reader, true),
		new DataDefinition(nm_StreamSize, DataType.integer64Reader, true)
	};

	/** Size of the directory entry */
	static final int SIZE = DataDefinition.size(fields);

	/** Create a directory entry of the required type based on the directory entry name.
	*	@param	byteBuffer	The data stream for the msg file.
	*/
	static DirectoryEntry factory(java.nio.ByteBuffer byteBuffer)
	throws
		java.io.IOException
	{
		DataContainer dc = new DataContainer();
		dc.read(byteBuffer, fields);

		/* The name length returned includes the terminating null. */
		int directoryEntryNameLength = (Short)dc.get(nm_DirectoryEntryNameLength) - 1;
		String directoryEntryName = ((String)dc.get(nm_DirectoryEntryName)).substring(0, directoryEntryNameLength/2);
		ObjectType objectType = new ObjectType((Byte)dc.get(nm_ObjectType));
		int leftSiblingId = (Integer)dc.get(nm_LeftSiblingId);	
		int rightSiblingId = (Integer)dc.get(nm_RightSiblingId);	
		int childId = (Integer)dc.get(nm_ChildId);	
		ClassId clsid = (ClassId)dc.get(nm_CLSID);
		java.util.Date creationTime = (java.util.Date)dc.get(nm_CreationTime);
		java.util.Date modifiedTime = (java.util.Date)dc.get(nm_ModifiedTime);
		int startingSectorLocation = (Integer)dc.get(nm_StartingSectorLocation);
		long streamSize = (Long)dc.get(nm_StreamSize);

		java.util.regex.Matcher matcher;
		if (ROOT_ENTRY.equals(directoryEntryName)){
			return new RootEntry(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize); 
		} else if (NAMEID.equals(directoryEntryName)){
			return new NamedPropertiesMapping(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize); 
		} else if ((matcher = STRING_STREAM_PATTERN.matcher(directoryEntryName)).matches()){
			return new StringStream(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, matcher.group(1), matcher.group(2)); 
		} else if (PROPERTIES.equals(directoryEntryName)){
			return new Properties(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize); 
		} else if (RECIP_PATTERN.matcher(directoryEntryName).matches()){
			return new Recipient(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize); 
		} else if (ATTACH_PATTERN.matcher(directoryEntryName).matches()){
			return new Attachment(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize); 
		} else if (UNALLOCATED.equals(directoryEntryName)){
			return new Unallocated(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize); 
		} else {
			System.out.println(directoryEntryName);
			return new DirectoryEntry(directoryEntryName, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize);
		}
	}
}
