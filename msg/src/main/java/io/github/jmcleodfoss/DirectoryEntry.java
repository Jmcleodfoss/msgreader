package io.github.jmcleodfoss.msg;

/** The Directory Entry object contains a Compound File Directory Sector
*
* 	MS-CFB Section 2.6, Compound File Directory Sectors
*/
public class DirectoryEntry {

	private static final String ROOT_ENTRY = "Root Entry";
	private static final String NAMEID = "__nameid_version1.0";
	private static final java.util.regex.Pattern STRING_STREAM_PATTERN = java.util.regex.Pattern.compile("__substg1.0_(\\p{XDigit}{4})(\\p{XDigit}{4})");
	private static final String PROPERTIES = "__properties_version1.0";
	private static final java.util.regex.Pattern RECIP_PATTERN = java.util.regex.Pattern.compile("__recip_version1.0_#\\p{XDigit}{8}");
	private static final java.util.regex.Pattern ATTACH_PATTERN = java.util.regex.Pattern.compile("__attach_version1.0_#\\p{XDigit}{8}");
	private static final String UNALLOCATED = "";

	/** Property IDs are only defined for string stream entries; 0x0000 is
	*   never used as a property ID, so we use it as a sentinel value to
	*   indicate no property ID exists for other classes
	*	@see getPropertyId()
	*/
	private static final int NO_PROPERTY_ID = 0x0000;

	final String directoryEntryName;
	final int directoryEntryPosition;
	final ObjectType objectType;
	final int leftSiblingId;
	final int rightSiblingId;
	final int childId;
	final GUID clsid;
	final java.util.Date creationTime;
	final java.util.Date modifiedTime;
	final int startingSectorLocation;
	final long streamSize;

	/** The data repository (preserved after constructor since we don't
	*   read everything from it that we might want to display).
	*/
	private final DataContainer dc;

	protected DirectoryEntry(String directoryEntryName, int directoryEntryPosition, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, GUID clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, DataContainer dc)
	{
		this.directoryEntryName = directoryEntryName;
		this.directoryEntryPosition = directoryEntryPosition;
		this.objectType = objectType;
		this.leftSiblingId = leftSiblingId;
		this.rightSiblingId = rightSiblingId;
		this.childId = childId;
		this.clsid = clsid;
		this.creationTime = creationTime;
		this.modifiedTime = modifiedTime;
		this.startingSectorLocation = startingSectorLocation;
		this.streamSize = streamSize;
		this.dc = dc;
	}

	String createString(byte[] data)
	{
		if (data == null)
			return "Empty";
		return ByteUtil.createHexByteString(data);
	}

	byte[] getContent(java.nio.MappedByteBuffer mbb, Header header, FAT fat, MiniFAT miniFAT)
	{
		return null;
	}

	/** Return the property ID, if any.
	*	@return	The property ID. The default implementation, suitable
	*		for all classes except StringStrem, returns
	*		NO_PROPERTY_ID, a sentinel value indicating that there
	*		is no property related to this object type.
	*	@see	NO_PROPERTY_ID
	*/
	int getPropertyId()
	{
		return NO_PROPERTY_ID;
	}

	boolean isTextData()
	{
		return false;
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

	static class Attachment extends DirectoryEntry {
		private Attachment(String directoryEntryName, int directoryEntryPosition, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, GUID clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, DataContainer dc)
		{
			super(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc);
		}

		public String toString()
		{
			return String.format("Attachment %s child Id 0x%04x", objectType.toString(), childId);
		}
	}

	/** NamedPropertiesMapping entries have no siblings and no storage and null Class IDs. */
	static class NamedPropertiesMapping extends DirectoryEntry {
		private NamedPropertiesMapping(String directoryEntryName, int directoryEntryPosition, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, GUID clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, DataContainer dc)
		{
			super(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc);
		}

		public String toString()
		{
			return String.format("Named Properties Mapping %s child ID 0x%08x, created %s modified %s",  objectType.toString(), childId, creationTime.toString(), modifiedTime.toString());
		}
	}

	/** Properties have no siblings or children; Class ID and dates are always null, and Object Type is always Stream Object. */
	static class Properties extends DirectoryEntry {
		private Properties(String directoryEntryName, int directoryEntryPosition, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, GUID clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, DataContainer dc)
		{
			super(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc);
		}

		public String toString()
		{
			return String.format("Properties %s starting sector %d size %d", objectType.toString(), startingSectorLocation, streamSize);
		}
	}

	static class Recipient extends DirectoryEntry {
		private Recipient(String directoryEntryName, int directoryEntryPosition, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, GUID clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, DataContainer dc)
		{
			super(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc);
		}

		public String toString()
		{
			return String.format("Recipient %s child Id 0x%04x", objectType.toString(), childId);
		}
	}

	static class RootEntry extends DirectoryEntry {
		private RootEntry(String directoryEntryName, int directoryEntryPosition, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, GUID clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, DataContainer dc)
		{
			super(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc);
		}

		public String toString()
		{
			return String.format("%s %s %s child ID 0x%08x modified %s mini sector index %d size %d", directoryEntryName, objectType.toString(), clsid, childId, modifiedTime.toString(), startingSectorLocation, streamSize);
		}
	}

	static class StringStream extends DirectoryEntry {
		private static final int PROPERTY_TYPE_STRING = 0x001f;

		int propertyId;
		int propertyType;

		private StringStream(String directoryEntryName, int directoryEntryPosition, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, GUID clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, String propertyId, String propertyType, DataContainer dc)
		{
			super(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc);
			this.propertyId = Integer.decode("0x"+ propertyId);
			this.propertyType = Integer.decode("0x"+ propertyType);
		}

		@Override
		String createString(byte[] data)
		{
			if (data != null && isTextData())
				return DataType.createString(data);
			return super.createString(data);
		}

		@Override
		byte[] getContent(java.nio.MappedByteBuffer mbb, Header header, FAT fat, MiniFAT miniFAT)
		{
			if (streamSize < header.miniStreamCutoffSize)
				return miniFAT.read(startingSectorLocation, streamSize, mbb);
			return fat.read(startingSectorLocation, streamSize, mbb, header);
		}

		/* Return the property Id.
		*	@return	The property ID for this object.
		*/
		@Override
		int getPropertyId()
		{
			return this.propertyId;
		}

		@Override
		boolean isTextData()
		{
			return propertyType == PROPERTY_TYPE_STRING;
		}

		public String toString()
		{
			return String.format("String Stream %s 0x%04x 0x%04x starting sector %d size %d", objectType.toString(), propertyId, propertyType, startingSectorLocation, streamSize);
		}
	}

	static class Unallocated extends DirectoryEntry {
		private Unallocated(String directoryEntryName, int directoryEntryPosition, ObjectType objectType, int leftSiblingId, int rightSiblingId, int childId, GUID clsid, java.util.Date creationTime, java.util.Date modifiedTime, int startingSectorLocation, long streamSize, DataContainer dc)
		{
			super(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc);
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
	private static String nm_StateBits = "StateBits";
	private static String nm_CreationTime = "CreationTime";
	private static String nm_ModifiedTime = "ModifiedTime";
	private static String nm_StartingSectorLocation = "StartingSectorLocation";
	private static String nm_StreamSize = "StreamSize";
	private static String fldnm_PropertyName = "PropertyName";

	private static final DataDefinition[] fields = {
		new DataDefinition(nm_DirectoryEntryName, new DataType.UnicodeString(64), true),
		new DataDefinition(nm_DirectoryEntryNameLength, DataType.integer16Reader, true),
		new DataDefinition(nm_ObjectType, DataType.integer8Reader, true),
		new DataDefinition(nm_ColorFlag, DataType.integer8Reader, true),
		new DataDefinition(nm_LeftSiblingId, DataType.integer32Reader, true),
		new DataDefinition(nm_RightSiblingId, DataType.integer32Reader, true),
		new DataDefinition(nm_ChildId, DataType.integer32Reader, true),
		new DataDefinition(nm_CLSID, DataType.classIdReader, true),
		new DataDefinition(nm_StateBits, DataType.integer32Reader, true),
		new DataDefinition(nm_CreationTime, DataType.timeReader, true),
		new DataDefinition(nm_ModifiedTime, DataType.timeReader, true),
		new DataDefinition(nm_StartingSectorLocation, DataType.integer32Reader, true),
		new DataDefinition(nm_StreamSize, DataType.integer64Reader, true)
	};

	/** Size of the directory entry */
	static final int SIZE = DataDefinition.size(fields);

	/** Make full directory information data available to client applications
	*	@return	An array of key-value pairs consisting of a description of the data and the data itself
	*/
	KVPArray<String, String> data(final NamedProperties namedProperties, final java.util.HashMap<String, String> parents)
	{
		KVPArray<String, String> l = new KVPArray<String, String>();

		int propertyId = getPropertyId();
		String propertyName;
		if (propertyId == NO_PROPERTY_ID) {
			if (directoryEntryName.equals(NAMEID)){
				propertyName = "Named Property Mapping Storage";
			} else {
				propertyName = "n/a";
			}
		} else if (parents.get(directoryEntryName).equals(NAMEID)){
			if (propertyId == 0x0002) {
				propertyName = "GUID Stream";
			} else if (propertyId == 0x0003) {
				propertyName = "Entry Stream";
			} else if (propertyId == 0x0004) {
				propertyName = "String Stream";
			} else {
				propertyName = "Property Name to Property ID Mapping Stream";
			}
		} else if ((propertyId & 0x8000) != 0) {
			int propertyIndex = propertyId & 0x7fff;
			propertyName = namedProperties.getPropertyName(propertyIndex);
		} else if (PropertyTags.tags.keySet().contains(propertyId)) {
			propertyName = PropertyTags.tags.get(propertyId);
		} else {
			propertyName = String.format("Unknown property 0x%04x", propertyId);
		}
		l.add(fldnm_PropertyName, propertyName);

		l.add(nm_DirectoryEntryName, directoryEntryName);
		l.add(nm_DirectoryEntryNameLength, Short.toString((Short)dc.get(nm_DirectoryEntryNameLength)));
		l.add(nm_ObjectType, objectType.toString());
		l.add(nm_ColorFlag, Byte.toString((Byte)dc.get(nm_ColorFlag)));
		l.add(nm_LeftSiblingId, Integer.toString(leftSiblingId));
		l.add(nm_RightSiblingId, Integer.toString(rightSiblingId));
		l.add(nm_ChildId, Integer.toString(childId));
		l.add(nm_CLSID, clsid.toString());
		l.add(nm_StateBits, Integer.toString((Integer)dc.get(nm_StateBits)));
		l.add(nm_CreationTime, creationTime.toString());
		l.add(nm_ModifiedTime, modifiedTime.toString());
		l.add(nm_StartingSectorLocation, Integer.toString(startingSectorLocation));
		l.add(nm_StreamSize, Long.toString(streamSize));
		return l;
	}

	/** Create a directory entry of the required type based on the directory entry name.
	*	@param	byteBuffer	The data stream for the msg file.
	*/
	static DirectoryEntry factory(java.nio.ByteBuffer byteBuffer)
	throws
		java.io.IOException
	{
		DataContainer dc = new DataContainer();
		int directoryEntryPosition = byteBuffer.position();
		dc.read(byteBuffer, fields);

		/* The name length returned includes the terminating null. */
		int directoryEntryNameLength = (Short)dc.get(nm_DirectoryEntryNameLength) - 1;
		String directoryEntryName = ((String)dc.get(nm_DirectoryEntryName)).substring(0, directoryEntryNameLength/2);
		ObjectType objectType = new ObjectType((Byte)dc.get(nm_ObjectType));
		int leftSiblingId = (Integer)dc.get(nm_LeftSiblingId);	
		int rightSiblingId = (Integer)dc.get(nm_RightSiblingId);	
		int childId = (Integer)dc.get(nm_ChildId);	
		GUID clsid = (GUID)dc.get(nm_CLSID);
		java.util.Date creationTime = (java.util.Date)dc.get(nm_CreationTime);
		java.util.Date modifiedTime = (java.util.Date)dc.get(nm_ModifiedTime);
		int startingSectorLocation = (Integer)dc.get(nm_StartingSectorLocation);
		long streamSize = (Long)dc.get(nm_StreamSize);

		java.util.regex.Matcher matcher;
		if (ROOT_ENTRY.equals(directoryEntryName)){
			return new RootEntry(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc); 
		} else if (NAMEID.equals(directoryEntryName)){
			return new NamedPropertiesMapping(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc); 
		} else if ((matcher = STRING_STREAM_PATTERN.matcher(directoryEntryName)).matches()){
			return new StringStream(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, matcher.group(1), matcher.group(2), dc); 
		} else if (PROPERTIES.equals(directoryEntryName)){
			return new Properties(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc); 
		} else if (RECIP_PATTERN.matcher(directoryEntryName).matches()){
			return new Recipient(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc); 
		} else if (ATTACH_PATTERN.matcher(directoryEntryName).matches()){
			return new Attachment(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc); 
		} else if (UNALLOCATED.equals(directoryEntryName)){
			return new Unallocated(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc); 
		} else {
			System.out.println(directoryEntryName);
			return new DirectoryEntry(directoryEntryName, directoryEntryPosition, objectType, leftSiblingId, rightSiblingId, childId, clsid, creationTime, modifiedTime, startingSectorLocation, streamSize, dc);
		}
	}

	/** Provide keys (with empty values) to allow tables to be set up with
	*   the correct length before we have any data.
	*	@return	An array of key-value pairs consisting of a description of the data and an empty string.
	*/
	static KVPArray<String, String> keys()
	{
		KVPArray<String, String> l = new KVPArray<String, String>();
		l.add(fldnm_PropertyName, "");
		l.add(nm_DirectoryEntryName, "");
		l.add(nm_DirectoryEntryNameLength, "");
		l.add(nm_ObjectType, "");
		l.add(nm_ColorFlag, "");
		l.add(nm_LeftSiblingId, "");
		l.add(nm_RightSiblingId, "");
		l.add(nm_ChildId, "");
		l.add(nm_CLSID, "");
		l.add(nm_StateBits, "");
		l.add(nm_CreationTime, "");
		l.add(nm_ModifiedTime, "");
		l.add(nm_StartingSectorLocation, "");
		l.add(nm_StreamSize, "");
		return l;
	}

	public static void main(String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.Directory msg-file [log-level]");
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
			Directory directory = new Directory(mbb, header, fat);
			MiniFAT miniFAT = new MiniFAT(mbb, header, fat, directory);

			java.util.Iterator<DirectoryEntry> iterator = directory.iterator();
			int i = 0;
			while (iterator.hasNext()){
				DirectoryEntry de = iterator.next();
				System.out.printf("0x%02x: left 0x%08x right 0x%08x child 0x%08x %s\n",
					i, de.leftSiblingId, de.rightSiblingId, de.childId, de.objectType.toString());
				byte[] data = de.getContent(mbb, header, fat, miniFAT);
				if (data != null)
					System.out.println(de.createString(data));
				System.out.println();
				++i;
			}
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
