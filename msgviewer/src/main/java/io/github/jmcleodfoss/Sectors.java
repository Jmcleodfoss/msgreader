package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.MSG;

import javafx.scene.control.Tab;

class Sectors extends Tab
{
	static private final String PROPNAME_SECTOR_TAB_TITLE = "Sectors";

	Sectors(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_SECTOR_TAB_TITLE));
	}

	void update(MSG file, LocalizedText localizer)
	{
	}
}