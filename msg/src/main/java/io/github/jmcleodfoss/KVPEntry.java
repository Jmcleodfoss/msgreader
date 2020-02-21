package io.github.jmcleodfoss.msg;

/** Convenience wrapper for a key-value pair. */
public class KVPEntry extends java.util.AbstractMap.SimpleImmutableEntry<String, String>
{
	/** Construct a new KVP pair.
	*	@param	k	The key for the entry
	*	@param	v	The value
	*/
	KVPEntry(String k, String v)
	{
		super(k, v);
	}
}
