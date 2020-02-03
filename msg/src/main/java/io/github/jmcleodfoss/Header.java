package io.github.jmcleodfoss.msg;

/** The Header object is the CFB header.
*
* 	MS-CFB Section 2.2, Compound File Header
*/
public class Header {

	private static final String nm_qwHeaderSignature = "HeaderSignature";
	private static final String nm_HeaderCLSID = "HeaderCLSID";
	private static final String nm_MajorVersion = "MajorVersion";
	private static final String nm_ByteOrder = "ByteOrder";
	private static final String nm_SectorShift = "SectorShift";
	private static final String nm_MiniSectorShift = "MiniSectorShift";
	private static final String nm_NumberOfDirectorySectors = "NumberOfDirectorySectors";
	private static final String nm_NumberOfFATSectors = "NumberOfFATSectors";
	private static final String nm_FirstDirectorySectorLocation = "FirstDirectorySectorLocation";
	private static final String nm_FirstMiniFATSectorLocation = "FirstMiniFATSectorLocation";
	private static final String nm_NumberOfMiniFATSectors = "NumberOfMiniFATSectors";
	private static final String nm_FirstDIFATSectorLocation = "FirstMiniFATSectorLocation";
	private static final String nm_NumberOfDIFATSectors = "NumberOfDIFATSectors";
	private static final String nm_DIFATSectorLocations = "DIFATSectorLocationss";

	/** The fields in a CFB header object. */
	private static final DataDefinition[] header_fields = {
		new DataDefinition(nm_qwHeaderSignature, DataType.integer64Reader, true),
		new DataDefinition(nm_HeaderCLSID, new DataType.SizedByteArray(16)),
		new DataDefinition("MinorVersion", DataType.integer16Reader),
		new DataDefinition(nm_MajorVersion, DataType.integer16Reader, true),
		new DataDefinition(nm_ByteOrder, DataType.integer16Reader, true),
		new DataDefinition(nm_SectorShift, DataType.integer16Reader, true),
		new DataDefinition(nm_MiniSectorShift, DataType.integer16Reader, true),
		new DataDefinition("Reserved", new DataType.SizedByteArray(6)),
		new DataDefinition(nm_NumberOfDirectorySectors, DataType.integer32Reader, true),
		new DataDefinition(nm_NumberOfFATSectors, DataType.integer32Reader, true),
		new DataDefinition(nm_FirstDirectorySectorLocation, DataType.integer32Reader, true),
		new DataDefinition("TransactionSignatureNumber", DataType.integer32Reader),
		new DataDefinition("MiniStreamCutoffSize", DataType.integer32Reader),
		new DataDefinition(nm_FirstMiniFATSectorLocation, DataType.integer32Reader, true),
		new DataDefinition(nm_NumberOfMiniFATSectors, DataType.integer32Reader, true),
		new DataDefinition(nm_FirstDIFATSectorLocation, DataType.integer32Reader, true),
		new DataDefinition(nm_NumberOfDIFATSectors, DataType.integer32Reader, true),
		new DataDefinition(nm_DIFATSectorLocations, DataType.multipleInteger32Reader, true),
	};

	/** Size of the header block */
	private static final int SIZE = DataDefinition.size(header_fields);

	/** The major version number. */
	private static short majorVersion;

	/** The byte order. */
	private static short byteOrder;

	/** The sector shift. */
	private static int sectorSize;

	/** The mini sector shift */
	private static int miniSectorSize;

	/** The number of directory sectors */
	private static int numberOfDirectorySectors;

	/** The number of FAT sectors */
	private static int numberOfFATSectors;

	/** The first directory sector location */
	private static int firstDirectorySectorLocation;

	/** The first mini FAT sector location */
	private static int firstMiniFATSectorLocation;

	/** The number of mini FAT sectors */
	private static int numberOfMiniFATSectors;

	/** The first DIFAT (Double Indirect File Allocation Table) sector location */
	private static int firstDIFATSectorLocation;

	/** The number of DIFAT sectors */
	private static int numberOfDIFATSectors;

	/** The first 109 DIFAT sector locations */
	private static int[] difatSectorLocations;

	/** Read in the header data and save the fields we need for later.
	*
	*	@param	byteBuffer	The data stream from which to read the PST header.
	*
	*	@throws	NotCFBFileException	This is not a cfb file.
	*	@throws	java.io.IOException	An I/O error was encountered when reading the msg header.
	*/
	Header(java.nio.ByteBuffer byteBuffer)
	throws
		NotCFBFileException,
		java.io.IOException
	{
		DataContainer dc = new DataContainer();
		dc.read(byteBuffer, header_fields);

		HeaderSignature.validate((long)(Long)dc.get(nm_qwHeaderSignature));
		HeaderSignature.validate((Long)dc.get(nm_qwHeaderSignature));

		majorVersion = (Short)dc.get(nm_MajorVersion);
		byteOrder = (Short)dc.get(nm_ByteOrder);
		sectorSize = SectorSize.sectorSize((Short)dc.get(nm_SectorShift));
		miniSectorSize = SectorSize.sectorSize((Short)dc.get(nm_MiniSectorShift));
		numberOfDirectorySectors = (Integer)dc.get(nm_NumberOfDirectorySectors);
		numberOfFATSectors = (Integer)dc.get(nm_NumberOfFATSectors);
		firstDirectorySectorLocation = (Integer)dc.get(nm_FirstDirectorySectorLocation);
		firstMiniFATSectorLocation = (Integer)dc.get(nm_FirstMiniFATSectorLocation);
		numberOfMiniFATSectors = (Integer)dc.get(nm_NumberOfMiniFATSectors);
		firstDIFATSectorLocation = (Integer)dc.get(nm_FirstDIFATSectorLocation);
		numberOfDIFATSectors = (Integer)dc.get(nm_NumberOfDIFATSectors);
		difatSectorLocations = (int[])dc.get(nm_DIFATSectorLocations);
	}

	/** Calculate the size of the header block.
	*
	*	@return	The size of the header for this file.
	*/
	public int size()
	{
		return SIZE;
	}

	/** Provide a summary of the header in String form. This is typically used for debugging.
	*
	*	@return	A description of the header.
	*/
	@Override
	public String toString()
	{
		return String.format("version 0x%04x\n" +
		"byte order 0x%04x sector size 0x%04x mini sector size 0x%04x\n" +
		"# dir sectors %d starting at %s\n" +
		"# FAT sectors %d\n" +
		"# mini FAT sectors %d starting at %s\n" +
		"# DIFAT sectors %d starting at %s",
		majorVersion,
		byteOrder, sectorSize, miniSectorSize,
 		numberOfDirectorySectors, Sector.getDescription(firstDirectorySectorLocation),
		numberOfFATSectors,
		numberOfMiniFATSectors, Sector.getDescription(firstMiniFATSectorLocation),
		numberOfDIFATSectors, Sector.getDescription(firstDIFATSectorLocation));
	}

	/** Test this class by reading in the MSG file header and printing it out.
	*
	*	@param	args	The command line arguments to the test application.
	*/
	public static void main(final String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.Header msg-file [log-level]");
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

			Header header = new Header(mbb);
			System.out.println(header);
for (int i = 0; i < 110; ++i) System.out.printf("DIFAT %d: 0x%08x\n", i, header.difatSectorLocations[i]);
Sector s = new Sector(header.sectorSize);
s.read(mbb, 1);
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
