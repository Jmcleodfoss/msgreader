package io.github.jmcleodfoss.msg;

class Directory {

	java.util.ArrayList<DirectoryEntry> entries;

	Directory(java.nio.ByteBuffer byteBuffer, Header header, FAT fat)
	throws
		java.io.IOException
	{
		entries = new java.util.ArrayList<DirectoryEntry>();

		java.util.Iterator<Integer> chain = fat.chainIterator(header.firstDirectorySectorLocation);
		while(chain.hasNext()){
			int dirSector = chain.next();
			byteBuffer.position((dirSector+1)*header.sectorSize);
			for (int i = 0; i < header.sectorSize / DirectoryEntry.size(); ++i) {
			 	entries.add(new DirectoryEntry(byteBuffer)); 
			}
		}
	}

	java.util.Iterator<DirectoryEntry> iterator()
	{
		return entries.iterator();
	}

	public static void main(String[] args)
	{
		if (args.length == 0) {
			System.out.println("use:\n\tjava io.github.jmcleodfoss.mst.Directory msg-file [log-level]");
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
			DIFAT difat = new DIFAT(mbb, header);
			FAT fat = new FAT(mbb, header, difat);
			Directory directory = new Directory(mbb, header, fat);	

			java.util.Iterator<DirectoryEntry> iterator = directory.iterator();
			while (iterator.hasNext())
				System.out.println(iterator.next());
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
