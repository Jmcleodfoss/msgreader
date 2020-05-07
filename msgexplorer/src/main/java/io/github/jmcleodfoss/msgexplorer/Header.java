package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;

/** Tab displaying the file header
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/05060311-bfce-4b12-874d-71fd4ce63aea">MS-CFB Section 2.2: Compound File Header</a>
*/
class Header extends KVPTableTab<String, String>
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "header.main.tabname";
	static private final String KEY_HEADING = "header.display.key-heading";
	static private final String VALUE_HEADING = "header.display.value-heading";

	/** Create the header display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	Header(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE), localizer.getText(KEY_HEADING), localizer.getText(VALUE_HEADING));
	}

	/** Update the header display.
	*	@param	msg	The msg object for the file we are displaying
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update(MSG msg, LocalizedText localizer)
	{
		if (msg == null)
			return;

		super.update(msg.headerData(), localizer);
	}
}
