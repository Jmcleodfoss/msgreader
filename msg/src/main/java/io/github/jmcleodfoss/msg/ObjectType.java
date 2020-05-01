package io.github.jmcleodfoss.msg;

/** The ObjectType class represents a CFB entry object type; it can be any of { Unknown, Storage, Stream, Root Storage. }
 * @see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/60fe8611-66c3-496b-b70d-a504c94c9ace">MS-CFB 2.6.1 Compound File Directory Entry</a>
 */
class ObjectType {
	/** The object type for Unknown or Unallocated entries. */
	private static final byte UNKNOWN = 0x00;

	/** The object type for Storage Objects */
	private static final byte STORAGE = 0x01;

	/** The object type for Stream Objects */
	private static final byte STREAM = 0x02;

	/** The object type for Root Storage Objects */
	private static final byte ROOT_STORAGE = 0x05;

	/** The actual object type. */
	private final byte type;

	/* Construct an ObjectType from the given byte read out of a directory entry block. */
	ObjectType(byte type)
	{
		if (type != UNKNOWN && type != STORAGE && type != STREAM && type != ROOT_STORAGE)
			throw new RuntimeException("Unrecognized storage type");

		this.type = type;
	}

	/** Is the a Root Storage Object?
	*	@return	true if this is a Root Storage Object, false otherwise.
	*/
	boolean isRootStorage()
	{
		return type == ROOT_STORAGE;
	}

	/** Is the a Storage Object?
	*	@return	true if this is a Storage Object, false otherwise.
	*/
	boolean isStorage()
	{
		return type == STORAGE;
	}

	/** Is the a Stream Object?
	*	@return	true if this is a Stream Object, false otherwise.
	*/
	boolean isStream()
	{
		return type == STREAM;
	}

	/** Create a String value describing this object type.
	*	@return	A String containing a description of this object type.
	*/
	public String toString()
	{
		switch (type){
		case UNKNOWN: return "Unknown or unallocated";
		case STORAGE: return "Storage Object";
		case STREAM: return "Stream Object";
		case ROOT_STORAGE: return "Root Storage Object";
		default: throw new RuntimeException("Unrecognized storage type");
		}
	}
}
