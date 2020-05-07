package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;

/** Tab displaying the double-indirect file allocation table (DIFAT).
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/0afa4e43-b18f-432a-9917-4f276eca7a73">MS-CFB Section 2.5: Compound File DIFAT Sectors</a>
*/
class DIFAT extends KVPTableTab<Integer, Integer>
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "difat.main.tabname";
	static private final String KEY_HEADING = "difat.display.key-heading";
	static private final String VALUE_HEADING = "difat.display.value-heading";

	/** Create the DIFAT display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	DIFAT(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE), localizer.getText(KEY_HEADING), localizer.getText(VALUE_HEADING));
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
