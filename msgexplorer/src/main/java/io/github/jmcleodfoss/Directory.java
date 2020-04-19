package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.MSG;
import io.github.jmcleodfoss.msg.NamedPropertyEntry;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

class Directory extends Tab
{
	static private final String PROPNAME_DIRECTORY_TAB_TITLE = "Directory";
	static private final String PROPNAME_DIRECTORY_CONTENTS_READABLE = "HumanReadable";
	static private final String PROPNAME_DIRECTORY_CONTENTS_KEY = "Description";
	static private final String PROPNAME_DIRECTORY_CONTENTS_VALUE = "DirContentValue";
	static private final String PROPNAME_DIRECTORY_CONTENTS_RAW = "DirContentRaw";

	static private final String PROPNAME_GUIDS_INDEX_HEADER = "namedproperties.guids.index-header";
	static private final String PROPNAME_GUIDS_GUID_HEADER = "namedproperties.guids.guid-header";

	static private final String PROPNAME_NUMERICALENTRIES_LABEL = "namedproperties.numericalentries.label";
	static private final String PROPNAME_NUMERICALENTRIES_NAME_ID_HEADER = "namedproperties.numericalentries.name-id-header";
	static private final String PROPNAME_SNENTRIES_PROPERTY_INDEX_HEADER = "namedproperties.snentries.property-index";
	static private final String PROPNAME_SNENTRIES_GUID_INDEX_HEADER = "namedproperties.snentries.guid-index";

	static private final String PROPNAME_STRINGENTRIES_LABEL = "namedproperties.stringentries.label";
	static private final String PROPNAME_STRINGENTRIES_STRING_OFFSET_HEADER = "namedproperties.stringentries.string-offset-header";

	static private final String PROPNAME_STRINGSTREAM_LABEL = "namedproperties.stringstream.label";
	static private final String PROPNAME_STRINGSTREAM_OFFSET_HEADER = "namedproperties.stringstream.offset-header";
	static private final String PROPNAME_STRINGSTREAM_STRING_HEADER = "namedproperties.stringstream.string-header";

	static private final String PROPNAME_ENTRY_LABEL = "namedproperties.entries.label";
	static private final String PROPNAME_ENTRY_KEY_HEADER = "namedproperties.entries.key-header";
	static private final String PROPNAME_ENTRY_VALUE_HEADER = "namedproperties.entries.value-header";

	static private final String PROPNAME_PROPERTIES_HEADER_LABEL = "properties.header.label";
	static private final String PROPNAME_PROPERTIES_HEADER_KEY_HEADER = "properties.header.key-header";
	static private final String PROPNAME_PROPERTIES_HEADER_VALUE_HEADER = "properties.header.value-header";

	static private final String PROPNAME_PROPERTIES_FIXEDWIDTH = "properties.entries.label";

	static private final Text WIDEST_GUID_TEXT = new Text("00000000-0000-0000-0000-000000000000");

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

	private Tab tabNamedPropertyGuids;
	private TableView<GUIDRow> namedPropertyGuids;

	private Tab tabNumericalEntries;
	private NamedPropertiesTable numericalEntries;

	private Tab tabStringEntries;
	private NamedPropertiesTable stringEntries;

	private KVPTableTab<Integer, String> tabStringStream;

	private KVPTableTab<String, String> tabNamedPropertyEntries;

	private KVPTableTab<String, Integer> tabPropertiesHeader;

	private Tab tabProperties;
	private PropertyTable properties;

	private TreeView<DirectoryEntryData> tree;

	private MSG msg;
	private LocalizedText localizer;

	private UpdateInfoService updateInfoService;

	public class GUIDRow {
		private StringProperty guid;
		public StringProperty getGuidProperty()
		{
			if (guid == null) guid = new SimpleStringProperty(this, "guid");
			return guid;
		}
		public String getGuid()
		{
			return getGuidProperty().get();
		}
		public void setGuid(String guid)
		{
			getGuidProperty().set(guid);
		}

		private IntegerProperty index;
		public IntegerProperty getIndexProperty()
		{
			if (index == null) index = new SimpleIntegerProperty(this, "index");
			return index;
		}
		public int getIndex()
		{
			return getIndexProperty().get();
		}
		public void setIndex(int index)
		{
			getIndexProperty().set(index);
		}

		GUIDRow(int index, String guid)
		{
			setGuid(guid);
			setIndex(index);
		}
	}

	public class NamedPropertyRow {
		private IntegerProperty nameIdentifierOrStringOffset;
		public IntegerProperty getNameIdentifierOrStringOffsetProperty()
		{
			if (nameIdentifierOrStringOffset == null) nameIdentifierOrStringOffset = new SimpleIntegerProperty(this, "index");
			return nameIdentifierOrStringOffset;
		}
		public int getNameIdentifierOrStringOffset()
		{
			return getNameIdentifierOrStringOffsetProperty().get();
		}
		public void setNameIdentifierOrStringOffset(int nameIdentifierOrOffset)
		{
			getNameIdentifierOrStringOffsetProperty().set(nameIdentifierOrOffset);
		}

		private IntegerProperty propertyIndex;
		public IntegerProperty getPropertyIndexProperty()
		{
			if (propertyIndex == null) propertyIndex = new SimpleIntegerProperty(this, "index");
			return propertyIndex;
		}
		public int getPropertyIndex()
		{
			return getPropertyIndexProperty().get();
		}
		public void setPropertyIndex(int nameIdentifierOrOffset)
		{
			getPropertyIndexProperty().set(nameIdentifierOrOffset);
		}

		private IntegerProperty guidIndex;
		public IntegerProperty getGuidIndexProperty()
		{
			if (guidIndex == null) guidIndex = new SimpleIntegerProperty(this, "index");
			return guidIndex;
		}
		public int getGuidIndex()
		{
			return getGuidIndexProperty().get();
		}
		public void setGuidIndex(int nameIdentifierOrOffset)
		{
			getGuidIndexProperty().set(nameIdentifierOrOffset);
		}

		NamedPropertyRow(int nameIdentifierOrStringOffset, int propertyIndex, int guidIndex)
		{
			setNameIdentifierOrStringOffset(nameIdentifierOrStringOffset);
			setPropertyIndex(propertyIndex);
			setGuidIndex(guidIndex);
		}
	}

	private class NamedPropertiesTable extends TableView<NamedPropertyRow>
	{
		NamedPropertiesTable(LocalizedText localizer, String propNameIdOrStringProperty)
		{
			super();

			TableColumn<NamedPropertyRow, Integer> nameIdentifierOrStringOffsetPropertyColumn = new TableColumn<NamedPropertyRow, Integer>(localizer.getText(propNameIdOrStringProperty));
			nameIdentifierOrStringOffsetPropertyColumn.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, Integer>("nameIdentifierOrStringOffset"));

			TableColumn<NamedPropertyRow, Integer> propertyIndexColumn = new TableColumn<NamedPropertyRow, Integer>(localizer.getText(PROPNAME_SNENTRIES_PROPERTY_INDEX_HEADER));
			propertyIndexColumn.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, Integer>("propertyIndex"));

			TableColumn<NamedPropertyRow, Integer> guidIndexColumn = new TableColumn<NamedPropertyRow, Integer>(localizer.getText(PROPNAME_SNENTRIES_GUID_INDEX_HEADER));
			guidIndexColumn.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, Integer>("guidIndex"));

			getColumns().setAll(nameIdentifierOrStringOffsetPropertyColumn, propertyIndexColumn, guidIndexColumn);
			setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		}

		void update(java.util.ArrayList<NamedPropertyEntry> al)
		{
			ObservableList<NamedPropertyRow> ol = FXCollections.observableArrayList();
			for (java.util.Iterator<NamedPropertyEntry> iter = al.iterator(); iter.hasNext(); ){
				NamedPropertyEntry item = iter.next();
				ol.add(new NamedPropertyRow(item.nameIdentifierOrStringOffset, item.propertyIndex, item.guidIndex));
			}
			setItems(ol);
		}
	}

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
					properties.update(msg.getProperties(de.entry, fileData), localizer);
					updateTabs(tabPropertiesHeader, tabProperties);
				} else if (msg.isTextData(de.entry)) {
					fileContentsText.setText(msg.convertFileToString(de.entry, fileData));
					updateTabs(tabFileContentsText);
				} else {
					if (isGuidStream(treeItem)) {
						String[] guids = msg.namedPropertiesGUIDs();
						ObservableList<GUIDRow> a1 = FXCollections.observableArrayList();
						for (int i = 0; i < guids.length; ++i)
							a1.add(new GUIDRow(i, guids[i]));
						namedPropertyGuids.setItems(a1);
						updateTabs(tabNamedPropertyGuids);
					} else if (isEntryStream(treeItem)) {
						numericalEntries.update(msg.namedPropertiesNumericalEntries());
						stringEntries.update(msg.namedPropertiesStringEntries());
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
			localizer.getText(PROPNAME_DIRECTORY_CONTENTS_READABLE),
			localizer.getText(PROPNAME_DIRECTORY_CONTENTS_KEY),
			localizer.getText(PROPNAME_DIRECTORY_CONTENTS_VALUE));
		tabDescription.update(MSG.getDirectoryEntryKeys(), localizer);

		data = new ByteDataTable();
		tabData = new Tab(localizer.getText(PROPNAME_DIRECTORY_CONTENTS_RAW));
		tabData.setContent(data);

		contentTabs = new TabPane(tabDescription, tabData);
		contentTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		fileContentsRaw = new ByteDataTable();
		tabFileContentsRaw = new Tab("Raw");
		tabFileContentsRaw.setContent(fileContentsRaw);

		fileContentsText = new Text();
		tabFileContentsText = new Tab("Text");
		tabFileContentsText.setContent(fileContentsText);

		namedPropertyGuids = new TableView<GUIDRow>();
		TableColumn<GUIDRow, Integer> indexColumn = new TableColumn<GUIDRow, Integer>(localizer.getText(PROPNAME_GUIDS_INDEX_HEADER));
		indexColumn.setCellValueFactory(new PropertyValueFactory<GUIDRow, Integer>("index"));

		TableColumn<GUIDRow, String> guidColumn = new TableColumn<GUIDRow, String>(localizer.getText(PROPNAME_GUIDS_GUID_HEADER));
		guidColumn.setCellValueFactory(new PropertyValueFactory<GUIDRow, String>("guid"));
		guidColumn.setPrefWidth(WIDEST_GUID_TEXT.getBoundsInLocal().getWidth());

		namedPropertyGuids.getColumns().setAll(indexColumn, guidColumn);
		namedPropertyGuids.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		tabNamedPropertyGuids = new Tab(localizer.getText("GUIDS"));
		tabNamedPropertyGuids.setContent(namedPropertyGuids);

		numericalEntries = new NamedPropertiesTable(localizer, PROPNAME_NUMERICALENTRIES_NAME_ID_HEADER);
		((TableColumn<NamedPropertyRow, Integer>)(numericalEntries.getColumns().get(0))).setCellFactory(new Callback<TableColumn<NamedPropertyRow, Integer>, TableCell<NamedPropertyRow, Integer>>(){
			@Override public TableCell<NamedPropertyRow, Integer> call(TableColumn<NamedPropertyRow, Integer> column){
				return new TableCell<NamedPropertyRow, Integer>(){
					@Override protected void updateItem(Integer item, boolean empty){
						super.updateItem(item, empty);
						setText(item == null ? "" : String.format("0x%04x", item));
					};
				};
			}
		});
		tabNumericalEntries = new Tab(localizer.getText(PROPNAME_NUMERICALENTRIES_LABEL));
		tabNumericalEntries.setContent(numericalEntries);

		stringEntries = new NamedPropertiesTable(localizer, PROPNAME_STRINGENTRIES_STRING_OFFSET_HEADER);
		tabStringEntries = new Tab(localizer.getText(PROPNAME_STRINGENTRIES_LABEL));
		tabStringEntries.setContent(stringEntries);

		tabStringStream = new KVPTableTab<Integer, String>(localizer.getText(PROPNAME_STRINGSTREAM_LABEL), localizer.getText(PROPNAME_STRINGSTREAM_OFFSET_HEADER), localizer.getText(PROPNAME_STRINGSTREAM_STRING_HEADER));
		tabNamedPropertyEntries = new KVPTableTab<String, String>(localizer.getText(PROPNAME_ENTRY_LABEL), localizer.getText(PROPNAME_ENTRY_KEY_HEADER), localizer.getText(PROPNAME_ENTRY_VALUE_HEADER));

		tabPropertiesHeader = new KVPTableTab<String, Integer>(localizer.getText(PROPNAME_PROPERTIES_HEADER_LABEL), localizer.getText(PROPNAME_PROPERTIES_HEADER_KEY_HEADER), localizer.getText(PROPNAME_PROPERTIES_HEADER_VALUE_HEADER));

		properties = new PropertyTable(localizer);
		tabProperties = new Tab(localizer.getText(PROPNAME_PROPERTIES_FIXEDWIDTH));
		tabProperties.setContent(properties);

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
