package io.github.jmcleodfoss.msg;

/** The file signature. */
class HeaderSignature {

	/** The bytes which form the signature, in the order in which they are documented. */
	static final byte[] SIGNATURE_BYTES = {(byte)0xd0, (byte)0xcf, (byte)0x11, (byte)0xe0, (byte)0xa1, (byte)0xb1, (byte)0x1a, (byte)0xe1};

	/** The header signature as an 8-byte (long) value. */
	static final long SIGNATURE = io.github.jmcleodfoss.msg.ByteUtil.makeLongLE(SIGNATURE_BYTES);

	/** Validate that the passed signature is a valid header signature.
	*	@param	signature	The signature to be checked
	*	@throws	NotCFBFileException	The signature does not match the expected value.
	*/
	static void validate(long signature)
	throws
		NotCFBFileException
	{
		if (signature != SIGNATURE){
			throw new NotCFBFileException();
		}
	}
}
