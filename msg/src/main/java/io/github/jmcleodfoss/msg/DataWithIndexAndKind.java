package io.github.jmcleodfoss.msg;

class DataWithIndexAndKind
{
	/** The type of object. */
	static enum PropertyType {
		NUMERICAL_NAMED_PROPERTY,
		STRING_NAMED_PROPERTY
	};

	final int nameIdentifierOrStringOffset;
	final short propertyIndex;
	final short guidIndex;
	final PropertyType propertyType;

	/** Create a DataWithIndexAndKind from a raw byte stream
	*	@param	rawData	The byte stream to read this entry from.
	*/
	DataWithIndexAndKind(byte[] rawData)
	{
		java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(rawData);
		bb.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		nameIdentifierOrStringOffset = bb.getInt();
		int temp = bb.getInt();
		propertyIndex = (short)(temp >>> 16);
		guidIndex = (short)((temp & 0xffff) >>> 1);
		propertyType = ((temp & 0x01) != 0) ? PropertyType.STRING_NAMED_PROPERTY : PropertyType.NUMERICAL_NAMED_PROPERTY;
	}

	/** Get a String representation of this object.
	*	@return	A string representing this object
	*/
	public String toString()
	{
		String pt;
		if (propertyType == PropertyType.NUMERICAL_NAMED_PROPERTY)
			pt = "Numerical named property";
		else if (propertyType == PropertyType.STRING_NAMED_PROPERTY)
			pt = "String named property";
		else
			pt = "Unknown named property";
		return String.format("0x%08x property index 0x%04x GUID index 0x%04x %s", 
			nameIdentifierOrStringOffset, propertyIndex, guidIndex, pt);
	}
}
