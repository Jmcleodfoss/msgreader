package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.MSG;

import javafx.scene.control.Tab;

class Directory extends Tab
{
	static private final String PROPNAME_DIRECTORY_TAB_TITLE = "Directory";

	Directory(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_DIRECTORY_TAB_TITLE));
	}

	void update(MSG file, LocalizedText localizer)
	{
	}
}
