package io.github.jmcleodfoss.msg;

/** Expose data from EntryStreamEntry objects to client applications */
public class EntryStreamEntryData {
	public final int nameIdentifierOrStringOffset;
	public final short propertyIndex;
	public final short guidIndex;

	EntryStreamEntryData(int nameIdentifierOrStringOffset, short propertyIndex, short guidIndex)
	{
		this.nameIdentifierOrStringOffset = nameIdentifierOrStringOffset;
		this.propertyIndex = propertyIndex;
		this.guidIndex = guidIndex;
	}
}
