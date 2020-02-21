package io.github.jmcleodfoss.msg;

/** Convenience class for propogating ordered KVP data to client applications. */
public class KVPArray extends java.util.ArrayList<KVPEntry>
{
	/** Add a new KVP pair to the end of the list.
	*	@param	k	The key for the entry
	*	@param	v	The value
	*/
	void add(String k, String v)
	{
		super.add(new KVPEntry(k, v));
	}
}
