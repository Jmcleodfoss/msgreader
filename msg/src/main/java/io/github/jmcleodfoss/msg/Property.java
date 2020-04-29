package io.github.jmcleodfoss.msg;

/** Class for passing fixed-width property data to client applications */
public abstract class Property
{
	public final int propertyTag;
	public final int flags;
	public final String propertyName;
	public final String propertyType;

	Property(int propertyTag, String propertyName, String propertyType, int flags)
	{
		this.propertyTag = propertyTag;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.flags = flags;
	}

	public abstract String value();

	private static class Boolean extends Property
	{
		boolean property;

		Boolean(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			this.property = bb.get() != 0;

			// Skip remaining bytes for this entry
			bb.position(bb.position()+7);
		}

		@Override
		public String value()
		{
			return java.lang.Boolean.toString(property);
		}
	}

	private static class Integer32 extends Property
	{
		int property;

		Integer32(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			this.property = bb.getInt();

			// Skip remaining bytes for this entry
			bb.position(bb.position()+4);
		}

		@Override
		public String value()
		{
			return String.format("0x%08x", property);
		}
	}

	private static class Integer64 extends Property
	{
		long property;

		Integer64(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			this.property = bb.getLong();
		}

		@Override
		public String value()
		{
			return String.format("0x%016x", property);
		}
	}

	private static class Time extends Property
	{
		java.util.Date time;

		Time(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			time = (java.util.Date)DataType.timeReader.read(bb);
		}

		@Override
		public String value()
		{
			return time.toString();
		}
	}

	private static class VariableWidth extends Property
	{
		int length;
		int attachmentTypeFlag;

		VariableWidth(int propertyTag, String propertyName, String propertyType, int flags, java.nio.ByteBuffer bb)
		{
			super(propertyTag, propertyName, propertyType, flags);
			length = bb.getInt();
			attachmentTypeFlag = bb.getInt();
		}

		@Override
		public String value()
		{
			return Integer.toString(length);
		}
	}

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
