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

	static int offset(int sectorIndex, Header header)
	{
		return (sectorIndex + 1) * Header.sectorSize;
	}
}


