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
	static private final String PROPNAME_NUMERICALENTRIES_LABEL = "namedproperties.numericalentries.label";
	static private final String PROPNAME_NUMERICALENTRIES_NAME_ID_HEADER = "namedproperties.numericalentries.name-id-header";

	static private final String PROPNAME_STRINGENTRIES_LABEL = "namedproperties.stringentries.label";
	static private final String PROPNAME_STRINGENTRIES_STRING_OFFSET_HEADER = "namedproperties.stringentries.string-offset-header";

	static private final String PROPNAME_SNENTRIES_PROPERTY_INDEX_HEADER = "namedproperties.snentries.property-index";
	static private final String PROPNAME_SNENTRIES_GUID_INDEX_HEADER = "namedproperties.snentries.guid-index";

	public class NamedPropertyRow {
		private ObjectProperty<EntryStreamEntryData> namedPropertyEntry;
		public ObjectProperty<EntryStreamEntryData> getNamedPropertyEntryProperty()
		{
			if (namedPropertyEntry == null)
				namedPropertyEntry = new SimpleObjectProperty<EntryStreamEntryData>(this, "namedPropertyEntry");
			return namedPropertyEntry;
		}
		public EntryStreamEntryData getNamedPropertyEntry()
		{
			return getNamedPropertyEntryProperty().get();
		}
		public void setNamedPropertyEntry(EntryStreamEntryData namedPropertyEntry)
		{
			getNamedPropertyEntryProperty().set(namedPropertyEntry);
		}

		NamedPropertyRow(EntryStreamEntryData namedPropertyEntry)
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

			TableColumn<NamedPropertyRow, EntryStreamEntryData> colPropertyIndex = new TableColumn<NamedPropertyRow, EntryStreamEntryData>(localizer.getText(PROPNAME_SNENTRIES_PROPERTY_INDEX_HEADER));
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

			TableColumn<NamedPropertyRow, EntryStreamEntryData> colGuidIndex = new TableColumn<NamedPropertyRow, EntryStreamEntryData>(localizer.getText(PROPNAME_SNENTRIES_GUID_INDEX_HEADER));
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
		return new NamedPropertiesTableTab(localizer, PROPNAME_NUMERICALENTRIES_LABEL, PROPNAME_NUMERICALENTRIES_NAME_ID_HEADER, "0x04x");
	}

	static NamedPropertiesTableTab stringNamedPropertyEntriesTableTabFactory(LocalizedText localizer)
	{
		return new NamedPropertiesTableTab(localizer, PROPNAME_STRINGENTRIES_LABEL, PROPNAME_STRINGENTRIES_STRING_OFFSET_HEADER, "0x04x");
	}
}
