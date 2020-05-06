package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.MSG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

class Directory extends Tab
{
	static private final String PROPNAME_DIRECTORY_TAB_TITLE = "Directory";

	static private final String PROPNAME_HEADER_FIELDS_TITLE= "entry.header-fields.title";
	static private final String PROPNAME_HEADER_FIELDS_KEY = "entry.header-fields.key";
	static private final String PROPNAME_HEADER_FIELDS_VALUE = "entry.header-fields.value";

	static private final String PROPNAME_HEADER_RAW_TITLE = "entry.header-raw.title";

	static private final String PROPNAME_DIRECTORY_CONTENTS_RAW_TITLE = "entry.raw.title";
	static private final String PROPNAME_DIRECTORY_CONTENTS_TEXT_TITLE = "entry.text.title";

	static private final String PROPNAME_STRINGSTREAM_LABEL = "namedproperties.stringstream.label";
	static private final String PROPNAME_STRINGSTREAM_OFFSET_HEADER = "namedproperties.stringstream.offset-header";
	static private final String PROPNAME_STRINGSTREAM_STRING_HEADER = "namedproperties.stringstream.string-header";

	static private final String PROPNAME_ENTRY_LABEL = "namedproperties.entries.label";
	static private final String PROPNAME_ENTRY_KEY_HEADER = "namedproperties.entries.key-header";
	static private final String PROPNAME_ENTRY_VALUE_HEADER = "namedproperties.entries.value-header";

	static private final String PROPNAME_PROPERTIES_HEADER_LABEL = "properties.header.label";
	static private final String PROPNAME_PROPERTIES_HEADER_KEY_HEADER = "properties.header.key-header";
	static private final String PROPNAME_PROPERTIES_HEADER_VALUE_HEADER = "properties.header.value-header";

	static private final String PROPNAME_MENUITEM_EXPORT = "tree.contextmenu.export";
	static private final String PROPNAME_MENUITEM_SAVE_ATTACHMENT = "tree.contextmenu.save-attachment";

	static private final String PROPNAME_EXPORT_FILECHOOSER_TEXT_TITLE = "export.filechooser.title-text";
	static private final String PROPNAME_EXPORT_FILECHOOSER_BINARY_TITLE = "export.filechooser.title-bin";
	static private final String PROPNAME_SAVE_ATTACHMENT_DIRECTORYCHOOSER_TITLE = "save.directorychooser.title";

	static private final String PROPNAME_ALL_FILES = "export.filechooser.all-files";
	static private final String PROPNAME_BIN_FILES = "export.filechooser.bin-files";
	static private final String PROPNAME_TXT_FILES = "export.filechooser.txt-files";

	/** The overall pane for all directory info. Left side is the directory
	*   tree, and the right side is the information about the selected node
	*   (if any). The right side is invisible if no node is selected.
	*/
	private SplitPane containingPane;

	/** The directory tree */
	private StackPane treePane;

	/** The information about the selected node (if any). The top is the
	*   directory entry contents (a tabbed pane allowing display of human-
	*   readable text or raw bytes), and the bottom is the "file"
	*   for this directory entry (if any).
	*/
	private SplitPane infoPane;

	/** The tabbed pane showing the directory entry contents. */
	private TabPane contentTabs;

	private KVPTableTab<String, String> tabDescription;

	private Tab tabData;
	private ByteDataTable data;

	private TabPane filePane;

	private Tab tabFileContentsRaw;
	private ByteDataTable fileContentsRaw;

	private Tab tabFileContentsText;
	private TextArea fileContentsText;

	private GUIDTableTab tabNamedPropertyGuids;
	private NamedPropertiesTableTab tabNumericalEntries;
	private NamedPropertiesTableTab tabStringEntries;
	private KVPTableTab<Integer, String> tabStringStream;
	private KVPTableTab<String, String> tabNamedPropertyEntries;
	private KVPTableTab<String, Integer> tabPropertiesHeader;
	private PropertyTableTab tabProperties;

	private TreeView<DirectoryEntryData> tree;

	private MSG msg;
	private LocalizedText localizer;

	private UpdateInfoService updateInfoService;

	private class SelectionChangeListener implements ChangeListener<TreeItem<DirectoryEntryData>>
	{
		@Override
		public void changed(ObservableValue<? extends TreeItem<DirectoryEntryData>> observable, TreeItem<DirectoryEntryData> oldVal, TreeItem<DirectoryEntryData> newVal)
		{
			if (newVal == null) {
				// Probably loading a new file. No need to take any action, but clean up the display.
				fileContentsRaw.clear();
				fileContentsText.setText("");
				return;
			}

			final DirectoryEntryData de = newVal.getValue();
			tabDescription.update(de.kvps, localizer);
			data.update(msg.getRawDirectoryEntry(de));

			// Header points to the mini stream, so skip it.
			if (!msg.isRootStorageObject(de)) {
				updateInfoService.setItem(newVal);
				updateInfoService.setOnSucceeded(new SuccessfulReadHandler());
				updateInfoService.restart();
			} else {
				fileContentsRaw.clear();
				fileContentsText.setText("");
			}
		}
	}

	private class SuccessfulReadHandler implements EventHandler<WorkerStateEvent>
	{
		@Override
		public final void handle(WorkerStateEvent t)
		{
			byte[] fileData = (byte[])t.getSource().getValue();
			if (fileData != null) {
				fileContentsRaw.update(fileData);
				TreeItem<DirectoryEntryData> treeItem = updateInfoService.getItem();
				DirectoryEntryData de = treeItem.getValue();
				if (msg.isProperty(de)) {
					KVPArray<String, Integer> header = msg.getPropertiesHeader(de, fileData);
					if (header.size() > 0)
						tabPropertiesHeader.update(header, localizer);
					tabProperties.update(msg.getPropertiesAsList(de, fileData), localizer);
					if (header.size() > 0)
						updateTabs(tabPropertiesHeader, tabProperties);
					else
						updateTabs(tabProperties);
				} else if (msg.hasTextData(de)) {
					fileContentsText.setText(msg.convertFileToString(de, fileData));
					updateTabs(tabFileContentsText);
				} else {
					if (isGuidStream(treeItem)) {
						tabNamedPropertyGuids.update(msg);
						updateTabs(tabNamedPropertyGuids);
					} else if (isEntryStream(treeItem)) {
						tabNumericalEntries.update(msg.namedPropertiesNumericalEntries());
						tabStringEntries.update(msg.namedPropertiesStringEntries());
						updateTabs(tabNumericalEntries, tabStringEntries);
					} else if (isStringStream(treeItem)) {
						tabStringStream.update(msg.namedPropertiesStrings(), localizer);
						updateTabs(tabStringStream);
					} else if (isEntryStreamEntryData(treeItem)) {
						int i = 0;
						TreeItem<DirectoryEntryData> item = treeItem.previousSibling();
						while (item != null) {
							item = item.previousSibling();
							++i;
						}
						tabNamedPropertyEntries.update(msg.namedPropertyEntry(i-3), localizer);
						updateTabs(tabNamedPropertyEntries);
					} else {
						fileContentsText.setText("");
						updateTabs();
					}
				}
			} else {
				fileContentsRaw.clear();
				fileContentsText.setText("");
				updateTabs();
			}
		}
	}

	private class UpdateInfoService extends Service<byte[]>
	{
		private ObjectProperty<TreeItem<DirectoryEntryData>> item;
		private ObjectProperty<TreeItem<DirectoryEntryData>> itemProperty()
		{
			if (item == null)
				item = new SimpleObjectProperty<TreeItem<DirectoryEntryData>>();
			return item;
		}
		public TreeItem<DirectoryEntryData> getItem()
		{
			return itemProperty().get();
		}
		public void setItem(TreeItem<DirectoryEntryData> entry)
		{
			itemProperty().set(entry);
		}

		protected Task<byte[]> createTask()
		{
			return new Task<byte[]>() {
				protected byte[] call()
				{
					return msg.getFile(getItem().getValue());
				}
			};
		}
	}

	Directory(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_DIRECTORY_TAB_TITLE));
		this.localizer = localizer;
		updateInfoService = new UpdateInfoService();

		tree = new TreeView<DirectoryEntryData>();
		tree.getSelectionModel().selectedItemProperty().addListener(new SelectionChangeListener());
		treePane = new StackPane();
		treePane.getChildren().add(tree);

		tabDescription = new KVPTableTab<String, String>(
			localizer.getText(PROPNAME_HEADER_FIELDS_TITLE),
			localizer.getText(PROPNAME_HEADER_FIELDS_KEY),
			localizer.getText(PROPNAME_HEADER_FIELDS_VALUE));
		tabDescription.update(MSG.getDirectoryEntryKeys(), localizer);

		data = new ByteDataTable();
		tabData = new Tab(localizer.getText(PROPNAME_HEADER_RAW_TITLE));
		tabData.setContent(data);

		contentTabs = new TabPane(tabDescription, tabData);
		contentTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		fileContentsRaw = new ByteDataTable();
		tabFileContentsRaw = new Tab(localizer.getText(PROPNAME_DIRECTORY_CONTENTS_RAW_TITLE));
		tabFileContentsRaw.setContent(fileContentsRaw);

		fileContentsText = new TextArea();
		fileContentsText.setEditable(false);

		tabFileContentsText = new Tab(localizer.getText(PROPNAME_DIRECTORY_CONTENTS_TEXT_TITLE));
		tabFileContentsText.setContent(fileContentsText);

		tabNamedPropertyGuids = new GUIDTableTab(localizer);
		tabNumericalEntries = NamedPropertiesTableTab.numericalNamedPropertyEntriesTableTabFactory(localizer);
		tabStringEntries = NamedPropertiesTableTab.stringNamedPropertyEntriesTableTabFactory(localizer);
		tabStringStream = new KVPTableTab<Integer, String>(localizer.getText(PROPNAME_STRINGSTREAM_LABEL), localizer.getText(PROPNAME_STRINGSTREAM_OFFSET_HEADER), localizer.getText(PROPNAME_STRINGSTREAM_STRING_HEADER));
		tabNamedPropertyEntries = new KVPTableTab<String, String>(localizer.getText(PROPNAME_ENTRY_LABEL), localizer.getText(PROPNAME_ENTRY_KEY_HEADER), localizer.getText(PROPNAME_ENTRY_VALUE_HEADER));
		tabPropertiesHeader = new KVPTableTab<String, Integer>(localizer.getText(PROPNAME_PROPERTIES_HEADER_LABEL), localizer.getText(PROPNAME_PROPERTIES_HEADER_KEY_HEADER), localizer.getText(PROPNAME_PROPERTIES_HEADER_VALUE_HEADER));

		tabProperties = new PropertyTableTab(localizer);

		filePane = new TabPane(tabFileContentsRaw);
		filePane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		infoPane = new SplitPane();
		infoPane.getItems().addAll(contentTabs, filePane);
		infoPane.setOrientation(Orientation.VERTICAL);
		infoPane.setDividerPositions(0.5f);

		containingPane = new SplitPane();
		containingPane.getItems().addAll(treePane, infoPane);
		containingPane.setDividerPositions(0.4f);

		/* Export saves the stream object's contents, defaulting to using the entry's name. */
		MenuItem export = new MenuItem(localizer.getText(PROPNAME_MENUITEM_EXPORT));
		export.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				DirectoryEntryData de = tree.getFocusModel().getFocusedItem().getValue();

				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(PROPNAME_ALL_FILES, "*.*"));
				if (msg.hasTextData(de)) {
					fileChooser.setTitle(localizer.getText(PROPNAME_EXPORT_FILECHOOSER_TEXT_TITLE));
					fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(PROPNAME_TXT_FILES, "*.txt"));
					fileChooser.setInitialFileName(de.name + ".txt");
				} else {
					fileChooser.setTitle(localizer.getText(PROPNAME_EXPORT_FILECHOOSER_BINARY_TITLE));
					fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(PROPNAME_BIN_FILES, "*.bin"));
					fileChooser.setInitialFileName(de.name + ".bin");
				}
				File file = fileChooser.showSaveDialog(tree.getScene().getWindow());
				if (file != null)
					save(file, de);
			}
		});

		MenuItem saveAttachment = new MenuItem(localizer.getText(PROPNAME_MENUITEM_SAVE_ATTACHMENT));
		saveAttachment.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				DirectoryEntryData de = tree.getFocusModel().getFocusedItem().getValue();
				String filename = msg.getAttachmentName(de);

				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
				directoryChooser.setTitle(localizer.getText(PROPNAME_SAVE_ATTACHMENT_DIRECTORYCHOOSER_TITLE));
				File directory = directoryChooser.showDialog(tree.getScene().getWindow());
				if (directory == null)
					return;

				File file = new File(directory, filename);
				if (file.exists()){
					String absPath = file.getAbsolutePath();
					int extensionIndex = absPath.lastIndexOf('.');
					String baseName = absPath.substring(0, extensionIndex);
					String extension = absPath.substring(extensionIndex);

					int i = 0;
					do {
						file = new File(String.format("%s (%d)%s", baseName, i++, extension));
					} while (file.exists());
				}

				save(file, de);
			}
		});

		final ContextMenu contextMenu = new ContextMenu();
		contextMenu.getItems().addAll(export, saveAttachment);
		tree.setContextMenu(contextMenu);

		tree.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, new EventHandler<ContextMenuEvent>(){
			@Override
			public void handle(ContextMenuEvent e){
				final DirectoryEntryData de = tree.getFocusModel().getFocusedItem().getValue();
				if (!msg.isStreamObject(de))
					e.consume();
				saveAttachment.setVisible(de.name.equals("__substg1.0_37010102"));
			}
		});

		setContent(containingPane);
	}

	void save(File file, DirectoryEntryData de)
	{
		try {
			FileChannel fc = new FileOutputStream(file).getChannel();
			fc.write(ByteBuffer.wrap(msg.getFile(de)));
			fc.close();
		} catch (FileNotFoundException ex){
			Alert alert = new Alert(AlertType.WARNING);
			alert.setHeaderText("File not found");
			alert.setContentText(String.format("File \"%s\" was not found", file.getAbsolutePath()));
			alert.showAndWait();
		} catch (IOException ex){
			Alert alert = new Alert(AlertType.WARNING);
			alert.setHeaderText("I/O Error");
			alert.setContentText(String.format("An I/O error was encountered when trying to write \"%s\"", file.getAbsolutePath()));
			alert.showAndWait();
		}
	}

	private TreeItem<DirectoryEntryData> addEntry(MSG msg, DirectoryEntryData ded)
	{
		TreeItem<DirectoryEntryData> node = new TreeItem<DirectoryEntryData>(ded);
		java.util.Iterator<DirectoryEntryData> iter = msg.getChildIterator(ded);
		while (iter.hasNext())
			node.getChildren().add(addEntry(msg, iter.next()));
		return node;
	}

	private boolean isEntryStream(TreeItem<DirectoryEntryData> item)
	{
		return isEntryStreamEntryData(item) && item.previousSibling() != null && item.previousSibling().previousSibling() == null;
	}

	private boolean isGuidStream(TreeItem<DirectoryEntryData> item)
	{
		return isEntryStreamEntryData(item) && item.previousSibling() == null;
	}

	private boolean isEntryStreamEntryData(TreeItem<DirectoryEntryData> item)
	{
		if (item == null)
			return false;
		TreeItem<DirectoryEntryData> parent = item.getParent();
		if (parent == null)
			return false;
		return parent.getParent() == tree.getRoot() && parent.previousSibling() == null;
	}

	private boolean isStringStream(TreeItem<DirectoryEntryData> item)
	{
		return isEntryStreamEntryData(item) && item.previousSibling() != null && item.previousSibling().previousSibling() != null && item.previousSibling().previousSibling().previousSibling() == null;
	}

	void update(MSG msg, LocalizedText localizer)
	{
		tree.setRoot(addEntry(msg, msg.getDirectoryTree()));
		tree.getTreeItem(0).setExpanded(true);
		this.msg = msg;
	}

	void updateTabs(Tab... newTabs)
	{
		filePane.getTabs().retainAll(filePane.getTabs().get(0));

		for (Tab t: newTabs){
			if (!filePane.getTabs().contains(t))
				filePane.getTabs().add(t);
		}

	}
}
