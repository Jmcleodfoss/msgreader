package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;

/** Tab displaying the file allocation table (FAT).
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/30e1013a-a0ff-4404-9ccf-d75d835ff404">MS-CFB Section 2.3: Compound File FAT Sectors</a>
*/
class FAT extends KVPTableTab<String, String>
{
	/* Properties for the tab name and table column headings */
	static private final String PROPNAME_FAT_TAB_TITLE = "FAT";
	static private final String PROPNAME_FAT_COL1_HEADING = "FATKVPKey";
	static private final String PROPNAME_FAT_COL2_HEADING = "FATKVPValue";

	/** Create the FAT display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	FAT(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_FAT_TAB_TITLE), localizer.getText(PROPNAME_FAT_COL1_HEADING), localizer.getText(PROPNAME_FAT_COL2_HEADING), true);
	}

	/** Update the FAT display.
	*	@param	msg	The msg object for the file we are displaying
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update(MSG msg, LocalizedText localizer)
	{
		if (msg == null)
			return;

		super.update(msg.fatData(), localizer);
	}
}
