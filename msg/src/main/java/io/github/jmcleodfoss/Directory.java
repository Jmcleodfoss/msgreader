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
			for (int i = 0; i < header.sectorSize / DirectoryEntry.SIZE; ++i) {
			 	entries.add(DirectoryEntry.factory(byteBuffer)); 
			}
		}
	}

	void addSiblings(java.util.ArrayList<Integer> siblings, int childIndex)
	{
		DirectoryEntry child = entries.get(childIndex);
		if (child.leftSiblingId != Sector.FREESECT)
			addSiblings(siblings, child.leftSiblingId);
		siblings.add(childIndex);
		if (child.rightSiblingId != Sector.FREESECT)
			addSiblings(siblings, child.rightSiblingId);
	}

	java.util.ArrayList<Integer> getChildren(int parentIndex)
	{
		java.util.ArrayList<Integer> children = new java.util.ArrayList<Integer>();
		int childIndex = entries.get(parentIndex).childId;
		if (childIndex != Sector.FREESECT){
			addSiblings(children, childIndex);
		}
		return children;
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
			int i = 0;
			while (iterator.hasNext())
				System.out.printf("0x%02x: %s\n", i++, iterator.next().toString());

			System.out.println("\n");
			for (i = 0; i < directory.entries.size(); ++i){
				java.util.ArrayList<Integer> children = directory.getChildren(i);
				if (children.size() > 0){
					System.out.printf("Children of 0x%02x:\n", i);
					java.util.Iterator<Integer> childIterator = children.iterator();
					while (childIterator.hasNext())
						System.out.println("\t" + childIterator.next());
				}
			}
		} catch (final Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
