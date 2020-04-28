package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;

class FAT extends KVPTableTab<String, String>
{
	static private final String PROPNAME_FAT_TAB_TITLE = "FAT";
	static private final String PROPNAME_FAT_COL1_HEADING = "FATKVPKey";
	static private final String PROPNAME_FAT_COL2_HEADING = "FATKVPValue";

	FAT(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_FAT_TAB_TITLE),
			localizer.getText(PROPNAME_FAT_COL1_HEADING),
			localizer.getText(PROPNAME_FAT_COL2_HEADING),
			true);
	}

	void update(MSG msg, LocalizedText localizer)
	{
		if (msg == null)
			return;

		super.update(msg.fatData(), localizer);
	}
}
