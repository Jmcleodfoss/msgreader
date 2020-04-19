package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.NamedPropertyEntry;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
		private IntegerProperty nameIdentifierOrStringOffset;
		public IntegerProperty getNameIdentifierOrStringOffsetProperty()
		{
			if (nameIdentifierOrStringOffset == null) nameIdentifierOrStringOffset = new SimpleIntegerProperty(this, "index");
			return nameIdentifierOrStringOffset;
		}
		public int getNameIdentifierOrStringOffset()
		{
			return getNameIdentifierOrStringOffsetProperty().get();
		}
		public void setNameIdentifierOrStringOffset(int nameIdentifierOrOffset)
		{
			getNameIdentifierOrStringOffsetProperty().set(nameIdentifierOrOffset);
		}

		private IntegerProperty propertyIndex;
		public IntegerProperty getPropertyIndexProperty()
		{
			if (propertyIndex == null) propertyIndex = new SimpleIntegerProperty(this, "index");
			return propertyIndex;
		}
		public int getPropertyIndex()
		{
			return getPropertyIndexProperty().get();
		}
		public void setPropertyIndex(int nameIdentifierOrOffset)
		{
			getPropertyIndexProperty().set(nameIdentifierOrOffset);
		}

		private IntegerProperty guidIndex;
		public IntegerProperty getGuidIndexProperty()
		{
			if (guidIndex == null) guidIndex = new SimpleIntegerProperty(this, "index");
			return guidIndex;
		}
		public int getGuidIndex()
		{
			return getGuidIndexProperty().get();
		}
		public void setGuidIndex(int nameIdentifierOrOffset)
		{
			getGuidIndexProperty().set(nameIdentifierOrOffset);
		}

		NamedPropertyRow(int nameIdentifierOrStringOffset, int propertyIndex, int guidIndex)
		{
			setNameIdentifierOrStringOffset(nameIdentifierOrStringOffset);
			setPropertyIndex(propertyIndex);
			setGuidIndex(guidIndex);
		}
	}

	private class NamedPropertiesTable extends TableView<NamedPropertyRow>
	{
		NamedPropertiesTable(LocalizedText localizer, String propNameIdOrStringProperty)
		{
			super();

			TableColumn<NamedPropertyRow, Integer> colNameIdOrStringOffset = new TableColumn<NamedPropertyRow, Integer>(localizer.getText(propNameIdOrStringProperty));
			colNameIdOrStringOffset.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, Integer>("nameIdentifierOrStringOffset"));

			TableColumn<NamedPropertyRow, Integer> colPropertyIndex = new TableColumn<NamedPropertyRow, Integer>(localizer.getText(PROPNAME_SNENTRIES_PROPERTY_INDEX_HEADER));
			colPropertyIndex.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, Integer>("propertyIndex"));

			TableColumn<NamedPropertyRow, Integer> colGuidIndex = new TableColumn<NamedPropertyRow, Integer>(localizer.getText(PROPNAME_SNENTRIES_GUID_INDEX_HEADER));
			colGuidIndex.setCellValueFactory(new PropertyValueFactory<NamedPropertyRow, Integer>("guidIndex"));

			getColumns().setAll(colNameIdOrStringOffset, colPropertyIndex, colGuidIndex);
			setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		}
	}

	private NamedPropertiesTable table;

	NamedPropertiesTableTab(String tabName, LocalizedText localizer, String propNameIdOrStringProperty)
	{
		super(tabName);
		table = new NamedPropertiesTable(localizer, propNameIdOrStringProperty);
		setContent(table);
	}

	void update(java.util.ArrayList<NamedPropertyEntry> al)
	{
		ObservableList<NamedPropertyRow> ol = FXCollections.observableArrayList();
		for (java.util.Iterator<NamedPropertyEntry> iter = al.iterator(); iter.hasNext(); ){
			NamedPropertyEntry item = iter.next();
			ol.add(new NamedPropertyRow(item.nameIdentifierOrStringOffset, item.propertyIndex, item.guidIndex));
		}
		table.setItems(ol);
	}

	static NamedPropertiesTableTab numericalNamedPropertyEntriesTableTabFactory(LocalizedText localizer)
	{
		NamedPropertiesTableTab tab = new NamedPropertiesTableTab(localizer.getText(PROPNAME_NUMERICALENTRIES_LABEL), localizer, PROPNAME_NUMERICALENTRIES_NAME_ID_HEADER);
		((TableColumn<NamedPropertiesTableTab.NamedPropertyRow, Integer>)(tab.table.getColumns().get(0))).setCellFactory(new Callback<TableColumn<NamedPropertiesTableTab.NamedPropertyRow, Integer>, TableCell<NamedPropertiesTableTab.NamedPropertyRow, Integer>>(){
			@Override public TableCell<NamedPropertiesTableTab.NamedPropertyRow, Integer> call(TableColumn<NamedPropertiesTableTab.NamedPropertyRow, Integer> column){
				return new TableCell<NamedPropertiesTableTab.NamedPropertyRow, Integer>(){
					@Override protected void updateItem(Integer item, boolean empty){
						super.updateItem(item, empty);
						setText(item == null ? "" : String.format("0x%04x", item));
					};
				};
			}
		});

		return tab;
	}

	static NamedPropertiesTableTab stringNamedPropertyEntriesTableTabFactory(LocalizedText localizer)
	{
		return new NamedPropertiesTableTab(localizer.getText(PROPNAME_STRINGENTRIES_LABEL), localizer, PROPNAME_STRINGENTRIES_STRING_OFFSET_HEADER);
	}
}
