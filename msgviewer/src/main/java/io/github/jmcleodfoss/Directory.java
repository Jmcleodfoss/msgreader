package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.DirectoryEntryData;
import io.github.jmcleodfoss.msg.MSG;

import java.util.Iterator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

class Directory extends Tab
{
	static private final String PROPNAME_DIRECTORY_TAB_TITLE = "Directory";
	static private final String PROPNAME_DIRECTORY_CONTENTS_KEY = "Description";
	static private final String PROPNAME_DIRECTORY_CONTENTS_VALUE = "Value";

	static private final String[] COLUMN_HEADINGS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

	private SplitPane containingPane;
	private StackPane treePane;
	private VBox contentPane;
	private KVPTable description;
	private ByteDataTable data;

	private TreeView<DirectoryEntryData> tree;

	private MSG msg;
	private LocalizedText localizer;

	class SelectionChangeListener implements ChangeListener<TreeItem<DirectoryEntryData>>
	{
		@Override
		public void changed(ObservableValue<? extends TreeItem<DirectoryEntryData>> observable, TreeItem<DirectoryEntryData> oldVal, TreeItem<DirectoryEntryData> newVal)
		{
			TreeItem<DirectoryEntryData> selectedItem = newVal;
			description.update(newVal.getValue().kvps, localizer);
			data.update(msg.getRawDirectoryEntry(selectedItem.getValue().startingSector));
		}
	}

	Directory(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_DIRECTORY_TAB_TITLE));
		this.localizer = localizer;

		containingPane = new SplitPane();

		tree = new TreeView<DirectoryEntryData>();
		tree.getSelectionModel().selectedItemProperty().addListener(new SelectionChangeListener());
		treePane = new StackPane();
		treePane.getChildren().add(tree);

		description = new KVPTable<String, String>(
			localizer.getText(PROPNAME_DIRECTORY_CONTENTS_KEY),
			localizer.getText(PROPNAME_DIRECTORY_CONTENTS_VALUE));
		data = new ByteDataTable(COLUMN_HEADINGS, true);
		contentPane = new VBox();
		contentPane.getChildren().addAll(description, data);

		containingPane.getItems().addAll(treePane, contentPane);
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

	void update(MSG msg, LocalizedText localizer)
	{
		tree.setRoot(addEntry(msg, 0));
		this.msg = msg;
	}
}
