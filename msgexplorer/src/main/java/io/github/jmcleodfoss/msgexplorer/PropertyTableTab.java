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

/** Tab displaying properties entries
*	@see <a href="https://docs.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxmsg/20c1125f-043d-42d9-b1dc-cb9b7e5198ef">MS-OXMSG Section 2.4: Property Stream</a>
*/
class PropertyTableTab extends Tab
{
	/* Properties for the tab name and table column headings */
	static private final String TAB_TITLE = "directory.entry.properties-values.tabname";
	static private final String PROPERTY_ID_HEADING = "directory.entry.properties-values.id-heading";
	static private final String PROPERTY_TYPE_HEADING = "directory.entry.properties-values.type-heading";
	static private final String PROPERTY_NAME_HEADING = "directory.entry.properties-values.name-heading";
	static private final String PROPERTY_FLAGS_HEADING = "directory.entry.properties-values.flags-heading";
	static private final String PROPERTY_VALUE_HEADING = "directory.entry.properties-values.value-heading";

	/** The table in which the properties are displayed. */
	TableView<PropertyRow> table;

	/** A row in the property display table. */
	static public class PropertyRow
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

	/** Create the property display tab.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
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

	/** Update the property values when a new properties entry is selected
	*	@param	properties	The list of new properties and values.
	*	@param	localizer	The localizer mapping for the current locale.
	*/
	void update (ArrayList<Property> properties, LocalizedText localizer)
	{
		ObservableList<PropertyRow> ol = FXCollections.observableArrayList();
		for (Property p : properties)
			ol.add(new PropertyRow(p));
		table.setItems(ol);
	}
}
