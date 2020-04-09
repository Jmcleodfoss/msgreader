package io.github.jmcleodfoss.msgexplorer;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

class ByteDataTable extends TableView<ByteDataTable.Row>
{
	static private final Text WIDEST_BYTE_STRING = new Text(" 88");

	static private final String[] COLUMN_HEADINGS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

	/** Convenience class for displaying byte arrays in a TableView */
	static public class Row {
		private ListProperty<Byte> columns;

		public ListProperty<Byte> getColumnsProperty()
		{
			if (columns == null) columns = new SimpleListProperty<Byte>(this, "columns");
			return columns;
		}
		public ObservableList<Byte> getColumns()
		{
			return getColumnsProperty().get();
		}
		public void setColumns(ObservableList<Byte> value)
		{
			getColumnsProperty().set(value);
		}

		Row(byte[] data)
		{
			ArrayList<Byte> al = new ArrayList<Byte>(data.length);
			for(byte b: data)
				al.add(b);
			ObservableList<Byte> ol = FXCollections.observableArrayList(al);
			columns = new SimpleListProperty<Byte>(ol);
		}
	}

	public class HexTableCell extends ListPropertyEntryValueFactory<Byte, Row, String>
	{
		HexTableCell(String property, int index)
		{
			super(property, index);
		}

		@Override
		public ObservableValue<String> call(TableColumn.CellDataFeatures<Row, String> param)
		{
			return new ReadOnlyObjectWrapper<String>(String.format("%02x", getValue(param)));
		}
	}

	private int nColumns;

	ByteDataTable()
	{
		super();

		int dataLength = COLUMN_HEADINGS.length;
		nColumns = dataLength;

		double cellWidth = WIDEST_BYTE_STRING.getBoundsInLocal().getWidth();
		ArrayList<TableColumn<Row, String>> columns = new ArrayList<TableColumn<Row, String>>();
		for (int i = 0; i < dataLength; ++i) {
			TableColumn<Row, String> col = new TableColumn<Row, String>(COLUMN_HEADINGS[i]);
		  	col.setCellValueFactory(new HexTableCell("columns", i));
			col.setPrefWidth(cellWidth);
			columns.add(col);
  		}

		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		getColumns().setAll(columns);
	}

	/** Clear the table's cells */
	void clear()
	{
		getItems().clear();
	}

	/** Update the table with new data.
	*	@param	data	The data to display
	*/
	void update(byte[] data)
	{
		ObservableList<Row> a = FXCollections.observableArrayList();

		int srcIndex = 0;
		while (srcIndex < data.length){
			byte[] rowBytes = Arrays.copyOfRange(data, srcIndex, srcIndex + nColumns);
			a.add(new Row(rowBytes));
			srcIndex += nColumns;
		}
		setItems(a);
	}
}
