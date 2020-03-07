package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.MSG;

import javafx.scene.control.Tab;

class MiniStream extends Tab
{
	static private final String PROPNAME_MINISTREAM_TAB_TITLE = "Ministream";

	MiniStream(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_MINISTREAM_TAB_TITLE));
	}

	void update(MSG msg, LocalizedText localizer)
	{
	}
}
