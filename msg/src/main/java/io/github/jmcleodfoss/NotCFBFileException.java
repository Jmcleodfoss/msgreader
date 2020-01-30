package io.github.jmcleodfoss.msg;

/**	The NotCFBFileException is thrown when the first eight bytes of the file are not the CFB file signature bytes.
*
*	@see	Header#validate_qwSignature
*/
public class NotCFBFileException extends Exception {

	/**	The serialVersionUID is required because the base class is serializable. */
	private static final long serialVersionUID = 1L;

	/**	Create a NotCFBFileException. */
	NotCFBFileException()
	{
		super();
	}
}

