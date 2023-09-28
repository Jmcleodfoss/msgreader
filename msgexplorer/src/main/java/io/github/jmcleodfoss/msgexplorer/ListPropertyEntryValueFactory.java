package io.github.jmcleodfoss.msgexplorer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;

/** Cell factory for use when property lists provide cell data for a column.
 * 	@param	<R>	data type of the list associated with this factory.
 * 	@param	<S>	row data type
 * 	@param	<T>	cell type
 *
 * The property list can be of any kind; the user-defined call function converts
 * it to the appropriate type for display (typically a String).
 *
 * Use
 *
 * 	// Class variable
 * 	class Row
 * 	{
 * 		private ListProperty<Byte> columns;
 * 		public setColumns(ListProperty<Byte> columns)
 * 		{
 * 			columnsProperty().set(columns);
 * 		}
 * 		public getColumns()
 * 		{
 * 			return columnsProperty().get();
 * 		}
 * 		public columnsProperty()
 * 		{
 * 			if (columns == null) columns = new SimpleListProperty<Byte>(this, "columns");
 * 			return columns;
 * 		}
 *
 *		Row(byte[] data)
 *		{
 *			ArrayList<Byte> al = new ArrayList<Byte>(data.length);
 *			for (byte b: data)
 *				al.add(b);
 *			ObservableList<Byte> ol = FXCollections.observableArrayList(al);
 *			columns = new SimpleListProperty<Byte>(ol);
 *		}
 * 	}
 *
 * 	Row[] columns;
 *
 * 	public static void main(String[] args)
 * 	{
 * 		String[] headers = {"Col 1", "Col B", "Col iii", "Col four" };
 *
 *		ObservableList<Row> rows = FXCollections.observableArrayList();
 *
 * 		int nColumns = headings.length;
 *		TableColumn<Row, String> columns = new TableColumn<Row, String>[nColumns];
 *
 *	 	for (int i = 0; i < nColumns; ++i){
 * 			col[i] = new TableColumn<Row, String>(headers[i]);
 * 			col[i].setCellValueFactory(
 * 				new ListPropertyEntryValueFactory<Byte, Rows, String>("columnsData", i){
 *					@Override
 *					public ObservableValue<String> call(TableColumn.CellDataFeatures<Rows,String> param)
 *					{
 *						byte b = getValue(param);
 *						return new ReadOnlyObjectWrapper(String.format("%02x", getValue(param)));
 *					}
 * 				}
 * 			);
 * 		}
 *
 * 		TreeView tree = new TreeView(rows);
 * 	}
 *
 * Notes
 */
class ListPropertyEntryValueFactory<R, S, T> extends PropertyValueFactory<S, T>
{
	/** The index into the list backing this property */
	private int index;

	/** Create a ListPropertyEntryValueFactory
	*	@param	property	The list (as a property) to use when creating the cell content
	*	@param	index		The index into the list
	*/
	ListPropertyEntryValueFactory(String property, int index)
	{
		super(property);
		this.index = index;
	}

	/** Get the value from the underlying list so it can be manipulated for
	*   display.
	*	@param	param	The row object and ancillary information
	*	@return	The value to base the display on
	*/
	R getValue(TableColumn.CellDataFeatures<S,T> param)
	{
		@SuppressWarnings("unchecked")
		ReadOnlyObjectWrapper<ObservableList<R>> o = (ReadOnlyObjectWrapper<ObservableList<R>>)super.call(param);
		return o.get().get(index);
	}
}
