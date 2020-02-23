package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;

import java.util.List;
import javafx.application.Application;
import javafx.application.Application.Parameters;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

public class MsgViewer extends javafx.application.Application
{
	static private final String PROPNAME_SECTOR_TAB_TITLE = "Sectors";
	static private final String PROPNAME_MINISTREAM_TAB_TITLE = "Ministream";
	static private final String PROPNAME_DIRECTORY_TAB_TITLE = "Directory";

	String filename;

	MSG file;

	LocalizedText localizer;

	TabPane tabs;

	Header header;
	DIFAT difat;
	FAT fat;
	Tab sectors;
	Tab miniStream;
	Tab directory;

	public MsgViewer()
	{
	}

	public void start(javafx.stage.Stage stage)
	{
		localizer = new LocalizedText();

		header = new Header(localizer);
		difat = new DIFAT(localizer);
		fat = new FAT(localizer);
		sectors = new Tab(localizer.getText(PROPNAME_SECTOR_TAB_TITLE));
		miniStream = new Tab(localizer.getText(PROPNAME_MINISTREAM_TAB_TITLE));
		directory = new Tab(localizer.getText(PROPNAME_DIRECTORY_TAB_TITLE));
		
		tabs = new TabPane(header, difat, fat, sectors, miniStream, directory);
		tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		Parameters parameters = getParameters();
		List<String> args = parameters.getRaw();
		if (args.size() > 0){
			filename = args.get(0);
		}

		if (filename != null) {
			stage.setTitle(filename);
			try {
				file = new MSG(filename);
			} catch (Exception e){
				file = null;
				filename = null;
			}
			if (file != null)
				update(file);
		} else {
			stage.setTitle("msg Viewer application");
		}

		AnchorPane ap = new AnchorPane(tabs);
		ap.setTopAnchor(tabs, 0.0);
		ap.setBottomAnchor(tabs, 0.0);
		ap.setLeftAnchor(tabs, 0.0);
		ap.setRightAnchor(tabs, 0.0);

		Scene scene = new Scene(ap, 800, 600);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	private void update(MSG msg)
	{
		header.update(msg, localizer);
		difat.update(msg, localizer);
		fat.update(msg, localizer);
	}

	static public void main(String[] args)
	{
		launch(MsgViewer.class, args);
	}
}
