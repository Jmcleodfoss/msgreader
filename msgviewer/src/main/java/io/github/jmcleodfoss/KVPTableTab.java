package io.github.jmcleodfoss.msgviewer;

import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.KVPEntry;

import java.util.Iterator;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;


class KVPTableTab<K, V> extends Tab
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

	/** The KVP table */
	private TableView<TableData> table;

	KVPTableTab(String tabName, String keyColumnName, String valueColumnName)
	{
		super(tabName);

		table = new TableView<TableData>();

		TableColumn<TableData, String> keyColumn = new TableColumn<TableData, String>(keyColumnName);
		keyColumn.setCellValueFactory(new PropertyValueFactory("key"));

		TableColumn<TableData, String> valueColumn = new TableColumn<TableData, String>(valueColumnName);
		valueColumn.setCellValueFactory(new PropertyValueFactory("value"));

		table.getColumns().setAll(keyColumn, valueColumn);

		setContent(table);
	}

	void update(KVPArray<K, V> data, LocalizedText localizer)
	{
		ObservableList<TableData> a = FXCollections.observableArrayList();
		Iterator<KVPEntry<K, V>> iter = data.iterator();
		while(iter.hasNext()){
			KVPEntry<K, V> kvp = iter.next();
			a.add(new TableData(localizer.getText(kvp.getKey().toString()), kvp.getValue().toString()));
		}
		table.setItems(a);
	}
}
