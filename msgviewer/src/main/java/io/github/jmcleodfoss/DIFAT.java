package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.MSG;

class DIFAT extends KVPTableTab<Integer, Integer>
{
	static private final String PROPNAME_DIFAT_TAB_TITLE = "DIFAT";
	static private final String PROPNAME_DIFAT_COL1_HEADING = "DIFATKVPKey";
	static private final String PROPNAME_DIFAT_COL2_HEADING = "DIFATKVPValue";

	DIFAT(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_DIFAT_TAB_TITLE),
			localizer.getText(PROPNAME_DIFAT_COL1_HEADING),
			localizer.getText(PROPNAME_DIFAT_COL2_HEADING));
	}

	void update(MSG msg, LocalizedText localizer)
	{
		if (msg == null)
			return;

		super.update(msg.difatData(), localizer);
	}
}
