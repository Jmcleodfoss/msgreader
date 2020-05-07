package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.EntryStreamEntryData;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/** Display tab for the Numerical and String Named Properties
*   These are displayed in separate tabs, created by {@link #numericalNamedPropertyEntriesTableTabFactory} and {@link #stringNamedPropertyEntriesTableTabFactory}
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxmsg/81159dd0-649e-4491-b216-877008b23f65">MS-OXMSG Section 2.2.3.1.2: Entry Stream</a>
*/
class NamedPropertiesTableTab extends Tab
{
	/* Properties for the tab name and table column headings */
	static private final String NUMERICALENTRIES_LABEL = "directory.entry.namedproperties-numericalentries.tabname";
	static private final String NUMERICALENTRIES_NAME_ID_HEADING = "directory.entry.namedproperties-numericalentries.name-id-heading";

	static private final String STRINGENTRIES_LABEL = "directory.entry.namedproperties-stringentries.tabname";
	static private final String STRINGENTRIES_STRING_OFFSET_HEADING = "directory.entry.namedproperties-stringentries.string-offset-heading";

	static private final String NUMERICAL_AND_STRING_ENTRIES_PROPERTY_INDEX_HEADING = "directory.entry.namedproperties-numerical-and-string-entries.property-index-heading";
	static private final String NUMERICAL_AND_STRING_ENTRIES_GUID_INDEX_HEADING = "directory.entry.namedproperties-numerical-and-string-entries.guid-index-heading";

	/** A row in the display table */
	public class NamedPropertyRow {
		private ObjectProperty<EntryStreamEntryData> namedPropertyEntry;
		private ObjectProperty<EntryStreamEntryData> namedPropertyEntryProperty()
		{
			if (namedPropertyEntry == null)
				namedPropertyEntry = new SimpleObjectProperty<EntryStreamEntryData>(this, "namedPropertyEntry");
			return namedPropertyEntry;
		}
		public EntryStreamEntryData getNamedPropertyEntry()
		{
			return namedPropertyEntryProperty().get();
		}
		public void setNamedPropertyEntry(EntryStreamEntryData namedPropertyEntry)
		{
			namedPropertyEntryProperty().set(namedPropertyEntry);
		}

		/** Create a row object from a value passed from the MSG object.
		*	@param	namedPropertyEntry	The information about the property to display on this row
		*/
		private NamedPropertyRow(EntryStreamEntryData namedPropertyEntry)
		{
			setNamedPropertyEntry(namedPropertyEntry);
		}
	}

	/** The table showing the String and Numerical Named Properties */
	class NamedPropertiesTable extends TableView<NamedPropertyRow>
	{
		/** Create the Named Properties display table.
		*   The final two parameters determine whether this is for displaying Numerical Named Properties or String Named Properties.
		*	@param	localizer	The localizer mapping for the current locale.
		*	@param	propColHeaderNameIdOrStringOffset	The property name to use for the first column (Name ID for Numerical Named Properties,
		*							String Offset for String Named Properties)
		*	@param	fmtNameIdOrStringOffset	The format to use for displaying values in the first column
		*	@see #numericalNamedPropertyEntriesTableTabFactory
		*	@see #stringNamedPropertyEntriesTableTabFactory
		*/
		NamedPropertiesTable(LocalizedText localizer, String propColHeaderNameIdOrStringOffset, String fmtNameIdOrStringOffset)
		{
			super();

			TableColumn<NamedPropertyRow, EntryStreamEntryData> colNameIdOrStringOffset = new TableColumn<NamedPropertyRow, EntryStreamEntryData>(localizer.getText(propColHeaderNameIdOrStringOffset));
			colNameIdOrStringOffset.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, EntryStreamEntryData>("namedPropertyEntry"));
			colNameIdOrStringOffset.setCellFactory(new Callback<TableColumn<NamedPropertyRow, EntryStreamEntryData>, TableCell<NamedPropertyRow, EntryStreamEntryData>>(){
				@Override public TableCell<NamedPropertyRow, EntryStreamEntryData> call(TableColumn<NamedPropertyRow, EntryStreamEntryData> column){
					return new TableCell<NamedPropertyRow, EntryStreamEntryData>(){
						@Override protected void updateItem(EntryStreamEntryData item, boolean empty){
							super.updateItem(item, empty);
							setText(item == null ? "" : String.format(fmtNameIdOrStringOffset, item.nameIdentifierOrStringOffset));
						}
					};
				}
			});

			TableColumn<NamedPropertyRow, EntryStreamEntryData> colPropertyIndex = new TableColumn<NamedPropertyRow, EntryStreamEntryData>(localizer.getText(NUMERICAL_AND_STRING_ENTRIES_PROPERTY_INDEX_HEADING));
			colPropertyIndex.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, EntryStreamEntryData>("namedPropertyEntry"));
			colPropertyIndex.setCellFactory(new Callback<TableColumn<NamedPropertyRow, EntryStreamEntryData>, TableCell<NamedPropertyRow, EntryStreamEntryData>>(){
				@Override public TableCell<NamedPropertyRow, EntryStreamEntryData> call(TableColumn<NamedPropertyRow, EntryStreamEntryData> column){
					return new TableCell<NamedPropertyRow, EntryStreamEntryData>(){
						@Override protected void updateItem(EntryStreamEntryData item, boolean empty){
							super.updateItem(item, empty);
							setText(item == null ? "" : Integer.toString(item.propertyIndex));
						}
					};
				}
			});

			TableColumn<NamedPropertyRow, EntryStreamEntryData> colGuidIndex = new TableColumn<NamedPropertyRow, EntryStreamEntryData>(localizer.getText(NUMERICAL_AND_STRING_ENTRIES_GUID_INDEX_HEADING));
			colGuidIndex.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, EntryStreamEntryData>("namedPropertyEntry"));
			colGuidIndex.setCellFactory(new Callback<TableColumn<NamedPropertyRow, EntryStreamEntryData>, TableCell<NamedPropertyRow, EntryStreamEntryData>>(){
				@Override public TableCell<NamedPropertyRow, EntryStreamEntryData> call(TableColumn<NamedPropertyRow, EntryStreamEntryData> column){
					return new TableCell<NamedPropertyRow, EntryStreamEntryData>(){
						@Override protected void updateItem(EntryStreamEntryData item, boolean empty){
							super.updateItem(item, empty);
							setText(item == null ? "" : Integer.toString(item.guidIndex));
						}
					};
				}
			});

			getColumns().setAll(colNameIdOrStringOffset, colPropertyIndex, colGuidIndex);
			setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		}
	}

	/** The table displaying the String or Numerical Named Properties */
	NamedPropertiesTable table;

	/** Create the Named Properties display table.
	*   The final three parameters determine whether this is for displaying Numerical Named Properties or String Named Properties.
	*	@param	localizer	The localizer mapping for the current locale.
	*	@param	propTabName	The property name to use to look up the tab name
	*	@param	propColHeaderNameIdOrStringOffset	The property name to use for the first column (Name ID for Numerical Named Properties,
	*							String Offset for String Named Properties)
	*	@param	fmtNameIdOrStringOffset	The format to use for displaying values in the first column
	*	@see #numericalNamedPropertyEntriesTableTabFactory
	*	@see #stringNamedPropertyEntriesTableTabFactory
	*	@see NamedPropertiesTable
	*/
	NamedPropertiesTableTab(LocalizedText localizer, String propTabName, String propColHeaderNameIdOrStringOffset, String fmtNameIdOrStringOffset)
	{
		super(localizer.getText(propTabName));
		table = new NamedPropertiesTable(localizer, propColHeaderNameIdOrStringOffset, fmtNameIdOrStringOffset);
		setContent(table);
	}

	/** Update the Named Properties display
	*	@param	al	The list of new properties to be displayed
	*/
	void update(java.util.ArrayList<EntryStreamEntryData> al)
	{
		ObservableList<NamedPropertyRow> ol = FXCollections.observableArrayList();
		for (java.util.Iterator<EntryStreamEntryData> iter = al.iterator(); iter.hasNext(); ){
			EntryStreamEntryData item = iter.next();
			ol.add(new NamedPropertyRow(item));
		}
		table.setItems(ol);
	}

	/** Create a tab in which to display the Numerical Named Properties
	*	@param	localizer	The localizer mapping for the current locale.
	*	@return	A NamedPropertiesTab for displaying numerical named properties
	*	@see NamedPropertiesTableTab
	*	@see NamedPropertiesTable
	*/
	static NamedPropertiesTableTab numericalNamedPropertyEntriesTableTabFactory(LocalizedText localizer)
	{
		return new NamedPropertiesTableTab(localizer, NUMERICALENTRIES_LABEL, NUMERICALENTRIES_NAME_ID_HEADING, "0x%04x");
	}

	/** Create a tab in which to display the String Named Properties
	*	@param	localizer	The localizer mapping for the current locale.
	*	@return	A NamedPropertiesTab for displaying string named properties
	*	@see NamedPropertiesTableTab
	*	@see NamedPropertiesTable
	*/
	static NamedPropertiesTableTab stringNamedPropertyEntriesTableTabFactory(LocalizedText localizer)
	{
		return new NamedPropertiesTableTab(localizer, STRINGENTRIES_LABEL, STRINGENTRIES_STRING_OFFSET_HEADING, "0x%04x");
	}
}
