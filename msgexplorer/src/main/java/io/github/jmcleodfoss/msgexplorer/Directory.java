package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.Property;
import io.github.jmcleodfoss.msg.PropertyTags;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/** Class to show the directory information: the tree, the directory entry metadata, and the data (file contents) for each entry, including raw bytes and human-readable content where available.
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-cfb/a94d7445-c4be-49cd-b6b9-2f4abc663817">MS-CFB Srction 2.6: Compound File Directory Sectors</a>
*	@see GUIDTableTab
*	@see NamedPropertiesTableTab#numericalNamedPropertyEntriesTableTabFactory
*	@see NamedPropertiesTableTab#stringNamedPropertyEntriesTableTabFactory
*/
class Directory extends Tab
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "directory.main.tabname";

	static private final String HEADER_FIELDS_TAB_TITLE = "directory.header.fields.tabname";
	static private final String HEADER_FIELDS_KEY_HEADING = "directory.header.fields.key-heading";
	static private final String HEADER_FIELDS_VALUE_HEADING = "directory.header.fields.value-heading";

	static private final String HEADER_RAW_TAB_TITLE = "directory.header.raw.tabname";

	static private final String ENTRY_RAW_TAB_TITLE = "directory.entry.raw.tabname";
	static private final String ENTRY_TEXT_TAB_TITLE = "directory.entry.text.tabname";
	static private final String ENTRY_IMAGE_TAB_TITLE = "directory.entry.image.tabname";

	static private final String NP_STRINGSTREAM_TAB_TITLE = "directory.entry.namedproperties-stringstream.tabname";
	static private final String NP_STRINGSTREAM_OFFSET_HEADING = "directory.entry.namedproperties-stringstream.offset-heading";
	static private final String NP_STRINGSTREAM_STRING_HEADING = "directory.entry.namedproperties-stringstream.string-heading";

	static private final String NP_ENTRIES_TAB_TITLE = "directory.entry.namedproperties-entries.tabname";
	static private final String NP_ENTRIES_KEY_HEADING = "directory.entry.namedproperties-entries.key-heading";
	static private final String NP_ENTRIES_VALUE_HEADING = "directory.entry.namedproperties-entries.value-heading";

	static private final String PROPERTIES_HEADER_TAB_TITLE = "directory.entry.properties-header.tabname";
	static private final String PROPERTIES_HEADER_KEY_HEADING = "directory.entry.properties-header.key-heading";
	static private final String PROPERTIES_HEADER_VALUE_HEADING = "directory.entry.properties-header.value-heading";

	static private final String MENUITEM_EXPORT = "tree.contextmenu.export";
	static private final String MENUITEM_SAVE_ATTACHMENT = "tree.contextmenu.save-attachment";

	static private final String EXPORT_FILECHOOSER_TEXT_TITLE = "export.filechooser.title-text";
	static private final String EXPORT_FILECHOOSER_BINARY_TITLE = "export.filechooser.title-bin";
	static private final String SAVE_ATTACHMENT_DIRECTORYCHOOSER_TITLE = "save.directorychooser.title";

	static private final String ALL_FILES = "export.filechooser.all-files";
	static private final String BIN_FILES = "export.filechooser.bin-files";
	static private final String TXT_FILES = "export.filechooser.txt-files";

	/** The list of mime types that can be displayed by javafx.scene.image.Image */
	private static final ArrayList<String> IMAGE_MIME_TYPES = new ArrayList<String>();
	static {
		IMAGE_MIME_TYPES.add("image/bmp");
		IMAGE_MIME_TYPES.add("image/gif");
		IMAGE_MIME_TYPES.add("image/jpe");
		IMAGE_MIME_TYPES.add("image/jpg");
		IMAGE_MIME_TYPES.add("image/jpeg");
		IMAGE_MIME_TYPES.add("image/png");
	};

	/** The directory tree */
	private TreeView<DirectoryEntryData> tree;

	/** The directory entry metadata, in human-readable form. */
	private KVPTableTab<String, String> tabDescription;

	/** The directory entry metadata, as raw bytes. */
	private ByteDataTable data;

	/** Container for the {@link fileContentsRaw file contents in bytes} */
	private ByteDataTable fileContentsRaw;

	/** The file contents as text, where available */
	private TextArea fileContentsText;

	/** Container for the {@link fileContentsText file contents as text} */
	private Tab tabFileContentsText;

	/** This file contents as image, as supported (BMP, JPEG, GIF, PNG) */
	private ImageView fileContentsImage;

	/** Container for the {@link fileContentsImage file contents as Image} */
	private Tab tabFileContentsImage;

	/** Display for Named Properties GUID Stream */
	private GUIDTableTab tabNamedPropertyGuids;

	/** Display for Named Properties Entry Stream Numerical Entries */
	private NamedPropertiesTableTab tabNumericalEntries;

	/** Display for Named Properties Entry Stream String Entries */
	private NamedPropertiesTableTab tabStringEntries;

	/** Display for the Named Properties String Stream */
	private KVPTableTab<Integer, String> tabStringStream;

	/** Display for the named properties entries */
	private KVPTableTab<String, String> tabNamedPropertyEntries;

	/** Display for the properties entries' header */
	private KVPTableTab<String, Integer> tabPropertiesHeader;

	/** Display for the properties entries' values */
	private PropertyTableTab tabProperties;

	/** Container for directory entry and data */
	private TabPane filePane;

	/** The underlying MSG object */
	private MSG msg;

	/** Localization object for the current locale */
	private LocalizedText localizer;

	/** Asynchronous update object */
	private UpdateInfoService updateInfoService;

	/** Handle selection of a new entry in the tree by initiating an asynchronous read request for the entry.
	*   The display will be updated when the read is complete.
	*/
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

	/** Update the entry displays when the asynchronous read is complete after a new entry has been selected */
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
					tabProperties.update(msg.parsePropertiesAsList(de, fileData), localizer);
					if (header.size() > 0)
						updateTabs(tabPropertiesHeader, tabProperties);
					else
						updateTabs(tabProperties);
				} else if (de.isText()) {
					fileContentsText.setText(msg.convertFileToString(de, fileData));
					updateTabs(tabFileContentsText);
				} else if (de.propertyTag == PropertyTags.PidTagAttachDataBinary) {
					HashMap<Integer, Property> properties = msg.getParentPropertiesAsHashMap(de);
					String mimeType = msg.getPropertyValue(properties.get(PropertyTags.PidTagAttachMimeTag));
					if (IMAGE_MIME_TYPES.contains(mimeType)) {
						ByteArrayInputStream imageSource = new ByteArrayInputStream(fileData);
						double imgWidth = filePane.getWidth();

						// Put a buffer at the bottom of the image to reassure the viewer that the image is all there
						double imgHeight = filePane.getHeight() - filePane.getTabMaxHeight() - 10;
						Image image = new Image(imageSource, imgWidth, imgHeight, true, true);
						fileContentsImage.setImage(image);
						updateTabs(tabFileContentsImage);
					}
				} else if (isNamedPropertiesGuidStream(treeItem)) {
					tabNamedPropertyGuids.update(msg);
					updateTabs(tabNamedPropertyGuids);
				} else if (isNamedPropertiesEntryStream(treeItem)) {
					tabNumericalEntries.update(msg.namedPropertiesNumericalEntries());
					tabStringEntries.update(msg.namedPropertiesStringEntries());
					updateTabs(tabNumericalEntries, tabStringEntries);
				} else if (isNamedPropertiesStringStream(treeItem)) {
					tabStringStream.update(msg.namedPropertiesStrings(), localizer);
					updateTabs(tabStringStream);
				} else if (isNamedPropertiesEntry(treeItem)) {
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
			} else {
				fileContentsRaw.clear();
				fileContentsText.setText("");
				updateTabs();
			}
		}
	}

	/** Initiate asynchronous reading of the newly-selected entry */
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

	/** Create and populate the Directory display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	@SuppressWarnings("PMD.ExcessiveMethodLength")
	Directory(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE));
		this.localizer = localizer;
		updateInfoService = new UpdateInfoService();

		tree = new TreeView<DirectoryEntryData>();
		tree.getSelectionModel().selectedItemProperty().addListener(new SelectionChangeListener());

		StackPane treePane = new StackPane();
		treePane.getChildren().add(tree);

		tabDescription = new KVPTableTab<String, String>( localizer.getText(HEADER_FIELDS_TAB_TITLE), localizer.getText(HEADER_FIELDS_KEY_HEADING), localizer.getText(HEADER_FIELDS_VALUE_HEADING));
		tabDescription.update(MSG.getDirectoryEntryKeys(), localizer);

		data = new ByteDataTable();

		Tab tabData = new Tab(localizer.getText(HEADER_RAW_TAB_TITLE));
		tabData.setContent(data);

		TabPane contentTabs = new TabPane(tabDescription, tabData);
		contentTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		fileContentsRaw = new ByteDataTable();

		Tab tabFileContentsRaw = new Tab(localizer.getText(ENTRY_RAW_TAB_TITLE));
		tabFileContentsRaw.setContent(fileContentsRaw);

		fileContentsText = new TextArea();
		fileContentsText.setEditable(false);

		tabFileContentsText = new Tab(localizer.getText(ENTRY_TEXT_TAB_TITLE));
		tabFileContentsText.setContent(fileContentsText);

		fileContentsImage = new ImageView();
		tabFileContentsImage = new Tab(localizer.getText(ENTRY_IMAGE_TAB_TITLE));
		tabFileContentsImage.setContent(fileContentsImage);

		tabNamedPropertyGuids = new GUIDTableTab(localizer);
		tabNumericalEntries = NamedPropertiesTableTab.numericalNamedPropertyEntriesTableTabFactory(localizer);
		tabStringEntries = NamedPropertiesTableTab.stringNamedPropertyEntriesTableTabFactory(localizer);
		tabStringStream = new KVPTableTab<Integer, String>(localizer.getText(NP_STRINGSTREAM_TAB_TITLE), localizer.getText(NP_STRINGSTREAM_OFFSET_HEADING), localizer.getText(NP_STRINGSTREAM_STRING_HEADING));
		tabNamedPropertyEntries = new KVPTableTab<String, String>(localizer.getText(NP_ENTRIES_TAB_TITLE), localizer.getText(NP_ENTRIES_KEY_HEADING), localizer.getText(NP_ENTRIES_VALUE_HEADING));
		tabPropertiesHeader = new KVPTableTab<String, Integer>(localizer.getText(PROPERTIES_HEADER_TAB_TITLE), localizer.getText(PROPERTIES_HEADER_KEY_HEADING), localizer.getText(PROPERTIES_HEADER_VALUE_HEADING));

		tabProperties = new PropertyTableTab(localizer);

		filePane = new TabPane(tabFileContentsRaw);
		filePane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		SplitPane infoPane = new SplitPane();
		infoPane.getItems().addAll(contentTabs, filePane);
		infoPane.setOrientation(Orientation.VERTICAL);
		infoPane.setDividerPositions(0.5f);

		SplitPane containingPane = new SplitPane();
		containingPane.getItems().addAll(treePane, infoPane);
		containingPane.setDividerPositions(0.4f);

		/* Export saves the stream object's contents, defaulting to using the entry's name. */
		MenuItem export = new MenuItem(localizer.getText(MENUITEM_EXPORT));
		export.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				DirectoryEntryData de = tree.getFocusModel().getFocusedItem().getValue();

				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(localizer.getText(ALL_FILES), "*.*"));
				if (de.isText()) {
					fileChooser.setTitle(localizer.getText(EXPORT_FILECHOOSER_TEXT_TITLE));
					fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(localizer.getText(TXT_FILES), "*.txt"));
					fileChooser.setInitialFileName(de.name + ".txt");
				} else {
					fileChooser.setTitle(localizer.getText(EXPORT_FILECHOOSER_BINARY_TITLE));
					fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(localizer.getText(BIN_FILES), "*.bin"));
					fileChooser.setInitialFileName(de.name + ".bin");
				}
				File file = fileChooser.showSaveDialog(tree.getScene().getWindow());
				if (file != null)
					save(file, de);
			}
		});

		MenuItem saveAttachment = new MenuItem(localizer.getText(MENUITEM_SAVE_ATTACHMENT));
		saveAttachment.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				DirectoryEntryData de = tree.getFocusModel().getFocusedItem().getValue();
				HashMap<Integer, Property> properties = msg.getParentPropertiesAsHashMap(de);
				Property p = properties.get(PropertyTags.PidTagAttachLongFilename);
				if (p == null)
					return;

				String filename = msg.getPropertyValue(p);

				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
				directoryChooser.setTitle(localizer.getText(SAVE_ATTACHMENT_DIRECTORYCHOOSER_TITLE));
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

	/** Recursively add directory entries to the directory tree.
	*	@param	msg	The underlying MSG object representing the current file.
	*	@param	ded	The directory entry to add to the tree.
	*	@return	The new TreeItem and all its children
	*/
	private TreeItem<DirectoryEntryData> addEntry(MSG msg, DirectoryEntryData ded)
	{
		TreeItem<DirectoryEntryData> node = new TreeItem<DirectoryEntryData>(ded);
		Iterator<DirectoryEntryData> iter = msg.getChildIterator(ded);
		while (iter.hasNext())
			node.getChildren().add(addEntry(msg, iter.next()));
		return node;
	}

	/** Is the given TreeItem the Named Properties Entry Stream?
	*	@param	item	The TreeIem to check
	*	@return	true if this TreeItem is the Named Properties Entry Stream, false otherwise.
	*/
	private boolean isNamedPropertiesEntryStream(TreeItem<DirectoryEntryData> item)
	{
		return isNamedPropertiesEntry(item) && item.previousSibling() != null && item.previousSibling().previousSibling() == null;
	}

	/** Is the passed TreeItem an entry in the Named Properties folder?
	*	@param	item	The TreeItem to check
	*	@return	true if this TreeItem is in the Named Properties folder, false otherwise.
	*/
	private boolean isNamedPropertiesEntry(TreeItem<DirectoryEntryData> item)
	{
		if (item == null)
			return false;
		TreeItem<DirectoryEntryData> parent = item.getParent();
		if (parent == null)
			return false;
		return parent.getParent() == tree.getRoot() && parent.previousSibling() == null;
	}

	/** Is the given TreeItem the Named Properties GUID Stream?
	*	@param	item	The TreeIem to check
	*	@return	true if this TreeItem is the Named Properties GUID Stream, false otherwise.
	*/
	private boolean isNamedPropertiesGuidStream(TreeItem<DirectoryEntryData> item)
	{
		return isNamedPropertiesEntry(item) && item.previousSibling() == null;
	}

	/** Is the given TreeItem the Named Properties String Stream?
	*	@param	item	The TreeIem to check
	*	@return	true if this TreeItem is the Named Properties String Stream, false otherwise.
	*/
	private boolean isNamedPropertiesStringStream(TreeItem<DirectoryEntryData> item)
	{
		return isNamedPropertiesEntry(item) && item.previousSibling() != null && item.previousSibling().previousSibling() != null && item.previousSibling().previousSibling().previousSibling() == null;
	}

	/** Save directory entry contents as a file
	*	@param	file	The File to save as
	*	@param	de	The directory entry to save the file for
	*/
	private void save(File file, DirectoryEntryData de)
	{
		try {
			FileChannel fc = new FileOutputStream(file).getChannel();
			fc.write(ByteBuffer.wrap(msg.getFile(de)));
			fc.close();
		} catch (final FileNotFoundException ex){
			Alert alert = new Alert(AlertType.WARNING);
			alert.setHeaderText("File not found");
			alert.setContentText(String.format("File \"%s\" was not found", file.getAbsolutePath()));
			alert.showAndWait();
		} catch (final IOException ex){
			Alert alert = new Alert(AlertType.WARNING);
			alert.setHeaderText("I/O Error");
			alert.setContentText(String.format("An I/O error was encountered when trying to write \"%s\"", file.getAbsolutePath()));
			alert.showAndWait();
		}
	}

	/** Update the GUIDdisplay.
	*	@param	msg	The msg object for the file we are displaying
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update(MSG msg, LocalizedText localizer)
	{
		tree.setRoot(addEntry(msg, msg.getDirectoryTree()));
		tree.getTreeItem(0).setExpanded(true);
		this.msg = msg;
	}

	/** Update the visible tabs for the directory entry file by removing all but the raw data tab, and adding the passed tabs.
	*	@param	newTabs	The list of tabs to make visible
	*/
	private void updateTabs(Tab... newTabs)
	{
		filePane.getTabs().retainAll(filePane.getTabs().get(0));

		for (Tab t: newTabs){
			if (!filePane.getTabs().contains(t))
				filePane.getTabs().add(t);
		}

	}
}
