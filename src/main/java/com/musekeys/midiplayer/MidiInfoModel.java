package com.musekeys.midiplayer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MidiInfoModel {
	private StringProperty name=new SimpleStringProperty(),version=new SimpleStringProperty(),
			vendor=new SimpleStringProperty(),description=new SimpleStringProperty();
	private BooleanProperty selected=new SimpleBooleanProperty(false);

	public StringProperty nameProperty() {
		return this.name;
	}
	

	public String getName() {
		return this.nameProperty().get();
	}
	

	public void setName(final String name) {
		this.nameProperty().set(name);
	}
	

	public StringProperty versionProperty() {
		return this.version;
	}
	

	public String getVersion() {
		return this.versionProperty().get();
	}
	

	public void setVersion(final String version) {
		this.versionProperty().set(version);
	}
	

	public StringProperty vendorProperty() {
		return this.vendor;
	}
	

	public String getVendor() {
		return this.vendorProperty().get();
	}
	

	public void setVendor(final String vendor) {
		this.vendorProperty().set(vendor);
	}
	

	public StringProperty descriptionProperty() {
		return this.description;
	}
	

	public String getDescription() {
		return this.descriptionProperty().get();
	}
	

	public void setDescription(final String description) {
		this.descriptionProperty().set(description);
	}


	public BooleanProperty selectedProperty() {
		return this.selected;
	}
	


	public boolean isSelected() {
		return this.selectedProperty().get();
	}
	


	public void setSelected(final boolean selected) {
		this.selectedProperty().set(selected);
	}
	
	
	
}
