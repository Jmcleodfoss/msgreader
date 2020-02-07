package io.github.jmcleodfoss.msg;

/** The ClassID class holds a class ID, which specifies the format of the corresponding data.
*/
class ClassId {

	/** The size of a ClassId, in bytes */
	static final int SIZE = 16;

	/** The null ClassId (used for unassigned or unknown ClassIds). */
	static final ClassId CLASSID_NULL = new ClassId(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

	/** The ClassId data */
	final byte[] classId;

	/** Create a ClassId from the bytes in the given array.
	*
	*	@param	arr	The array of bytes from which to construct the ClassId
	*/
	ClassId(byte[] arr)
	{
		classId = new byte[SIZE];
		for (int i = 0; i < SIZE; ++i)
			classId[i] = arr[i];
	}

	/**	Compare two ClassId.
	*
	*	@param	o	The other ClassId to check.
	*
	*	@return	false if the other ClassId differs from this one, true if it is the same as this one.
	*/
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof ClassId))
			return false;

		if (o == null)
			return this == null;

		final ClassId classId = (ClassId)o;
		for (int i = 0; i < this.classId.length; ++i) {
			if (this.classId[i] != classId.classId[i])
				return false;
		}

		return true;
	}

	/**	Calculate hashcode.
	*
	*	@return	Hashcode for the ClassId.
	*/
	@Override
	public int hashCode()
	{
		int hashcode = 0;
		for (int i = 0; i < classId.length; ++i) {
			hashcode += classId[i];
		}

		return hashcode;
	}

	/**	Obtain a string representation of this Class ID
	*
	*	@return	A string describing the Class ID in "Canonical" format (0x000000000000 0x00000000 0x0000000000).
	*/
	@Override
	public String toString()
	{
		final int[] blockOffsets = {4, 6, 8, 10, SIZE}; 
		String s = new String("");

		int i = 0;
		for (int b = 0; b < blockOffsets.length; ++b) {
			if (b > 0)
				s += "-";

			for (; i < blockOffsets[b]; ++i) 
				s += String.format("%02x", classId[i] & 0xff);
		}

		return s;
	}
}
