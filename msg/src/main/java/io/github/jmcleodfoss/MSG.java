package io.github.jmcleodfoss.msg;

/** Consolidated interface for reading MSG files (this will work for other CFB
 * files but has special handling for some information found only in MSG files).
 */
public class MSG
{
	/**	The data stream for the file. */
	private java.io.FileInputStream stream;

	/**	The FileChannel of the data stream, used to jump around the file. */
	private java.nio.channels.FileChannel fc;

	/**	The file, as a memory-mapped byte file. */
	private java.nio.MappedByteBuffer mbb;

	/**	The header */
	public Header header;

	/**	The DIFAT */
	public DIFAT difat;

	/**	The DAT */
	public FAT fat;

	/**	The directory */
	public Directory directory;

	/**	The Mini FAT */
	public MiniFAT miniFAT;

	/**	The named properties */
	public NamedProperties namedProperties;

	/**	Create a FileChannel for the given filename and read in the
 	*	header, DIFAT, etc.
	*	@param	fn	The name of the file to read.
	*	@throws NotCFBFileException	The input stream does not contain a PST file.
	* 	@throws java.io.IOException	There was an I/O error reading the input stream.
	*/
	public MSG(String fn)
	throws
		NotCFBFileException,
		java.io.IOException
	{
		stream = new java.io.FileInputStream(fn);
		fc = stream.getChannel();

		mbb = fc.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, fc.size());
		mbb.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		header = new Header(mbb);
		difat = new DIFAT(mbb, header);
		fat = new FAT(mbb, header, difat);
		directory = new Directory(mbb, header, fat);	
		miniFAT = new MiniFAT(mbb, header, fat, directory);
		namedProperties = new NamedProperties(mbb, header, fat, directory, miniFAT);
	}

	/**	Close the file.
	* 	@throws java.io.IOException	There was a problem closing the file.
	*/
	public void close()
	throws
		java.io.IOException
	{
		fc.close();
	}
}
