package io.github.jmcleodfoss.msg;

/** Expose data from DataWithIndexAndKind objects to client applications */
public class NamedPropertyEntry {
	public final int nameIdentifierOrStringOffset;
	public final short propertyIndex;
	public final short guidIndex;

	NamedPropertyEntry(int nameIdentifierOrStringOffset, short propertyIndex, short guidIndex)
	{
		this.nameIdentifierOrStringOffset = nameIdentifierOrStringOffset;
		this.propertyIndex = propertyIndex;
		this.guidIndex = guidIndex;
	}
}
