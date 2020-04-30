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

class KVPTableTab<K, V> extends Tab
{
	/** The KVP table */
	private KVPTable<K, V> table;

	KVPTableTab(String tabName, String keyColumnName, String valueColumnName)
	{
		this(tabName, keyColumnName, valueColumnName, false);
	}

	KVPTableTab(String tabName, String keyColumnName, String valueColumnName, boolean fWideData)
	{
		super(tabName);
		table = new KVPTable<K, V>(keyColumnName, valueColumnName, fWideData);
		setContent(table);
	}
	
	void update(KVPArray<K, V> data, LocalizedText localizer)
	{
		if (data == null)
			table.getItems().clear();
		else
			table.update(data, localizer);
	}
}
