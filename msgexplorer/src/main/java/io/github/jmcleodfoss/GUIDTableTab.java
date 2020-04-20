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
	static private final String PROPNAME_GUIDS_TAB_TITLE = "namedproperties.guids.tab-title";

	static private final String PROPNAME_GUIDS_INDEX_HEADER = "namedproperties.guids.index-header";
	static private final String PROPNAME_GUIDS_GUID_HEADER = "namedproperties.guids.guid-header";

	static private final Text WIDEST_GUID_TEXT = new Text("00000000-0000-0000-0000-000000000000");

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

	private TableView<GUIDRow> table;

	GUIDTableTab(LocalizedText localizer)
	{
		super(localizer.getText(PROPNAME_GUIDS_TAB_TITLE));

		table = new TableView<GUIDRow>();
		TableColumn<GUIDRow, Integer> colIndex = new TableColumn<GUIDRow, Integer>(localizer.getText(PROPNAME_GUIDS_INDEX_HEADER));
		colIndex.setCellValueFactory(new PropertyValueFactory<GUIDRow, Integer>("index"));

		TableColumn<GUIDRow, String> colGuid = new TableColumn<GUIDRow, String>(localizer.getText(PROPNAME_GUIDS_GUID_HEADER));
		colGuid.setCellValueFactory(new PropertyValueFactory<GUIDRow, String>("guid"));
		colGuid.setPrefWidth(WIDEST_GUID_TEXT.getBoundsInLocal().getWidth());

		table.getColumns().setAll(colIndex, colGuid);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		setContent(table);
	}

	void update(MSG msg)
	{
		String[] guids = msg.namedPropertiesGUIDs();
		ObservableList<GUIDRow> a1 = FXCollections.observableArrayList();
		for (int i = 0; i < guids.length; ++i)
			a1.add(new GUIDRow(i, guids[i]));
		table.setItems(a1);
	}
}
