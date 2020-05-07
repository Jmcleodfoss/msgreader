package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.KVPEntry;

import java.util.Iterator;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;

/** A table of key-value pairs.
*	@param	<K>	The data type for the key entries of the key-value pairs
*	@param	<V>	The data type for the value entries of the key-value pairs
*	Two flavours are supported, based on a boolean constructor parameter
*	* A table where both columns are the same width, for use when no value text is likely to wrap when the screen real estate is split between the two columns
*	* A "wide" variant which adjusts the width of the key column so that it doesn't wrap, at the expense of the key column's width.
*	There is a convenience constructor for the version with equiwidth columns.
*/
class KVPTable<K, V> extends TableView<KVPTable<K,V>.TableData>
{
	/** Table row data, stored as to Strings to allow for localization by the calling function. */
	public class TableData {
		private StringProperty key;
		private StringProperty keyProperty()
		{
			if (key == null)
				key = new SimpleStringProperty(this, "key");
			return key;
		}
		public void setKey(String key)
		{
			keyProperty().set(key);
		}
		public String getKey()
		{
			return keyProperty().get();
		}

		private StringProperty value;
		private StringProperty valueProperty()
		{
			if (value == null)
				value = new SimpleStringProperty(this, "value");
			return value;
		}
		public void setValue(String value)
		{
			valueProperty().set(value);
		}
		public String getValue()
		{
			return valueProperty().get();
		}

		/** Construct a row of the table
		*	@param	key	The key part of the KVP
		*	@param	value	The value part of the KVP
		*/
		private TableData(String key, String value)
		{
			setKey(key);
			setValue(value);
		}
	}

	/** Cell factory class to support wide cells */
	private class WideCellFactoryCallback implements Callback<TableColumn<TableData, String>, TableCell<TableData, String>>
	{
		/** The column whose width to preserve */
		private int columnToPreserveWidthOf;

		/** The column to set the wrapping width of to preserve the width of the other column */
		private int columnToWrap;

		/** Initialize class members based on the index of the column whose width we want to preserve */
		private WideCellFactoryCallback(int columnToPreserveWidthOf)
		{
			this.columnToPreserveWidthOf  = columnToPreserveWidthOf;
			this.columnToWrap = columnToPreserveWidthOf == 0 ? 1 : 0;
		}

		/** Create a wide TableCell with its wrapping width set to allow the other column to display without wrapping
		*	@param	col	The column this table is displayed in
		*	@return	A TableCell ready to display
		*/
		@Override
		public TableCell<TableData, String> call(TableColumn<TableData, String> col){
			final TableCell<TableData, String> cell = new TableCell<TableData, String>() {
				private Text text;
				@Override
				public void updateItem(String item, boolean empty){
					super.updateItem(item, empty);
					if (!isEmpty()){
						text = new Text(item);
						double newWidth = KVPTable.this.getWidth() - getTableUsedWidth();
						text.setWrappingWidth(newWidth);
						getColumns().get(columnToWrap).setPrefWidth(newWidth);
						setGraphic(text);
					}
				}
			};
			return cell;
		}

		/** Find the width of the table data display area without any vertical scroll bars
		*	@return	The width of the column we want to preserve the width of plus 2 x the scrollbar width (regardless of whether the scrollbar is visible)
		*/
		private double getTableUsedWidth()
		{
			double preservedColumnWidth = getColumns().get(columnToPreserveWidthOf).getWidth();

			/* Find the width of the vertical scrollbar (include it even if it won't be shown) */
			Iterator<Node> iter = getChildrenUnmodifiable().iterator();
			while (iter.hasNext()){
				Node n = iter.next();
				if (!(n instanceof Parent))
					continue;
				Iterator<Node> iter2 = ((Parent)n).getChildrenUnmodifiable().iterator();
				while (iter2.hasNext()){
					Node n2 = iter2.next();
					if (!(n2 instanceof ScrollBar))
						continue;
					ScrollBar sb = (ScrollBar)n2;
					if (sb.getOrientation() == Orientation.VERTICAL){
						// It's a little more aesthetically pleasing to not go right up to the scrollbar.
						return preservedColumnWidth + 2*sb.getWidth();
					}
				}
			}
			return preservedColumnWidth;
		}
	}

	/** Create an empty table for data which is not likely to wrap anywhere when all columns are the same width.
	*	@param	keyColumnName	The heading for the first column which displays the keys
	*	@param	valueColumnName	The heading for the second column, which displays the values
	*/
	KVPTable(String keyColumnName, String valueColumnName)
	{
		this(keyColumnName, valueColumnName, false);
	}

	/** Create an empty table where the key column has a fixed width and the data column wraps to display the key names correctly
	*	@param	keyColumnName	The heading for the first column which displays the keys
	*	@param	valueColumnName	The heading for the second column, which displays the values
	*	@param	fWideData	Flag indicating whether to increase the width of the key column to prevent the displayed data from wrapping
	*/
	KVPTable(String keyColumnName, String valueColumnName, boolean fWideData)
	{
		super();

		TableColumn<TableData, String> keyColumn = new TableColumn<TableData, String>(keyColumnName);
		keyColumn.setCellValueFactory(new PropertyValueFactory<TableData, String>("key"));

		TableColumn<TableData, String> valueColumn = new TableColumn<TableData, String>(valueColumnName);
		valueColumn.setCellValueFactory(new PropertyValueFactory<TableData, String>("value"));
		if (fWideData)
			valueColumn.setCellFactory(new WideCellFactoryCallback(0));

		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		getColumns().setAll(keyColumn, valueColumn);
	}

	/** Update the table with new data.
	*	@param	data		The data to display
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update(KVPArray<K, V> data, LocalizedText localizer)
	{
		ObservableList<TableData> a = FXCollections.observableArrayList();
		Iterator<KVPEntry<K, V>> iter = data.iterator();
		while(iter.hasNext()){
			KVPEntry<K, V> kvp = iter.next();
			a.add(new TableData(localizer.getText(kvp.getKey().toString()), kvp.getValue().toString()));
		}
		setItems(a);
	}
}
