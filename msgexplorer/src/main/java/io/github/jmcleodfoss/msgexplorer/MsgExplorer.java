package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;

public class MsgExplorer extends javafx.application.Application
{
	private static final String APPLICATION_TITLE_DEFAULT = "application.title.default";
	private static final String MENU_FILE = "menu.file";
	private static final String MENU_FILE_OPEN = "menu.file.open";
	private static final String MENU_FILE_EXIT = "menu.fil.exite";
	private static final String LOAD_FILE = "openfile.filechooser.title";
	private static final String ALL_FILES = "openfile.filechooser.all-files";
	private static final String MSG_FILES = "openfile.filechooser.msg-files";

	private MSG msg;

	private LocalizedText localizer;

	private BorderPane mainPane;

	private MenuBar menuBar;

	private FileChooser fileChooser;

	private TabPane tabs;

	private Header header;
	private DIFAT difat;
	private FAT fat;
	private Sectors sectors;
	private MiniStream miniStream;
	private Directory directory;

	public MsgExplorer()
	{
	}

	private void openFile(String pathAndFileName, javafx.stage.Stage stage)
	{
		if (pathAndFileName != null) {
			try {
				msg = new MSG(pathAndFileName);
			} catch (java.io.FileNotFoundException e) {
				System.out.printf("Error: %s not found\n", pathAndFileName);
				msg = null;
			} catch (NotCFBFileException e) {
				System.out.printf("Error: %s is not a compound binary file format or msg file\n", pathAndFileName);
				msg = null;
			} catch (java.io.IOException e) {
				System.out.printf("Error: %s is not a compound binary file format or msg file\n", pathAndFileName);
				msg = null;
			}

			if (msg != null){
				update(msg);
				stage.setTitle(pathAndFileName);
			} else {
				stage.setTitle(localizer.getText(APPLICATION_TITLE_DEFAULT));
			}
		}
	}

	private MenuBar setupMenus(javafx.stage.Stage stage)
	{
		MenuItem open = new MenuItem(localizer.getText("menu.file.open"));
		fileChooser = new FileChooser();
		open.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent e){
				fileChooser.setTitle(localizer.getText(LOAD_FILE));
				fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
				fileChooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter(localizer.getText(ALL_FILES), "*.*"),
					new FileChooser.ExtensionFilter(localizer.getText(MSG_FILES), "*.msg")
				);
				File file = fileChooser.showOpenDialog(stage);
				if (file != null){
					openFile(file.getPath(), stage);
				}
			}
		});
		open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));

		MenuItem exit = new MenuItem(localizer.getText("menu.file.exit"));
		exit.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent e){
				Platform.exit();
			}
		});
		exit.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));

		Menu fileMenu = new Menu(localizer.getText("menu.file"), null, open, exit);

		return new MenuBar(fileMenu);
	}

	public void start(javafx.stage.Stage stage)
	{
		localizer = new LocalizedText();
		stage.setTitle(localizer.getText(APPLICATION_TITLE_DEFAULT));

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
			String pathAndFilename = args.get(0);
			openFile(pathAndFilename, stage);
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
