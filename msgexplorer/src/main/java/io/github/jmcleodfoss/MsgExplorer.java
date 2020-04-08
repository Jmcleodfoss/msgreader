package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;

import java.util.List;
import javafx.application.Application;
import javafx.application.Application.Parameters;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class MsgExplorer extends javafx.application.Application
{
	String filename;
	MSG msg;

	LocalizedText localizer;

	BorderPane mainPane;

	MenuBar menuBar;

	TabPane tabs;

	Header header;
	DIFAT difat;
	FAT fat;
	Sectors sectors;
	MiniStream miniStream;
	Directory directory;

	public MsgExplorer()
	{
	}

	public void start(javafx.stage.Stage stage)
	{
		localizer = new LocalizedText();

		header = new Header(localizer);
		difat = new DIFAT(localizer);
		fat = new FAT(localizer);
		sectors = new Sectors(localizer);
		miniStream = new MiniStream(localizer);
		directory = new Directory(localizer);
		
		tabs = new TabPane(header, difat, fat, sectors, miniStream, directory);
		tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		MenuItem exit = new MenuItem("Exit");
		exit.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent e){
				Platform.exit();
			}
		});
		Menu fileMenu = new Menu("File", null, exit);
		menuBar = new MenuBar(fileMenu);

		// Add File menu

		mainPane = new BorderPane();
		mainPane.setTop(menuBar);
		mainPane.setCenter(tabs);

		Parameters parameters = getParameters();
		List<String> args = parameters.getRaw();
		if (args.size() > 0){
			filename = args.get(0);
		}

		if (filename != null) {
			stage.setTitle(filename);
			try {
				msg = new MSG(filename);
			} catch (Exception e){
				msg = null;
				filename = null;
			}
			if (msg != null)
				update(msg);
		} else {
			stage.setTitle("msg Viewer application");
		}

		Scene scene = new Scene(mainPane, 800, 600);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	private void update(MSG msg)
	{
		header.update(msg, localizer);
		difat.update(msg, localizer);
		fat.update(msg, localizer);
		sectors.update(msg, localizer);
		miniStream.update(msg, localizer);
		directory.update(msg, localizer);
	}

	static public void main(String[] args)
	{
		launch(MsgExplorer.class, args);
		System.exit(0);
	}
}
