package io.github.jmcleodfoss.msg;

class Sector {
	static final int MAXREGSEC = 0xfffffffa;
	static final int RESERVED = 0xfffffffb;
	static final int DIFSECT = 0xfffffffc;
	static final int FATSECT = 0xfffffffd;
	static final int ENDOFCHAIN = 0xfffffffe;
	static final int FREESECT = 0xffffffff;

	static String getDescription(int sectorId)
	{
		switch (sectorId){
			case RESERVED: return "Reserved";
			case DIFSECT: return "DIFAT";
			case FATSECT: return "FAT";
			case ENDOFCHAIN: return "End of Chain";
			case FREESECT: return "Free";
			case MAXREGSEC: return "Max Regular Sector";
		}

		return "Regular Sector " + String.format("0x%08x", sectorId);
	}

	final int sectorSize;

	Sector(Header header)
	{
		this.sectorSize = header.sectorSize;
	}

	byte[] read(java.nio.MappedByteBuffer mbb, int sectorIndex)
	{
		byte data[] = new byte[sectorSize];
		mbb.position((sectorIndex+1)*sectorSize);
		mbb.get(data);
		return data;
	}

	public static void main(final String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.Sector msg-file [log-level]");
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
			Sector s = new Sector(header);
			byte[] sector = s.read(mbb, 0);
			for (int i = 0; i < header.sectorSize; i+=4)
				System.out.printf("%02d: 0x%02x 0x%02x 0x%02x 0x%02x\n", i/4, sector[i], sector[i+1], sector[i+2], sector[i+3]);
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}


