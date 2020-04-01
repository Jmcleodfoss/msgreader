package io.github.jmcleodfoss.msg;

/** Convenience class for propagating ordered KVP data to client applications. */
public class KVPArray<K, V> extends java.util.ArrayList<KVPEntry<K, V>>
{
	/** Add a new KVP pair to the end of the list.
	*	@param	k	The key for the entry
	*	@param	v	The value
	*/
	void add(K k, V v)
	{
		super.add(new KVPEntry<K, V>(k, v));
	}
}
