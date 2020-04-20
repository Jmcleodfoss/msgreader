package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.MSG;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

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
	private Text fileContentsText;

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
			data.update(msg.getRawDirectoryEntry(de.entry));

			// Header points to the mini stream, so skip it.
			if (de.entry != 0) {
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
				if (msg.isProperty(de.entry)) {
					KVPArray<String, Integer> header = msg.getPropertiesHeader(de.entry, fileData);
					if (header.size() > 0)
						tabPropertiesHeader.update(header, localizer);
					tabProperties.update(msg.getProperties(de.entry, fileData), localizer);
					updateTabs(tabPropertiesHeader, tabProperties);
				} else if (msg.isTextData(de.entry)) {
					fileContentsText.setText(msg.convertFileToString(de.entry, fileData));
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
					} else if (isNamedPropertyEntry(treeItem)) {
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
		ObjectProperty<TreeItem<DirectoryEntryData>> getItemProperty()
		{
			if (item == null)
				item = new SimpleObjectProperty<TreeItem<DirectoryEntryData>>();
			return item;
		}
		public TreeItem<DirectoryEntryData> getItem()
		{
			return getItemProperty().get();
		}
		public void setItem(TreeItem<DirectoryEntryData> entry)
		{
			getItemProperty().set(entry);
		}

		protected Task<byte[]> createTask()
		{
			return new Task<byte[]>() {
				protected byte[] call()
				{
					return msg.getFile(getItem().getValue().entry);
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

		fileContentsText = new Text();
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

		setContent(containingPane);
	}

	private TreeItem<DirectoryEntryData> addEntry(MSG msg, int entry)
	{
		DirectoryEntryData ded = msg.getDirectoryEntryData(entry);
		TreeItem<DirectoryEntryData> node = new TreeItem<DirectoryEntryData>(ded);
		java.util.Iterator<Integer> iter = ded.children.iterator();
		while (iter.hasNext())
			node.getChildren().add(addEntry(msg, iter.next()));
		return node;
	}

	private boolean isEntryStream(TreeItem<DirectoryEntryData> item)
	{
		return isNamedPropertyEntry(item) && item.previousSibling() != null && item.previousSibling().previousSibling() == null;
	}

	private boolean isGuidStream(TreeItem<DirectoryEntryData> item)
	{
		return isNamedPropertyEntry(item) && item.previousSibling() == null;
	}

	private boolean isNamedPropertyEntry(TreeItem<DirectoryEntryData> item)
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
		return isNamedPropertyEntry(item) && item.previousSibling() != null && item.previousSibling().previousSibling() != null && item.previousSibling().previousSibling().previousSibling() == null;
	}

	void update(MSG msg, LocalizedText localizer)
	{
		tree.setRoot(addEntry(msg, 0));
		tree.getTreeItem(0).setExpanded(true);
		this.msg = msg;
	}

	void updateTabs(Tab... newTabs)
	{
		ArrayList<Tab> retain = new ArrayList<Tab>();
		retain.add(filePane.getTabs().get(0));

		for (Tab t: newTabs){
			if (!filePane.getTabs().contains(t))
				filePane.getTabs().add(t);
			retain.add(t);
		}

		filePane.getTabs().retainAll(retain);
	}
}
