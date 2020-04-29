package io.github.jmcleodfoss.msg;

/** Class for passing fixed-width property data to client applications
*	@see DirectoryEntry#properties
*	@see DirectoryEntry.Properties
*/
public abstract class Property
{
	/** The property tag, consisting of a 2-byte property ID and a 2-byte data type as (Property ID) &lt;&lt; 16 | (Data Type)
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxprops/6f258f9a-5727-4a8e-b9d8-9bb729487ff8">MS-OXPROPS Section 2: Structures</a>
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxcdata/0c77892e-288e-435a-9c49-be1c20c7afdb">MS-OXCDATA Section 2.11.1: Property Data Types</a>
	*/
	public final int propertyTag;

	/** The property flags.
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxmsg/3be0f1c8-6dec-46ea-ad81-1ad30b0ac816">MS-OXMSG Section 2.4.2.1: Fixed Length Property Entry</a>
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxmsg/bac41dfb-c824-4e3c-9b5e-b61106f6739f">MS-OXMSG Section 2.4.2.2: Variable Length Property or Multiple-Valued Property Entry</a>
	*/
	public final int flags;

	/** The property's name. This is either looked up in the file's Named Property list; if it's not found there, it's
	*   looked up in the canonical list of PropertyTags, and if it's not found there, it will be the text
	*   "Not found: 0x####" where #### is the property tag value in hex.
	*   @see <a href="https://github.com/Jmcleodfoss/pstreader/blob/master/extras/properties.csv">pstreader/extras/properties.csv</a>
	*   @see <a href="https://github.com/Jmcleodfoss/msgreader/blob/master/extras/getpropertytags.sh">msgreader/extras.getpropertytags.sh</a>
	*/
	public final String propertyName;

	/** A description of the property type.
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxcdata/0c77892e-288e-435a-9c49-be1c20c7afdb">MS-OXCDATA Section 2.11.1: Property Data Types</a>
	*/
	public final String propertyType;

	/** Construct a Property object.
	*	@param	propertyTag	The property tag. @see propertyTag
	*	@param	propertyName	The property's name. @see propertyName
	*	@param	propertyType	The property's type. @see propertyType
	*	@param	flags	The property flags. @see flags
	*/
	private Property(int propertyTag, String propertyName, String propertyType, int flags)
	{
		this.propertyTag = propertyTag;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.flags = flags;
	}

	/** Return a String representation of the property's value.
	*	@return	A String which represents the property's value.
	*/
	public abstract String value();

	/** Encapsulate a Boolean property. */
	private static class Boolean extends Property
	{
		/** The value of the Boolean property */
		private boolean property;

		/** Create a representation of a Boolean property.
		*	@param	propertyTag	{@inheritDoc}
		*	@param	propertyName	{@inheritDoc}
		*	@param	propertyType	{@inheritDoc}
		*	@param	flags		{@inheritDoc}
		*	@param	bb		The ByteBuffer from which to read the property value.
		**/
		private Boolean(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			this.property = bb.get() != 0;

			// Skip remaining bytes for this entry
			bb.position(bb.position()+7);
		}

		/** Return a String representation of the property's value.
		*	@return	{@inheritDoc}
		*/
		@Override
		public String value()
		{
			return java.lang.Boolean.toString(property);
		}
	}

	/** Encapsulate a 32-bit integer property. */
	private static class Integer32 extends Property
	{
		/** The value of the 32-bit integer property */
		private int property;

		/** Create a representation of a 32-bit integer property.
		*	@param	propertyTag	{@inheritDoc}
		*	@param	propertyName	{@inheritDoc}
		*	@param	propertyType	{@inheritDoc}
		*	@param	flags		{@inheritDoc}
		*	@param	bb		The ByteBuffer from which to read the property value.
		**/
		private Integer32(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			this.property = bb.getInt();

			// Skip remaining bytes for this entry
			bb.position(bb.position()+4);
		}

		/** Return a String representation of the property's value.
		*	@return	{@inheritDoc}
		*/
		@Override
		public String value()
		{
			return String.format("0x%08x", property);
		}
	}

	/** Encapsulate a 64-bit integer property. */
	private static class Integer64 extends Property
	{
		/** The value of the 64-bit integer property */
		private long property;

		/** Create a representation of a 64-bit integer property.
		*	@param	propertyTag	{@inheritDoc}
		*	@param	propertyName	{@inheritDoc}
		*	@param	propertyType	{@inheritDoc}
		*	@param	flags		{@inheritDoc}
		*	@param	bb		The ByteBuffer from which to read the property value.
		**/
		private Integer64(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			this.property = bb.getLong();
		}

		/** Return a String representation of the property's value.
		*	@return	{@inheritDoc}
		*/
		@Override
		public String value()
		{
			return String.format("0x%016x", property);
		}
	}

	/** Encapsulate a time-date property. */
	private static class Time extends Property
	{
		/** The value of the time-date property */
		private java.util.Date time;

		/** Create a representation of a time-and-date property.
		*	@param	propertyTag	{@inheritDoc}
		*	@param	propertyName	{@inheritDoc}
		*	@param	propertyType	{@inheritDoc}
		*	@param	flags		{@inheritDoc}
		*	@param	bb		The ByteBuffer from which to read the property value.
		**/
		private Time(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			time = (java.util.Date)DataType.timeReader.read(bb);
		}

		/** Return a String representation of the property's value.
		*	@return	{@inheritDoc}
		*/
		@Override
		public String value()
		{
			return time.toString();
		}
	}

	/** Encapsulate a variable-width, or fixed width of more than 4-bytes wide, property. */
	private static class VariableWidth extends Property
	{
		/** The length of the property */
		private int length;

		/** The value of attachment method property for an attachment.
		*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxmsg/bac41dfb-c824-4e3c-9b5e-b61106f6739f">MS-OXMSG Section 2.4.2.2: Variable Length Property or Multiple-Valued Property Entry</a>
		*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxcmsg/252923d6-dd41-468b-9c57-d3f68051a516">MS-OXCMSG Section 2.2.2.9: PidTagAttachMethod Property</a>
		*/
		private int attachmentTypeFlag;

		/** Create a representation of a variable-width, or fixed width with more than 4 bytes, property.
		*	@param	propertyTag	{@inheritDoc}
		*	@param	propertyName	{@inheritDoc}
		*	@param	propertyType	{@inheritDoc}
		*	@param	flags		{@inheritDoc}
		*	@param	bb		The ByteBuffer from which to read the property value.
		**/
		private VariableWidth(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			length = bb.getInt();
			attachmentTypeFlag = bb.getInt();
		}

		/** Return a String representation of the property's value.
		*	@return	{@inheritDoc}
		*/
		@Override
		public String value()
		{
			return Integer.toString(length);
		}
	}

	/** Create a Property from the given ByteBuffer, advancing the position so the next property can be read.
	*	@param	bb		The ByteBuffer to read the Property from.
	*	@param	namedProperties	The file's NamedProperties object to look up non-standard properties
	*	@return	A Property object read out of the given ByteBuffer
	*/
	static Property factory(java.nio.ByteBuffer bb, NamedProperties namedProperties)
	{
		int propertyTag = bb.getInt();
		int propertyId = propertyTag >>> 16;

		String propertyName;
		if (PropertyTags.tags.keySet().contains(propertyId)) {
			propertyName = PropertyTags.tags.get(propertyId);
//		} else if (PropertyLIDs.lids.keySet().contains(propertyId)) {
//			propertyName = PropertyLIDs.lids.get(propertyId);
		} else if ((propertyId & 0x8000) != 0) {
			int propertyIndex = propertyId & 0x7fff;
			propertyName = namedProperties.getPropertyName(propertyIndex);
		} else {
			propertyName = String.format("Not found: 0x%04x", propertyId);
		}
		int flags = bb.getInt();

		switch (propertyTag & 0x0000ffff)
		{
			case DataType.BINARY:
				return new VariableWidth(propertyTag, propertyName, "Binary", flags, bb);

			case DataType.BOOLEAN:
				return new Boolean(propertyTag, propertyName, "Boolean", flags, bb);

			case DataType.INTEGER_32:
				return new Integer32(propertyTag, propertyName, "32-bit Integer", flags, bb);

			case DataType.INTEGER_64:
				return new Integer64(propertyTag, propertyName, "64-bit Integer", flags, bb);

			case DataType.STRING:
				return new VariableWidth(propertyTag, propertyName, "String", flags, bb);

			case DataType.TIME:
				return new Time(propertyTag, propertyName, "Time", flags, bb);

			default:
				return new Integer64(propertyTag, propertyName, "Unrecognized", flags, bb);
		}
	}
}
