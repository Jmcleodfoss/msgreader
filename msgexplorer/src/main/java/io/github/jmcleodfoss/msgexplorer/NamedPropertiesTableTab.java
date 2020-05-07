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

class NamedPropertiesTableTab extends Tab
{
	/* Properties for the tab name and table column headings */
	static private final String NUMERICALENTRIES_LABEL = "directory.entry.namedproperties-numericalentries.tabname";
	static private final String NUMERICALENTRIES_NAME_ID_HEADING = "directory.entry.namedproperties-numericalentries.name-id-heading";

	static private final String STRINGENTRIES_LABEL = "directory.entry.namedproperties-stringentries.tabname";
	static private final String STRINGENTRIES_STRING_OFFSET_HEADING = "directory.entry.namedproperties-stringentries.string-offset-heading";

	static private final String NUMERICAL_AND_STRING_ENTRIES_PROPERTY_INDEX_HEADING = "directory.entry.namedproperties-numerical-and-string-entries.property-index-heading";
	static private final String NUMERICAL_AND_STRING_ENTRIES_GUID_INDEX_HEADING = "directory.entry.namedproperties-numerical-and-string-entries.guid-index-heading";

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

		private NamedPropertyRow(EntryStreamEntryData namedPropertyEntry)
		{
			setNamedPropertyEntry(namedPropertyEntry);
		}
	}

	class NamedPropertiesTable extends TableView<NamedPropertyRow>
	{
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

	NamedPropertiesTable table;

	NamedPropertiesTableTab(LocalizedText localizer, String propTabName, String propColHeaderNameIdOrStringProperty, String fmtNameIdOrStringProperty)
	{
		super(localizer.getText(propTabName));
		table = new NamedPropertiesTable(localizer, propColHeaderNameIdOrStringProperty, fmtNameIdOrStringProperty);
		setContent(table);
	}

	void update(java.util.ArrayList<EntryStreamEntryData> al)
	{
		ObservableList<NamedPropertyRow> ol = FXCollections.observableArrayList();
		for (java.util.Iterator<EntryStreamEntryData> iter = al.iterator(); iter.hasNext(); ){
			EntryStreamEntryData item = iter.next();
			ol.add(new NamedPropertyRow(item));
		}
		table.setItems(ol);
	}

	static NamedPropertiesTableTab numericalNamedPropertyEntriesTableTabFactory(LocalizedText localizer)
	{
		return new NamedPropertiesTableTab(localizer, NUMERICALENTRIES_LABEL, NUMERICALENTRIES_NAME_ID_HEADING, "0x%04x");
	}

	static NamedPropertiesTableTab stringNamedPropertyEntriesTableTabFactory(LocalizedText localizer)
	{
		return new NamedPropertiesTableTab(localizer, STRINGENTRIES_LABEL, STRINGENTRIES_STRING_OFFSET_HEADING, "0x%04x");
	}
}
