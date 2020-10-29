package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NotCFBFileException;
import io.github.jmcleodfoss.msg.UnknownStorageTypeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javafx.application.Application;
import javafx.application.Application.Parameters;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

/** msg explorer application: show msg file at any required level of detail. */
public class MsgExplorer extends Application
{
	/* Properties for the menu, items, and actions */
	private static final String APPLICATION_TITLE_DEFAULT = "application.title.default";
	private static final String MENU_FILE = "menu.file";
	private static final String MENU_FILE_OPEN = "menu.file.open";
	private static final String MENU_FILE_EXIT = "menu.file.exit";
	private static final String LOAD_FILE = "openfile.filechooser.title";
	private static final String ALL_FILES = "openfile.filechooser.all-files";
	private static final String MSG_FILES = "openfile.filechooser.msg-files";

	/** The MSG object we are exploring */
	private MSG msg;

	/** Localization object for UI text */
	private LocalizedText localizer;

	/** FileChooser dialog object - only construct it once and use it when needed */
	private FileChooser fileChooser;

	/** The tab for the msg file header */
	private Header header;

	/** The tab for the msg file doubly-indirect file allocation table */
	private DIFAT difat;

	/** The tab for the msg file allocation table */
	private FAT fat;

	/** The tab for the msg file's sectors */
	private Sectors sectors;

	/** The tab for the msg file's mini stream */
	private MiniStream miniStream;

	/** The tab for the msg file's directory tree */
	private Directory directory;

	/** Open a new file and display the data
	*	@param	pathAndFileName	the file to open
	*	@param	stage	The current stage (used to set the window title)
	*/
	private void openFile(String pathAndFileName, Stage stage)
	{
		if (pathAndFileName != null) {
			try {
				msg = new MSG(pathAndFileName);
			} catch (final FileNotFoundException e) {
				System.out.printf("Error: %s not found%n", pathAndFileName);
				msg = null;
			} catch (final NotCFBFileException e) {
				System.out.printf("Error: %s is not a compound binary file format or msg file%n", pathAndFileName);
				msg = null;
			} catch (final UnknownStorageTypeException e) {
				System.out.printf("Error: Unknown storage type encountered while reading %s%n", pathAndFileName);
				msg = null;
			} catch (final IOException e) {
				System.out.printf("Error: %s is not a compound binary file format or msg file%n", pathAndFileName);
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

	/** Set up the menus
	*	@param	stage	The current stage (tp give the location of the FileChooser dialog box, and passed to openFile to update the window title)
	*	@return	The main menubar
	*/
	private MenuBar setupMenus(Stage stage)
	{
		MenuItem open = new MenuItem(localizer.getText(MENU_FILE_OPEN));
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

		MenuItem exit = new MenuItem(localizer.getText(MENU_FILE_EXIT));
		exit.setOnAction(new EventHandler<ActionEvent>(){
			@Override public void handle(ActionEvent e){
				Platform.exit();
			}
		});
		exit.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN));

		Menu fileMenu = new Menu(localizer.getText(MENU_FILE), null, open, exit);

		return new MenuBar(fileMenu);
	}

	/** Initialize and show the MsgExplorer display
	*	@param	stage	The current stage
	*/
	public void start(Stage stage)
	{
		localizer = new LocalizedText();
		stage.setTitle(localizer.getText(APPLICATION_TITLE_DEFAULT));

		header = new Header(localizer);
		difat = new DIFAT(localizer);
		fat = new FAT(localizer);
		sectors = new Sectors(localizer);
		miniStream = new MiniStream(localizer);
		directory = new Directory(localizer);

		TabPane tabs = new TabPane(header, difat, fat, sectors, miniStream, directory);
		tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		MenuBar menuBar = setupMenus(stage);

		BorderPane mainPane = new BorderPane();
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

	/** Update the constituent displays after loading a new file
	*	@param	msg	The MSG object for the file we have just opened.
	*/
	private void update(MSG msg)
	{
		header.update(msg, localizer);
		difat.update(msg, localizer);
		fat.update(msg, localizer);
		sectors.update(msg, localizer);
		miniStream.update(msg, localizer);
		directory.update(msg, localizer);
	}

	/** The main function
	*	@param	args	The command line arguments, used in {@link #start} if present
	*/
	@SuppressWarnings("PMD.DoNotCallSystemExit")
	static public void main(String[] args)
	{
		launch(MsgExplorer.class, args);
		System.exit(0);
	}
}
