package io.github.jmcleodfoss.msg;

class ObjectType {
	private static final byte UNKNOWN = 0x00;
	private static final byte STORAGE = 0x01;
	private static final byte STREAM = 0x02;
	private static final byte ROOT_STORAGE = 0x05;

	byte type;

	ObjectType(byte type)
	{
		if (type != UNKNOWN && type != STORAGE && type != STREAM && type != ROOT_STORAGE)
			throw new RuntimeException("Unrecognized storage type");

		this.type = type;
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