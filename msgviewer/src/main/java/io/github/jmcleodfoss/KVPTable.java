package io.github.jmcleodfoss.msgviewer;

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
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;

class KVPTable<K, V> extends TableView<KVPTable<K,V>.TableData>
{
	/** Convenience class for displaying KVP-type values in a TableView
	*   in a Tab
	*/
	public class TableData {
		/** The key / name / description, the first column. */
		private StringProperty key;

		/** Set the key
		*	@param	key	The new description
		*/
		public void setKey(String key)
		{
			keyProperty().set(key);
		}

		/** Retrieve the key
		*	@return	The current key
		*/
		public String getKey()
		{
			return keyProperty().get();
		}

		/** Set up the variable "key" as a SimpleStringProperty, if
		*   needed, and return it.
		*/
		public StringProperty keyProperty()
		{
			if (key == null)
				key = new SimpleStringProperty(this, "key");
			return key;
		}

		/** The value / data, the second column. */
		private StringProperty value;

		/** Set the value
		*	@param	value	The new value
		*/
		public void setValue(String value)
		{
			valueProperty().set(value);
		}

		/** Retrieve the value
		*	@return	The current value
		*/
		public String getValue()
		{
			return valueProperty().get();
		}

		/** Set up the variable "value" as a SimpleStringProperty, if
		*   needed, and return it.
		*/
		public StringProperty valueProperty()
		{
			if (value == null)
				value = new SimpleStringProperty(this, "value");
			return value;
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

	KVPTable(String keyColumnName, String valueColumnName)
	{
		this(keyColumnName, valueColumnName, false);
	}

	KVPTable(String keyColumnName, String valueColumnName, boolean fWideData)
	{
		super();

		TableColumn<TableData, String> keyColumn = new TableColumn<TableData, String>(keyColumnName);
		keyColumn.setCellValueFactory(new PropertyValueFactory("key"));

		TableColumn<TableData, String> valueColumn = new TableColumn<TableData, String>(valueColumnName);
		valueColumn.setCellValueFactory(new PropertyValueFactory("value"));
		if (fWideData)
			valueColumn.setCellFactory(new wideCellFactoryCallback());
		getColumns().setAll(keyColumn, valueColumn);
	}

	/* This was out of control as an inline class. */
	private class wideCellFactoryCallback implements Callback<TableColumn<TableData, String>, TableCell<TableData, String>>
	{
		private double getTableUsedWidth()
		{
			double col1Width = getColumns().get(0).getWidth();

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
						// It's a little more aesthetically pleasing to not
						// go right up to the scrollbar.
						return col1Width + 2*sb.getWidth();
					}
				}
			}
			return col1Width;
		}

		@Override
		public TableCell<TableData, String> call(TableColumn<TableData, String> param){
			final TableCell<TableData, String> cell = new TableCell<TableData, String>() {
				private Text text;
				@Override
				public void updateItem(String item, boolean empty){
					super.updateItem(item, empty);
					if (!isEmpty()){
						text = new Text(item);
						double newWidth = getWidth() - getTableUsedWidth();
						text.setWrappingWidth(newWidth);
						getColumns().get(1).setPrefWidth(newWidth);
						setGraphic(text);
					}
				}
			};
			return cell;
		}
	}

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
