package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;

/** Tab displaying the file allocation table (FAT).
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/30e1013a-a0ff-4404-9ccf-d75d835ff404">MS-CFB Section 2.3: Compound File FAT Sectors</a>
*/
class FAT extends KVPTableTab<String, String>
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "fat.main.tabname";
	static private final String KEY_HEADING = "fat.display.key-heading";
	static private final String VALUE_HEADING = "fat.display.value-heading";

	/** Create the FAT display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	FAT(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE), localizer.getText(KEY_HEADING), localizer.getText(VALUE_HEADING), true);
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
