package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.MSG;

class Header extends KVPTableTab
{
	static private final String PROPNAME_HEADER_TAB_TITLE = "Header";
	static private final String PROPNAME_HEADER_COL1_HEADING = "HeaderKVPKey";
	static private final String PROPNAME_HEADER_COL2_HEADING = "HeaderKVPValue";

	Header(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_HEADER_TAB_TITLE),
			localizer.getText(PROPNAME_HEADER_COL1_HEADING),
			localizer.getText(PROPNAME_HEADER_COL2_HEADING));
	}

	void update(MSG file, LocalizedText localizer)
	{
		if (file == null)
			return;

		super.update(file.headerData(), localizer);
	}
}
