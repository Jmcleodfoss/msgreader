package io.github.jmcleodfoss.msg;

/** The DataType class represents data types within a PST file as well as PST file properties.
*/
abstract class DataType {
	/** The number of bytes in an integer. */
	static final int SIZEOF_INT = Integer.SIZE / Byte.SIZE;

	/** The number of bytes in a long. */
	static final int SIZEOF_LONG = Long.SIZE / Byte.SIZE;

	/** The character encoding used for Unicode data.  */
	private static final String CHARSET_WIDE = new String("UTF-16LE");

	/** Create an object of type DataType. */
	protected DataType()
	{
	}

	/** Create a String describing an object of the type read in by this class.
	*	@param	o	The object to create a String representation of.
	*	@return	A String describing the object.
	*/
	abstract String makeString(final Object o);

	/** Read in an object of the target type.
	*	@param	byteBuffer	The incoming data stream from which to read the object.
	*	@return	The object read from the data stream.
	*/
	abstract Object read(java.nio.ByteBuffer byteBuffer);

	/** Get the size of the object read in in this class.
	*	@return	The size, in bytes, of the object read in by this class, if fixed (constant), otherwise 0.
	*/
	abstract int size();

	/** The SizedObject class contains functionality shared by manipulators for objects with known client-defined sizes. */
	private abstract static class SizedObject extends DataType {

		/** The size of this object read in. */
		protected final int size;

		/** Construct foundation for a manipulator of an object with known size.
		*	@param	size	The number of bytes in this object.
		*/
		protected SizedObject(final int size)
		{
			super();
			this.size = size;
		}

		/** Obtain the size of this object in the PST file.
		*	@return	The size of this object in the PST file, in bytes.
		*/
		int size()
		{
			return size;
		}
	}

	/** The SizedByteArray class is used to read in and display an array of bytes whose size is known. */
	static class SizedByteArray extends SizedObject {

		/** Create a reader/display manipulator for an array of bytes of known size.
		*	@param	size	The number of bytes in the array.
		*/
		SizedByteArray(final int size)
		{
			super(size);
		}

		/** Create a String describing a array of bytes.
		*	@param	o	The array of bytes to display.
		*	@return	A String showing the bytes in the array in hexadecimal.
		*/
		String makeString(final Object o)
		{
			byte[] a = (byte[])o;
			return ByteUtil.createHexByteString(a);
		}

		/** Read in an array of bytes of the given size.
		*	@param	byteBuffer	The incoming data stream to read from. Note that this is entirely consumed.
		*	@param	size		The number of bytes to read in
		*	@return	The array of bytes read in from the incoming data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer, final int size)
		{
			byte arr[] = new byte[size];
			byteBuffer.get(arr);
			return arr;
		}

		/** Read in an array of bytes.
		*	@param	byteBuffer	The incoming data stream to read from. Note that this is entirely consumed.
		*	@return	The array of bytes read in from the incoming data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			return read(byteBuffer, size);
		}
	}

	/** The ByteArray class described an array of bytes taking up the remainder of byteBuffer. */
	private static class ByteArray extends SizedByteArray {

		/** Construct an manipulator for a byte array. */
		private ByteArray()
		{
			super(0);
		}

		/** Read in an array of bytes.
		*	@param	byteBuffer	The incoming data stream to read from. Note that this is entirely consumed.
		*	@return	The array of bytes read in from the incoming data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			return read(byteBuffer, byteBuffer.remaining());
		}
	}

	/** The reader for generic arrays of bytes. */
	private static final ByteArray byteArrayReader = new ByteArray();
	
	/** The Integer8 data type describes how to manipulate an 8-bit integer. */
	private static class Integer8 extends DataType {

		/** Construct a manipulator for an 8-bit integer. */
		private Integer8()
		{
			super();
		}

		/** Create a String from the passed Byte object.
		*	@param	o	The Byte object to display.
		*	@return	A String representation of the Byte object (in hexadecimal).
		*/
		String makeString(final Object o)
		{
			return Integer.toHexString((Byte)o & 0xff);
		}

		/** Read in an 8-bit integer from the data stream.
		*	@param	byteBuffer	The incoming data stream from which to read the 8-bit integer.
		*	@return	A Byte object corresponding to the 8-bit integer read in from the data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			return (Byte)byteBuffer.get();
		}

		/** Obtain the size of an 8-bit integer in a PST file.
		*	@return	The size of an 8-bit integer in the PST file, in bytes.
		*/
		int size()
		{
			return 1;
		}
	}

	/** The manipulator for reading and displaying 8-bit integers. */
	static final Integer8 integer8Reader = new Integer8();

	/** The Integer16 data type describes how to read and display a 16-bit integer. */
	private static class Integer16 extends DataType {

		/** Construct a manipulator for an PST PtypInteger32 data type. */
		private Integer16()
		{
			super();
		}

		/** Create a String from the passed Short object.
		*	@param	o	The Short object to display.
		*	@return	A String representation of the Short object (in hexadecimal).
		*/
		String makeString(final Object o)
		{
			return Integer.toHexString((Short)o & 0xffff);
		}

		/** Read in a 16-bit integer from the data stream.
		*	@param	byteBuffer	The incoming data stream from which to read the 16-bit integer.
		*	@return	A Short object corresponding to the 16-bit integer read in from the data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			return (Short)byteBuffer.getShort();
		}

		/** Obtain the size of a 16-bit integer.
		*	@return	The size of a 16-bit integer in the PST file, in bytes.
		*/
		int size()
		{
			return 2;
		}
	}

	/** The reader/display manipulator for 16-bit integers in the PST file. */
	static final Integer16 integer16Reader = new Integer16();

	/** The Integer32 data type describes a 32-bit integer. */
	private static class Integer32 extends DataType {

		/** Construct a manipulator for an PST PtypInteger32 data type. */
		Integer32()
		{
			super();
		}

		/** Create a String from the passed Integer object.
		*	@param	o	The Integer object to display.
		*	@return	A String representation of the Integer object (in hexadecimal).
		*/
		String makeString(final Object o)
		{
			return Integer.toHexString((Integer)o);
		}

		/** Read in a 32-bit integer from the data stream.
		*	@param	byteBuffer	The incoming data stream from which to read the 32-bit integer.
		*	@return	An Integer object corresponding to the 32-bit integer read in from the data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			return (Integer)byteBuffer.getInt();
		}

		/** Obtain the size of a 32-bit integer in a PST file.
		*	@return	The size of a 32-bit integer in the PST file, in bytes.
		*/
		int size()
		{
			return 4;
		}
	}

	/** The reader/display manipulator for 32-bit integers in the PST file. */
	static final Integer32 integer32Reader = new Integer32();

	/** The Integer64 data type described a 64-bit integer. */
	private static class Integer64 extends DataType {

		/** Construct a manipulator for an PST PtypInteger32 data type. */
		private Integer64()
		{
			super();
		}

		/** Create a String from the passed Long object.
		*	@param	o	The Long object to display.
		*	@return	A String representation of the Long object (in hexadecimal).
		*/
		String makeString(final Object o)
		{
			return Long.toHexString((Long)o);
		}

		/** Read in a 64-bit integer from the data stream.
		*	@param	byteBuffer	The incoming data stream from which to read the 64-bit integer.
		*	@return	A Long object corresponding to the 64-bit integer read in from the data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			return (Long)byteBuffer.getLong();
		}

		/** Obtain the size of a 64-bit integer in a PST file.
		*	@return	The size of a 64-bit integer in the PST file, in bytes.
		*/
		int size()
		{
			return 8;
		}
	}

	/** The reader/display manipulator for 64-bit integers. */
	static final Integer64 integer64Reader = new Integer64();

	/** The UnicodeString class reads in a UTF-16 string of a given size. */
	static class UnicodeString extends SizedObject {
		/** Construct an manipulator for a UTF-16 String.
		*	@param	size	The number of bytes in the UTF-16 string.
		*/
		UnicodeString(final int size)
		{
			super(size);
		}

		/** Create a String representation of a String (to be consistent with other data types).
		*	@param	o	The String to display.
		*	@return	The given String.
		*/
		String makeString(final Object o)
		{
			return (String)o;
		}

		/** Read in a String from the data stream.
		*	@param	byteBuffer	The incoming data stream from which to read the data.
		*	@return	A String corresponding to the Boolean read in from the data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			byte arr[] = new byte[size];
			byteBuffer.get(arr);
			return createString(arr);
		}
	}

	static String createString(byte[] arr)
	{
		try {
			return new String(arr, CHARSET_WIDE);
		} catch (java.io.UnsupportedEncodingException e){
			// UTF-16 should be supported everywhere by now.
			return "";
		}
	}

	/** The Time class represents an MS Time object. It is converted on input to a standard Java Date object.
	*	@see	"[MS-OXDATA] Data Structures v20101026, Section 2.11.1"
	*	@see	<a href="http://msdn.microsoft.com/en-us/library/ee157583.aspx">Property Data Types (MSDN)</a>
	*/
	private static class Time extends DataType {

		/** The base for MS time, which is measured in hundreds of nanosecondss since January 1, 1601. */
		private static final java.util.Date PST_BASE_TIME = initBaseTime();

		/** The format to use when converting time objects to strings. */
		private static final java.text.SimpleDateFormat OUTPUT_FORMAT = new java.text.SimpleDateFormat("MMMM dd, yyyy hh:mm:ss");

		/** Initialize the base time; exit on exception.
		*	@return	A Date object for the base time used by PST files.
		*/
		private static java.util.Date initBaseTime()
		{
			try {
				final java.text.SimpleDateFormat PST_BASE_FORMAT = new java.text.SimpleDateFormat("MMMM dd, yyyy");
				return PST_BASE_FORMAT.parse("January 1, 1601");
			} catch (final java.text.ParseException e) {
				e.printStackTrace(System.out);
				System.exit(1);
			}
			return new java.util.Date();
		}

		/** Create a time reader/display manipulation object. */
		private Time()
		{
			super();
		}

		/** Create a String representation of a Date.
		*	@param	o	The Date to display.
		*	@return	A String representation of the given object, formatted according to {@link #OUTPUT_FORMAT}.
		*	@see	#OUTPUT_FORMAT
		*/
		String makeString(final Object o)
		{
			return OUTPUT_FORMAT.format((java.util.Date)o);
		}

		/** Read in an MS time from the data stream.
		*	@param	byteBuffer	The incoming data stream from which to read the time.
		*	@return	A Java Date object corresponding to the MS time read from the data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			long hundred_ns = byteBuffer.getLong();
			long ms = hundred_ns/10000;
			ms += PST_BASE_TIME.getTime();
			return new java.util.Date(ms);
		}

		/** Obtain the size in bytes of an MS time object in a PST file.
		*	@return	The size of an MS time object in a PST file.
		*/
		int size()
		{
			return 8;
		}
	}

	/** A reader/display manipulation object for times in PST files. */
	static final Time timeReader = new Time();

	/** The SizedInteger16Array class described how to read and display an array of 16-bit integers whose size is known. */
	private static class SizedInt16Array extends SizedObject {

		/** Create a reader/display manipulator for an array of 16-bit integers of known size.
		*	@param	size	The number of 16-bit integers in the array.
		*/
		SizedInt16Array(final int size)
		{
			super(size);
		}

		/** Create a String describing an array of shorts array.
		*	@param	o	The array of shorts to display.
		*	@return	A String showing the shorts in the array.
		*/
		String makeString(final Object o)
		{
			short[] a = (short[])o;
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < size; ++i) {
				if (i > 0)
					s.append(' ');
				s.append(a[i]);
			}
			return s.toString();
		}

		/** Read in an array of 16-bit integers of the predetermined size.
		*	@param	byteBuffer	The incoming data stream to read from.
		*	@return	The array of shorts read in from the incoming data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			short arr[] = new short[size];
			for (int i = 0; i < size; ++i)
				arr[i] = byteBuffer.getShort();
			return arr;
		}

		/** Return the size of the array of 16-bit integers.
		*	@return	The size of the array of 16-bit integers in the PST file.
		*/
		int size()
		{
			return size*2;
		}
	}

	/** Datatype for GUID class. */
	private static class GUID extends DataType {

		/** The size of a GUID. */
		static final int SIZE = GUID.SIZE;

		/** Create a reader / display object for GUIDs. */
		GUID()
		{
			super();
		}

		/** Create a String describing the GUID
		*	@param	o	The GUID to display.
		*	@return	A String showing the GUID.
		*/
		String makeString(final Object o)
		{
			return ((io.github.jmcleodfoss.msg.GUID)o).toString();
		}

		/** Read in GUID
		*	@param	byteBuffer	The incoming data stream to read from.
		*	@return	The GUID read in from the incoming data stream.
		*/
		Object read(java.nio.ByteBuffer byteBuffer)
		{
			byte arr[] = new byte[SIZE];
			byteBuffer.get(arr);
			io.github.jmcleodfoss.msg.GUID classId = new io.github.jmcleodfoss.msg.GUID(arr);
			return classId;
		}

		/** Return the size of GUID object
		*	@return	The size of a GUID
		*/
		int size()
		{
			return SIZE;
		}
	}

	/** A reader/display manipulation object for GUIDs. */
	static final GUID classIdReader = new GUID();
}
