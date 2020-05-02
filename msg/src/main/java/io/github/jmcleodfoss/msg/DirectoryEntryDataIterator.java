package io.github.jmcleodfoss.msg;

/** Convert an iterator through {@link DirectoryEntry} objects into an iterator through {@link DirectoryEntryData} objects for use by client applications */
class DirectoryEntryDataIterator implements java.util.Iterator<DirectoryEntryData>
{
	/** The iterator through the entry's children */
	private java.util.Iterator<DirectoryEntry> iterator;

	/** The directory the entries in the iterator belong to. */
	private Directory directory;

	/** The named properties list associated with the entries in the iterator. */
	private NamedProperties namedProperties;

	/** Create an iterator through the entry's children by setting up the local iterator to shadow. */
	DirectoryEntryDataIterator(java.util.Iterator<DirectoryEntry> iterator, Directory directory, NamedProperties namedProperties)
	{
		this.iterator = iterator;
		this.directory = directory;
		this.namedProperties = namedProperties;
	}

	/** Is there another entry in the list?
	*	@return	true if there is another entry, false otherwise
	*/
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	/** Get the next entry as a DirectoryEntryData object
	*	@return	A DirectoryEntryData object for the next entry.
	*/
	public DirectoryEntryData next()
	{
		return new DirectoryEntryData(iterator.next(), directory, namedProperties);
	}
}