package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;

class Header extends KVPTableTab<String, String>
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "header.main.tabname";
	static private final String KEY_HEADING = "header.display.key-heading";
	static private final String VALUE_HEADING = "header.display.value-heading";

	Header(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE), localizer.getText(KEY_HEADING), localizer.getText(VALUE_HEADING));
	}

	void update(MSG msg, LocalizedText localizer)
	{
		if (msg == null)
			return;

		super.update(msg.headerData(), localizer);
	}
}
