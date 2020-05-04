package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;

/** Tab displaying the double-indirect file access table (DIFAT).
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/0afa4e43-b18f-432a-9917-4f276eca7a73">MS-CFB Section 2.5: Compound File DIFAT Sectors</a>
*/
class DIFAT extends KVPTableTab<Integer, Integer>
{
	/* Properties for the tab name and table column headings */
	static private final String PROPNAME_DIFAT_TAB_TITLE = "DIFAT";
	static private final String PROPNAME_DIFAT_COL1_HEADING = "DIFATKVPKey";
	static private final String PROPNAME_DIFAT_COL2_HEADING = "DIFATKVPValue";

	/** Create the DIFAT display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	DIFAT(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_DIFAT_TAB_TITLE), localizer.getText(PROPNAME_DIFAT_COL1_HEADING), localizer.getText(PROPNAME_DIFAT_COL2_HEADING));
	}

	/** Update the DIFAT display.
	*	@param	msg	The msg object for the file we are displaying
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update(MSG msg, LocalizedText localizer)
	{
		if (msg == null)
			return;

		super.update(msg.difatData(), localizer);
	}
}
