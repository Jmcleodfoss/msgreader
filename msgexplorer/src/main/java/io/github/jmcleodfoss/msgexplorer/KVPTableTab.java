package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.KVPArray;
import io.github.jmcleodfoss.msg.KVPEntry;

import java.util.Iterator;

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

/** A tab shpowing a {@link KVPTable}
*	@param	<K>	The data type for the key entries of the key-value pairs
*	@param	<V>	The data type for the value entries of the key-value pairs
*	@see KVPTable
*/
class KVPTableTab<K, V> extends Tab
{
	/** The KVP table */
	private KVPTable<K, V> table;

	/** Create a tab with a table for data which is not likely to wrap anywhere when all columns are the same width.
	*	@param	tabName		The tab's name
	*	@param	keyColumnName	The heading for the first column which displays the keys
	*	@param	valueColumnName	The heading for the second column, which displays the values
	*/
	KVPTableTab(String tabName, String keyColumnName, String valueColumnName)
	{
		this(tabName, keyColumnName, valueColumnName, false);
	}

	/** Create a tab with a table where the key column can have a fixed width and the data column wraps to display the key names correctly
	*	@param	keyColumnName	The heading for the first column which displays the keys
	*	@param	valueColumnName	The heading for the second column, which displays the values
	*	@param	fWideData	Flag indicating whether to increase the width of the key column to prevent the displayed data from wrapping
	*/
	KVPTableTab(String tabName, String keyColumnName, String valueColumnName, boolean fWideData)
	{
		super(tabName);
		table = new KVPTable<K, V>(keyColumnName, valueColumnName, fWideData);
		setContent(table);
	}

	/** Update the table with the given data
	*	@param	data		The new data for the table
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update(KVPArray<K, V> data, LocalizedText localizer)
	{
		if (data == null)
			table.getItems().clear();
		else
			table.update(data, localizer);
	}
}
