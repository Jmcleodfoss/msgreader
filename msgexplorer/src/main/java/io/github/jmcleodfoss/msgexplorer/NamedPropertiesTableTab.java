package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.NamedPropertyEntry;

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
		private ObjectProperty<NamedPropertyEntry> namedPropertyEntry;
		public ObjectProperty<NamedPropertyEntry> getNamedPropertyEntryProperty()
		{
			if (namedPropertyEntry == null)
				namedPropertyEntry = new SimpleObjectProperty<NamedPropertyEntry>(this, "namedPropertyEntry");
			return namedPropertyEntry;
		}
		public NamedPropertyEntry getNamedPropertyEntry()
		{
			return getNamedPropertyEntryProperty().get();
		}
		public void setNamedPropertyEntry(NamedPropertyEntry namedPropertyEntry)
		{
			getNamedPropertyEntryProperty().set(namedPropertyEntry);
		}

		NamedPropertyRow(NamedPropertyEntry namedPropertyEntry)
		{
			setNamedPropertyEntry(namedPropertyEntry);
		}
	}

	class NamedPropertiesTable extends TableView<NamedPropertyRow>
	{
		NamedPropertiesTable(LocalizedText localizer, String propColHeaderNameIdOrStringOffset, String fmtNameIdOrStringOffset)
		{
			super();

			TableColumn<NamedPropertyRow, NamedPropertyEntry> colNameIdOrStringOffset = new TableColumn<NamedPropertyRow, NamedPropertyEntry>(localizer.getText(propColHeaderNameIdOrStringOffset));
			colNameIdOrStringOffset.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, NamedPropertyEntry>("namedPropertyEntry"));
			colNameIdOrStringOffset.setCellFactory(new Callback<TableColumn<NamedPropertyRow, NamedPropertyEntry>, TableCell<NamedPropertyRow, NamedPropertyEntry>>(){
				@Override public TableCell<NamedPropertyRow, NamedPropertyEntry> call(TableColumn<NamedPropertyRow, NamedPropertyEntry> column){
					return new TableCell<NamedPropertyRow, NamedPropertyEntry>(){
						@Override protected void updateItem(NamedPropertyEntry item, boolean empty){
							super.updateItem(item, empty);
							setText(item == null ? "" : String.format(fmtNameIdOrStringOffset, item.nameIdentifierOrStringOffset));
						}
					};
				}
			});

			TableColumn<NamedPropertyRow, NamedPropertyEntry> colPropertyIndex = new TableColumn<NamedPropertyRow, NamedPropertyEntry>(localizer.getText(PROPNAME_SNENTRIES_PROPERTY_INDEX_HEADER));
			colPropertyIndex.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, NamedPropertyEntry>("namedPropertyEntry"));
			colPropertyIndex.setCellFactory(new Callback<TableColumn<NamedPropertyRow, NamedPropertyEntry>, TableCell<NamedPropertyRow, NamedPropertyEntry>>(){
				@Override public TableCell<NamedPropertyRow, NamedPropertyEntry> call(TableColumn<NamedPropertyRow, NamedPropertyEntry> column){
					return new TableCell<NamedPropertyRow, NamedPropertyEntry>(){
						@Override protected void updateItem(NamedPropertyEntry item, boolean empty){
							super.updateItem(item, empty);
							setText(item == null ? "" : Integer.toString(item.propertyIndex));
						}
					};
				}
			});

			TableColumn<NamedPropertyRow, NamedPropertyEntry> colGuidIndex = new TableColumn<NamedPropertyRow, NamedPropertyEntry>(localizer.getText(PROPNAME_SNENTRIES_GUID_INDEX_HEADER));
			colGuidIndex.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, NamedPropertyEntry>("namedPropertyEntry"));
			colGuidIndex.setCellFactory(new Callback<TableColumn<NamedPropertyRow, NamedPropertyEntry>, TableCell<NamedPropertyRow, NamedPropertyEntry>>(){
				@Override public TableCell<NamedPropertyRow, NamedPropertyEntry> call(TableColumn<NamedPropertyRow, NamedPropertyEntry> column){
					return new TableCell<NamedPropertyRow, NamedPropertyEntry>(){
						@Override protected void updateItem(NamedPropertyEntry item, boolean empty){
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

	void update(java.util.ArrayList<NamedPropertyEntry> al)
	{
		ObservableList<NamedPropertyRow> ol = FXCollections.observableArrayList();
		for (java.util.Iterator<NamedPropertyEntry> iter = al.iterator(); iter.hasNext(); ){
			NamedPropertyEntry item = iter.next();
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
