package com.musekeys.control;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;

public class JumpBackBar extends EditableProgressBar{
	private IntegerProperty defaultValue;

	public JumpBackBar(){
		super();
		defaultValue=new SimpleIntegerProperty();
		this.setOnMouseReleased(e->	super.setValue(getDefaultValue()));
		this.setOnMouseDragReleased(e->	super.setValue(getDefaultValue()));
		this.setOnKeyReleased(e->{
			if (e.getCode().equals(KeyCode.RIGHT)||e.getCode().equals(KeyCode.LEFT)) {
				super.setValue(getDefaultValue());
			}		
		});
	}
	
	public JumpBackBar(int max,int defaultValue){
		this();		
		setMax(max);
		setDefaultValue(defaultValue);
		setValue(defaultValue);
	}
	
	public IntegerProperty defaultValueProperty() {
		return this.defaultValue;
	}	

	public int getDefaultValue() {
		return this.defaultValueProperty().get();
	}	

	public void setDefaultValue(final int defaultValue) {
		this.defaultValueProperty().set(defaultValue);
	}		
}
