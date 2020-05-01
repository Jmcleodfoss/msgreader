package io.github.jmcleodfoss.msg;

/** The Header object is the CFB header.
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
*/
class Header {

	/* The keys used to retrieve data from the DataContainer
	*	@see DataContainer
	*/
	private static final String nm_qwHeaderSignature = "HeaderSignature";
	private static final String nm_HeaderCLSID = "HeaderCLSID";
	private static final String nm_MinorVersion = "MinorVersion";
	private static final String nm_MajorVersion = "MajorVersion";
	private static final String nm_ByteOrder = "ByteOrder";
	private static final String nm_SectorShift = "SectorShift";
	private static final String nm_MiniSectorShift = "MiniSectorShift";
	private static final String nm_NumberOfDirectorySectors = "NumberOfDirectorySectors";
	private static final String nm_NumberOfFATSectors = "NumberOfFATSectors";
	private static final String nm_FirstDirectorySectorLocation = "FirstDirectorySectorLocation";
	private static final String nm_TransactionSignatureNumber = "TransactionSignatureNumber";
	private static final String nm_MiniStreamCutoffSize = "MiniStreamCutoffSize";
	private static final String nm_FirstMiniFATSectorLocation = "FirstMiniFATSectorLocation";
	private static final String nm_NumberOfMiniFATSectors = "NumberOfMiniFATSectors";
	private static final String nm_FirstDIFATSectorLocation = "FirstDIFATSectorLocation";
	private static final String nm_NumberOfDIFATSectors = "NumberOfDIFATSectors";

	/** The fields in a CFB header object.
	*	@see DataDefinition
	*	@see DataType#classIdReader
	*	@see DataType#integer16Reader
	*	@see DataType#integer32Reader
	*	@see DataType#integer64Reader
	*	@see DataType.SizedByteArray
	*/
	private static final DataDefinition[] header_fields = {
		new DataDefinition(nm_qwHeaderSignature, DataType.integer64Reader, true),
		new DataDefinition(nm_HeaderCLSID, DataType.classIdReader, true),
		new DataDefinition(nm_MinorVersion, DataType.integer16Reader, true),
		new DataDefinition(nm_MajorVersion, DataType.integer16Reader, true),
		new DataDefinition(nm_ByteOrder, DataType.integer16Reader, true),
		new DataDefinition(nm_SectorShift, DataType.integer16Reader, true),
		new DataDefinition(nm_MiniSectorShift, DataType.integer16Reader, true),
		new DataDefinition("Reserved", new DataType.SizedByteArray(6)),
		new DataDefinition(nm_NumberOfDirectorySectors, DataType.integer32Reader, true),
		new DataDefinition(nm_NumberOfFATSectors, DataType.integer32Reader, true),
		new DataDefinition(nm_FirstDirectorySectorLocation, DataType.integer32Reader, true),
		new DataDefinition(nm_TransactionSignatureNumber, DataType.integer32Reader, true),
		new DataDefinition(nm_MiniStreamCutoffSize, DataType.integer32Reader, true),
		new DataDefinition(nm_FirstMiniFATSectorLocation, DataType.integer32Reader, true),
		new DataDefinition(nm_NumberOfMiniFATSectors, DataType.integer32Reader, true),
		new DataDefinition(nm_FirstDIFATSectorLocation, DataType.integer32Reader, true),
		new DataDefinition(nm_NumberOfDIFATSectors, DataType.integer32Reader, true),
	};

	/** Size of the header block */
	private static final int SIZE = DataDefinition.size(header_fields);

	/** The data repository (preserved after constructor since we don't
	*   read everything from it that we might want to display).
	*/
	private final DataContainer dc;

	/** The file size */
	final long fileSize;

	/** The sector shift.
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int sectorSize;

	/** The mini sector shift
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int miniSectorSize;

	/** The number of directory sectors
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	private final int numberOfDirectorySectors;

	/** The number of FAT sectors
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int numberOfFATSectors;

	/** The first directory sector location
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int firstDirectorySectorLocation;

	/** The cut-off between storage in the Mini FAT stream or the regular one.
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int miniStreamCutoffSize;

	/** The first mini FAT sector location
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int firstMiniFATSectorLocation;

	/** The number of mini FAT sectors
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int numberOfMiniFATSectors;

	/** The first DIFAT (Double Indirect File Allocation Table) sector location
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int firstDIFATSectorLocation;

	/** The number of DIFAT sectors
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	final int numberOfDIFATSectors;

	/** Read in the header data and save the fields we need for later.
	*	@param	byteBuffer	The data stream from which to read the PST header.
	*	@param	fileSize	The length of the file
	*	@throws	NotCFBFileException	This is not a cfb file.
	*	@throws	java.io.IOException	An I/O error was encountered when reading the msg header.
	*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
	*/
	Header(java.nio.ByteBuffer byteBuffer, long fileSize)
	throws
		NotCFBFileException,
		java.io.IOException
	{
		this.fileSize = fileSize;

		dc = new DataContainer();
		dc.read(byteBuffer, header_fields);

		HeaderSignature.validate((Long)dc.get(nm_qwHeaderSignature));

		sectorSize = Sector.sectorSize((Short)dc.get(nm_SectorShift));
		miniSectorSize = Sector.sectorSize((Short)dc.get(nm_MiniSectorShift));
		numberOfDirectorySectors = (Integer)dc.get(nm_NumberOfDirectorySectors);
		numberOfFATSectors = (Integer)dc.get(nm_NumberOfFATSectors);
		firstDirectorySectorLocation = (Integer)dc.get(nm_FirstDirectorySectorLocation);
		miniStreamCutoffSize = (Integer)dc.get(nm_MiniStreamCutoffSize);
		firstMiniFATSectorLocation = (Integer)dc.get(nm_FirstMiniFATSectorLocation);
		numberOfMiniFATSectors = (Integer)dc.get(nm_NumberOfMiniFATSectors);
		firstDIFATSectorLocation = (Integer)dc.get(nm_FirstDIFATSectorLocation);
		numberOfDIFATSectors = (Integer)dc.get(nm_NumberOfDIFATSectors);
	}

	/** Make header data available to client applications
	*	@return	An array of key-value pairs consisting of a description of the data and the data itself
	*	@see KVPArray
	*/
	KVPArray<String, String> data()
	{
		KVPArray<String, String> l = new KVPArray<String, String>();
		l.add(nm_qwHeaderSignature, String.format("0x%16x", (Long)dc.get(nm_qwHeaderSignature)));
		l.add(nm_HeaderCLSID, ((GUID)dc.get(nm_HeaderCLSID)).toString());
		l.add(nm_MinorVersion, ((Short)dc.get(nm_MinorVersion)).toString());
		l.add(nm_MajorVersion, ((Short)dc.get(nm_MajorVersion)).toString());
		l.add(nm_ByteOrder, String.format("0x%04x", (Short)dc.get(nm_ByteOrder)));
		l.add(nm_SectorShift, ((Short)dc.get(nm_SectorShift)).toString());
		l.add(nm_MiniSectorShift, ((Short)dc.get(nm_MiniSectorShift)).toString());
		l.add(nm_NumberOfDirectorySectors, Integer.toString(numberOfDirectorySectors));
		l.add(nm_NumberOfFATSectors, Integer.toString(numberOfFATSectors));
		l.add(nm_FirstDirectorySectorLocation, Sector.getDescription(firstDirectorySectorLocation));
		l.add(nm_TransactionSignatureNumber, String.format("0x%016x", (Integer)dc.get(nm_TransactionSignatureNumber)));
		l.add(nm_MiniStreamCutoffSize, Integer.toString(miniStreamCutoffSize));
		l.add(nm_FirstMiniFATSectorLocation, Sector.getDescription(firstMiniFATSectorLocation));
		l.add(nm_NumberOfMiniFATSectors, Integer.toString(numberOfMiniFATSectors));
		l.add(nm_FirstDIFATSectorLocation, Sector.getDescription(firstDIFATSectorLocation));
		l.add(nm_NumberOfDIFATSectors, Integer.toString(numberOfDIFATSectors));
		return l;
	}

	/** The number of 4-byte integers (int) which will fit in a sector.
	* 	@return	The number of ints which will fit in a sector.
	*/
	int intsPerSector()
	{
		return sectorSize / DataType.SIZEOF_INT;
	}

	/** The number of sectors in this file
	*	@return	The number of sectors (based on the sector size)
	*/
	int numberOfSectors()
	{
		return (int)(fileSize / sectorSize);
	}

	/** Get the offset into the file for the given sector number (excluding the header sector)
	*   Sector index 0 returns physical sector 1, etc. This function cannot be used to
	*   retrieve the header contents.
	*	@param	sectorNumber	The sector to get the offset of
	*	@return	The offset into the file that the requested sector begins at.
	*/
	int offset(int sectorNumber)
	{
		return (sectorNumber + 1) * sectorSize;
	}

	/** Calculate the size of the header block.
	*	@return	The size of the header for this file.
	*/
	int size()
	{
		return SIZE;
	}

	/** Provide a summary of the header in String form. This is typically used for debugging.
	*	@return	A description of the header.
	*/
	@Override
	public String toString()
	{
		return String.format("sector size 0x%04x mini sector size 0x%04x\n" +
		"dir sectors %d starting at %s\n" +
		"FAT sectors %d\n" +
		"mini FAT sectors %d starting at %s\n" +
		"DIFAT sectors %d starting at %s",
		sectorSize, miniSectorSize,
		numberOfDirectorySectors, Sector.getDescription(firstDirectorySectorLocation),
		numberOfFATSectors,
		numberOfMiniFATSectors, Sector.getDescription(firstMiniFATSectorLocation),
		numberOfDIFATSectors, Sector.getDescription(firstDIFATSectorLocation));
	}

	/** Test this class by reading in the MSG file header and printing it out.
	*	@param	args	The command line arguments to the test application; this is expected to be a MSG file to processed and a log level.
	*/
	public static void main(final String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.Header msg-file [log-level]");
			System.exit(1);
		}
		try {
			java.io.File file = new java.io.File(args[0]);
			java.io.FileInputStream stream = new java.io.FileInputStream(file);
			java.nio.channels.FileChannel fc = stream.getChannel();
			java.nio.MappedByteBuffer mbb = fc.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, fc.size());
			mbb.order(java.nio.ByteOrder.LITTLE_ENDIAN);

			Header header = new Header(mbb, fc.size());
			System.out.println(header);

			System.out.println();
			java.util.Iterator<KVPEntry<String, String>> i = header.data().iterator();
			while (i.hasNext()){
				KVPEntry<String, String> kvp = i.next();
				System.out.println(kvp);
			}
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
