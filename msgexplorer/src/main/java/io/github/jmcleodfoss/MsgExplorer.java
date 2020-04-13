package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;

import java.io.File;
import java.util.List;
import javafx.application.Application;
import javafx.application.Application.Parameters;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class MsgExplorer extends javafx.application.Application
{
	private static final String PROPNAME_APPLICATION_TITLE_DEFAULT = "application.title.default";
	private static final String PROPNAME_MENU_FILE = "menu.file";
	private static final String PROPNAME_MENU_FILE_OPEN = "menu.file.open";
	private static final String PROPNAME_MENU_FILE_EXIT = "menu.fil.exite";
	private static final String PROPNAME_LOAD_FILE = "filechooser.title";
	private static final String PROPNAME_ALL_FILES = "filechooser.all-files";
	private static final String PROPNAME_MSG_FILES = "filechooser.msg-files";

	String filename;
	MSG msg;

	LocalizedText localizer;

	BorderPane mainPane;

	MenuBar menuBar;

	FileChooser fileChooser;

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

	public void openFile(String pathAndFileName, javafx.stage.Stage stage)
	{
		if (pathAndFileName != null) {
			try {
				msg = new MSG(pathAndFileName);
			} catch (Exception e){
				msg = null;
				filename = null;
			}

			if (msg != null){
				update(msg);
				stage.setTitle(pathAndFileName);
			} else {
				stage.setTitle(localizer.getText(PROPNAME_APPLICATION_TITLE_DEFAULT));
			}
		}
	}

	private MenuBar setupMenus(javafx.stage.Stage stage)
	{
		MenuItem open = new MenuItem(localizer.getText("menu.file.open"));
		fileChooser = new FileChooser();
		open.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent e){
				fileChooser.setTitle(PROPNAME_LOAD_FILE);
				fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
				fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter(PROPNAME_ALL_FILES, "*.*"),
					new FileChooser.ExtensionFilter(PROPNAME_MSG_FILES, "*.msg")
				);
				File file = fileChooser.showOpenDialog(stage);
				if (file != null){
					openFile(file.getPath(), stage);
				}
			}
		});

		MenuItem exit = new MenuItem(localizer.getText("menu.file.exit"));
		exit.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent e){
				Platform.exit();
			}
		});
		Menu fileMenu = new Menu(localizer.getText("menu.file"), null, open, exit);

		return new MenuBar(fileMenu);
	}

	public void start(javafx.stage.Stage stage)
	{
		localizer = new LocalizedText();
		stage.setTitle(localizer.getText(PROPNAME_APPLICATION_TITLE_DEFAULT));

		header = new Header(localizer);
		difat = new DIFAT(localizer);
		fat = new FAT(localizer);
		sectors = new Sectors(localizer);
		miniStream = new MiniStream(localizer);
		directory = new Directory(localizer);

		tabs = new TabPane(header, difat, fat, sectors, miniStream, directory);
		tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		menuBar = setupMenus(stage);

		mainPane = new BorderPane();
		mainPane.setTop(menuBar);
		mainPane.setCenter(tabs);

		Parameters parameters = getParameters();
		List<String> args = parameters.getRaw();
		if (args.size() > 0){
			filename = args.get(0);
		}

		openFile(filename, stage);

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
