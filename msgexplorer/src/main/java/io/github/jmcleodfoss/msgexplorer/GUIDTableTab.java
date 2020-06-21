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

/** Tab to display the Named Properties GUID Stream
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxmsg/e910b8f0-ab70-410b-bb3a-0fa236a55bfb">MS-OXMSG Section 2.2.3.1.1: GUID Stream</a>
*/
class GUIDTableTab extends Tab
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "directory.entry.namedproperties-guidstream.tabname";
	static private final String INDEX_HEADING = "directory.entry.namedproperties-guidstream.index-heading";
	static private final String GUIDS_HEADING = "directory.entry.namedproperties-guidstream.guid-heading";

	/** The size of the widest possible GUID, used to set the width of the GUID column in the table */
	static private final Text WIDEST_GUID_TEXT = new Text("00000000-0000-0000-0000-000000000000");

	/** A row in the GUID table */
	static public class GUIDRow {
		/** The GUID */
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

		/** The row number / GUID index */
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

	/** The GUID display table */
	private TableView<GUIDRow> table;

	/** Create the GUID display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
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

	/** Update the GUIDdisplay.
	*	@param	msg	The msg object for the file we are displaying
	*/
	void update(MSG msg)
	{
		String[] guids = msg.namedPropertiesGUIDs();
		ObservableList<GUIDRow> al = FXCollections.observableArrayList();
		for (int i = 0; i < guids.length; ++i)
			al.add(new GUIDRow(i, guids[i]));
		table.setItems(al);
	}
}
