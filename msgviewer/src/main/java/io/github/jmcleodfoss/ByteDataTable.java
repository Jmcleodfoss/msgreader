package io.github.jmcleodfoss.msgviewer;

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
	// Unicode for CFB files is UTF-16.
	static private final int UNICODE_BYTES = 2;

	static private final Text WIDEST_BYTE_STRING = new Text(" 88");

	/** Convenience class for displaying byte arrays in a TableView */
	static public class Row {
		private ListProperty<Byte> columns;

		public ListProperty<Byte> getColumnsProperty()
		{
			if (columns == null) columns = new SimpleListProperty(this, "columns");
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
			return new ReadOnlyObjectWrapper(String.format("%02x", getValue(param)));
		}
	}

	public class ASCIITableCell extends ListPropertyEntryValueFactory<Byte, Row, String>
	{
		ASCIITableCell(String property, int index)
		{
			super(property, index);
		}

		@Override
		public ObservableValue<String> call(TableColumn.CellDataFeatures<Row, String> param)
		{
			ReadOnlyObjectWrapper o = (ReadOnlyObjectWrapper)super.call(param);
			ObservableList<Byte> ol = (ObservableList)o.get();
			int index = getIndex();
			byte highByte = ol.get(index);
			byte lowByte  = ol.get(index+1);
			char codepoint = (char)(0xffff & (highByte << 8 | lowByte));
			if (!java.lang.Character.isDefined(codepoint))
				codepoint = 0;
			return new ReadOnlyObjectWrapper(String.format("%c", codepoint));
		}
	}

	private int nColumns;

	ByteDataTable(String[] headings, boolean fShowUnicode)
	{
		super();

		int dataLength = headings.length;
		nColumns = dataLength;

		double cellWidth = WIDEST_BYTE_STRING.getBoundsInLocal().getWidth();
		ArrayList<TableColumn<Row, String>> columns = new ArrayList<TableColumn<Row, String>>();
		for (int i = 0; i < dataLength; ++i) {
			TableColumn<Row, String> col = new TableColumn<Row, String>(headings[i]);
		  	col.setCellValueFactory(new HexTableCell("columns", i));
			col.setPrefWidth(cellWidth);
			columns.add(col);
  		}
		if (fShowUnicode){
			assert headings.length % UNICODE_BYTES == 0;

			TableColumn<Row, String> unicodeData = new TableColumn<Row, String>("Unicode");
			columns.add(unicodeData);
			++nColumns;

			for (int i = 0; i < headings.length / UNICODE_BYTES; ++i) {
				TableColumn<Row, String> col = new TableColumn<Row, String>();
			  	col.setCellValueFactory(new ASCIITableCell("columns", i));
				col.setPrefWidth(cellWidth/2);
				unicodeData.getColumns().add(col);
			}
		}

		getColumns().setAll(columns);
	}

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
