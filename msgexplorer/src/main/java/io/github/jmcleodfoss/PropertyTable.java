package io.github.jmcleodfoss.msgexplorer;

import io.github.jmcleodfoss.msg.Property;

import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

class PropertyTable extends TableView<PropertyTable.PropertyRow>
{
	static private final String PROPNAME_PROPERTY_ID_HEADER = "properties.values.id-header";
	static private final String PROPNAME_PROPERTY_TYPE_HEADER = "properties.values.type-header";
	static private final String PROPNAME_PROPERTY_NAME_HEADER = "properties.values.name-header";
	static private final String PROPNAME_PROPERTY_FLAGS_HEADER = "properties.values.flags-header";
	static private final String PROPNAME_PROPERTY_VALUE_HEADER = "properties.values.value-header";

	public class PropertyRow
	{
		private ObjectProperty<Property> property;
		public ObjectProperty<Property> getPropertyProperty()
		{
			if (property == null)
				property = new SimpleObjectProperty<Property>(this, "property");
			return property;
		}
		public Property getProperty()
		{
			return getPropertyProperty().get();
		}
		public void setProperty(Property property)
		{
			getPropertyProperty().set(property);
		}

		PropertyRow(Property property)
		{
			setProperty(property);
		}
	}

	PropertyTable(LocalizedText localizer)
	{
		super();

		TableColumn<PropertyRow, Property> colPropertyId = new TableColumn<PropertyRow, Property>(localizer.getText(PROPNAME_PROPERTY_ID_HEADER));
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

		TableColumn<PropertyRow, Property> colPropertyName = new TableColumn<PropertyRow, Property>(localizer.getText(PROPNAME_PROPERTY_NAME_HEADER));
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

		TableColumn<PropertyRow, Property> colPropertyType = new TableColumn<PropertyRow, Property>(localizer.getText(PROPNAME_PROPERTY_TYPE_HEADER));
		colPropertyType.setCellValueFactory(new PropertyValueFactory<PropertyRow, Property>("property"));
		colPropertyType.setCellFactory(new Callback<TableColumn<PropertyRow, Property>, TableCell<PropertyRow, Property>>(){
			@Override public TableCell<PropertyRow, Property> call(TableColumn<PropertyRow, Property> column){
				return new TableCell<PropertyRow, Property>(){
					@Override protected void updateItem(Property item, boolean empty){
						super.updateItem(item, empty);
						setText(item == null ? "" : item.type());
					};
				};
			}
		});

		TableColumn<PropertyRow, Property> colPropertyFlags = new TableColumn<PropertyRow, Property>(localizer.getText(PROPNAME_PROPERTY_FLAGS_HEADER));
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

		TableColumn<PropertyRow, Property> colValue = new TableColumn<PropertyRow, Property>(localizer.getText(PROPNAME_PROPERTY_VALUE_HEADER));
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

		getColumns().setAll(colPropertyId, colPropertyName, colPropertyType, colPropertyFlags, colValue);
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}

	void update (ArrayList<Property> properties, LocalizedText localizer)
	{
		ObservableList<PropertyRow> ol = FXCollections.observableArrayList();
		for (Property p : properties)
			ol.add(new PropertyRow(p));
		setItems(ol);
	}
}
