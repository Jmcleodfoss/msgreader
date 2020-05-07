package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.MSG;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

class GUIDTableTab extends Tab
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "directory.entry.namedproperties-guidstream.tabname";
	static private final String INDEX_HEADING = "directory.entry.namedproperties-guidstream.index-heading";
	static private final String GUIDS_HEADING = "directory.entry.namedproperties-guidstream.guid-heading";

	static private final Text WIDEST_GUID_TEXT = new Text("00000000-0000-0000-0000-000000000000");

	public class GUIDRow {
		private StringProperty guid;
		private StringProperty guidProperty()
		{
			if (guid == null)
				guid = new SimpleStringProperty(this, "guid");
			return guid;
		}
		public String getGuid()
		{
			return guidProperty().get();
		}
		public void setGuid(String guid)
		{
			guidProperty().set(guid);
		}

		private IntegerProperty index;
		private IntegerProperty indexProperty()
		{
			if (index == null)
				index = new SimpleIntegerProperty(this, "index");
			return index;
		}
		public int getIndex()
		{
			return indexProperty().get();
		}
		public void setIndex(int index)
		{
			indexProperty().set(index);
		}

		private GUIDRow(int index, String guid)
		{
			setGuid(guid);
			setIndex(index);
		}
	}

	private TableView<GUIDRow> table;

	GUIDTableTab(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE));

		table = new TableView<GUIDRow>();
		TableColumn<GUIDRow, Integer> colIndex = new TableColumn<GUIDRow, Integer>(localizer.getText(INDEX_HEADING));
		colIndex.setCellValueFactory(new PropertyValueFactory<GUIDRow, Integer>("index"));

		TableColumn<GUIDRow, String> colGuid = new TableColumn<GUIDRow, String>(localizer.getText(GUIDS_HEADING));
		colGuid.setCellValueFactory(new PropertyValueFactory<GUIDRow, String>("guid"));
		colGuid.setPrefWidth(WIDEST_GUID_TEXT.getBoundsInLocal().getWidth());

		table.getColumns().setAll(colIndex, colGuid);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		setContent(table);
	}

	void update(MSG msg)
	{
		String[] guids = msg.namedPropertiesGUIDs();
		ObservableList<GUIDRow> al = FXCollections.observableArrayList();
		for (int i = 0; i < guids.length; ++i)
			al.add(new GUIDRow(i, guids[i]));
		table.setItems(al);
	}
}
