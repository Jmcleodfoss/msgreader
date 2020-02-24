package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.MSG;

class FAT extends KVPTableTab
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

	void update(MSG file, LocalizedText localizer)
	{
		if (file == null)
			return;

		super.update(file.fatData(), localizer);
	}
}
