package io.github.jmcleodfoss.msg;

/** Constants and classes for dealing with sectors */
class Sector {

	/** The maximum regular sector index */
	static final int MAXREGSEC = 0xfffffffa;

	/** A reserved sector */
	static final int RESERVED = 0xfffffffb;

	/** A DIFAT (Double Indirect File Allocation Table) sector */
	static final int DIFSECT = 0xfffffffc;

	/** A FAT (File Allocation Table) sector */
	static final int FATSECT = 0xfffffffd;

	/** The end of a chain of sectors */
	static final int ENDOFCHAIN = 0xfffffffe;

	/** A free (unused) sector */
	static final int FREESECT = 0xffffffff;

	/** Get a description of the given sector ID
	*	@param	sectorId	The sector to describe
	*	@return	A description of the passed sector ID
	*/
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

		return "Regular Sector " + String.format("%d", sectorId);
	}

	/** Get the sector size given the sector shift.
	*   @param	sectorShift	The sector shift value (2 is shifted by this number to get the sector size)
	*/
	static int sectorSize(short sectorShift)
	{
		return 2 << (sectorShift - 1);
	}

	/** Get the offset into the file for the given sector
	*	@param	sectorIndex	The index of the sector to get the offset of
	*	@param	header		The header of this CFB file
	*	@return	The offset into the file that the requested sector begins at.
	*/
	static int offset(int sectorIndex, Header header)
	{
		return (sectorIndex + 1) * header.sectorSize;
	}
}
