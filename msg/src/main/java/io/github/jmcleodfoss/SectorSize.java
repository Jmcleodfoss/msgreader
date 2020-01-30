package io.github.jmcleodfoss.msg;

/** Utility class to find the sector size */
class SectorSize {
	/** Get the sector size given the sector shift.
	*   @param	sectorShift	The sector shift value (2 is shifted by this number to get the sector size)
	*/
	static int sectorSize(short sectorShift)
	{
		return 2 << sectorShift;
	}
}
