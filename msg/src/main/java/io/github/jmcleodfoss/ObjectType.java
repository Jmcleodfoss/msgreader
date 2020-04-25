package io.github.jmcleodfoss.msg;

class ObjectType {
	static final byte UNKNOWN = 0x00;
	static final byte STORAGE = 0x01;
	static final byte STREAM = 0x02;
	static final byte ROOT_STORAGE = 0x05;

	private final byte type;

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
