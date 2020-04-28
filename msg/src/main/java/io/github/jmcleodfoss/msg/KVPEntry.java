package io.github.jmcleodfoss.msg;

/** Convenience wrapper for a key-value pair. */
public class KVPEntry<K, V> extends java.util.AbstractMap.SimpleImmutableEntry<K, V>
{
	/** Construct a new KVP pair.
	*	@param	k	The key for the entry
	*	@param	v	The value
	*/
	KVPEntry(K k, V v)
	{
		super(k, v);
	}
}
