package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.Property;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

class PropertyTableTab extends Tab
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "directory.entry.properties-values.tabname";
	static private final String PROPERTY_ID_HEADING = "directory.entry.properties-values.id-heading";
	static private final String PROPERTY_TYPE_HEADING = "directory.entry.properties-values.type-heading";
	static private final String PROPERTY_NAME_HEADING = "directory.entry.properties-values.name-heading";
	static private final String PROPERTY_FLAGS_HEADING = "directory.entry.properties-values.flags-heading";
	static private final String PROPERTY_VALUE_HEADING = "directory.entry.properties-values.value-heading";

	public class PropertyRow
	{
		private ObjectProperty<Property> property;
		private ObjectProperty<Property> propertyProperty()
		{
			if (property == null)
				property = new SimpleObjectProperty<Property>(this, "property");
			return property;
		}
		public Property getProperty()
		{
			return propertyProperty().get();
		}
		public void setProperty(Property property)
		{
			propertyProperty().set(property);
		}

		private PropertyRow(Property property)
		{
			setProperty(property);
		}
	}

	TableView<PropertyRow> table;

	PropertyTableTab(LocalizedText localizer)
	{
		super(localizer.getText(TAB_TITLE));

		TableColumn<PropertyRow, Property> colPropertyId = new TableColumn<PropertyRow, Property>(localizer.getText(PROPERTY_ID_HEADING));
		colPropertyId.setCellValueFactory(new PropertyValueFactory<PropertyRow, Property>("property"));
		colPropertyId.setCellFactory(new Callback<TableColumn<PropertyRow, Property>, TableCell<PropertyRow, Property>>(){
			@Override public TableCell<PropertyRow, Property> call(TableColumn<PropertyRow, Property> column){
				return new TableCell<PropertyRow, Property>(){
					@Override protected void updateItem(Property item, boolean empty){
						super.updateItem(item, empty);
						setText(item == null ? "" : String.format("0x%08x", item.propertyTag));
					};
				};
			}
		});

		TableColumn<PropertyRow, Property> colPropertyName = new TableColumn<PropertyRow, Property>(localizer.getText(PROPERTY_NAME_HEADING));
		colPropertyName.setCellValueFactory(new PropertyValueFactory<PropertyRow, Property>("property"));
		colPropertyName.setCellFactory(new Callback<TableColumn<PropertyRow, Property>, TableCell<PropertyRow, Property>>(){
			@Override public TableCell<PropertyRow, Property> call(TableColumn<PropertyRow, Property> column){
				return new TableCell<PropertyRow, Property>(){
					@Override protected void updateItem(Property item, boolean empty){
						super.updateItem(item, empty);
						setText(item == null ? "" : item.propertyName);
					};
				};
			}
		});

		TableColumn<PropertyRow, Property> colPropertyType = new TableColumn<PropertyRow, Property>(localizer.getText(PROPERTY_TYPE_HEADING));
		colPropertyType.setCellValueFactory(new PropertyValueFactory<PropertyRow, Property>("property"));
		colPropertyType.setCellFactory(new Callback<TableColumn<PropertyRow, Property>, TableCell<PropertyRow, Property>>(){
			@Override public TableCell<PropertyRow, Property> call(TableColumn<PropertyRow, Property> column){
				return new TableCell<PropertyRow, Property>(){
					@Override protected void updateItem(Property item, boolean empty){
						super.updateItem(item, empty);
						setText(item == null ? "" : item.propertyType);
					};
				};
			}
		});

		TableColumn<PropertyRow, Property> colPropertyFlags = new TableColumn<PropertyRow, Property>(localizer.getText(PROPERTY_FLAGS_HEADING));
		colPropertyFlags.setCellValueFactory(new PropertyValueFactory<PropertyRow, Property>("property"));
		colPropertyFlags.setCellFactory(new Callback<TableColumn<PropertyRow, Property>, TableCell<PropertyRow, Property>>(){
			@Override public TableCell<PropertyRow, Property> call(TableColumn<PropertyRow, Property> column){
				return new TableCell<PropertyRow, Property>(){
					@Override protected void updateItem(Property item, boolean empty){
						super.updateItem(item, empty);
						setText(item == null ? "" : String.format("0x%04x", item.flags));
					};
				};
			}
		});

		TableColumn<PropertyRow, Property> colValue = new TableColumn<PropertyRow, Property>(localizer.getText(PROPERTY_VALUE_HEADING));
		colValue.setCellValueFactory(new PropertyValueFactory<PropertyRow, Property>("property"));
		colValue.setCellFactory(new Callback<TableColumn<PropertyRow, Property>, TableCell<PropertyRow, Property>>(){
			@Override public TableCell<PropertyRow, Property> call(TableColumn<PropertyRow, Property> column){
				return new TableCell<PropertyRow, Property>(){
					@Override protected void updateItem(Property item, boolean empty){
						super.updateItem(item, empty);
						setText(item == null ? "" : item.value());
					};
				};
			}
		});

		table = new TableView<PropertyRow>();
		table.getColumns().setAll(colPropertyId, colPropertyName, colPropertyType, colPropertyFlags, colValue);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setContent(table);
	}

	void update (ArrayList<Property> properties, LocalizedText localizer)
	{
		ObservableList<PropertyRow> ol = FXCollections.observableArrayList();
		for (Property p : properties)
			ol.add(new PropertyRow(p));
		table.setItems(ol);
	}
}
