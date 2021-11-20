package com.musekeys.control;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class NumberTextField extends TextField{
	private SimpleIntegerProperty value,min,max; 
	public NumberTextField(){
		value=new SimpleIntegerProperty();
		min=new SimpleIntegerProperty(0);
		max=new SimpleIntegerProperty(100);
		this.setText("0");
		this.textProperty().addListener(
		(ObservableValue<? extends String> obv,String oldStr,String newStr)->{
			if(newStr.trim().equals("")){
				setValue(getMin());
				setText(newStr);
				return;
			}
			try{
			Integer.parseInt(newStr);
			}catch(NumberFormatException e){
				setText(oldStr);
				return;
			}
			int newVal=Integer.parseInt(newStr);
			if(newVal>=getMin()&&newVal<=getMax())
			setValue(newVal);
			else if(newVal<getMin())setValue(getMin());	
			else setValue(getMax());	
		});
		valueProperty().addListener(
			(ObservableValue<? extends Number> num, Number old, Number newValue) -> {
				if(newValue.intValue()>=getMin()&&newValue.intValue()<=getMax())
			this.setText(Integer.toString(getValue()));
		});
	}
	
	public NumberTextField(int min,int max){
		this();
		setMin(min);
		setMax(max);
	}
	
	public SimpleIntegerProperty valueProperty() {
		return this.value;
	}
	
	public int getValue() {
		return this.valueProperty().get();
	}
	
	public void setValue(int value) {
		if(value>=getMin()&&value<=getMax())
		this.valueProperty().set(value);
	}
	public SimpleIntegerProperty minProperty() {
		return this.min;
	}
	
	public int getMin() {
		return this.minProperty().get();
	}
	
	public void setMin(final int min) {
		this.minProperty().set(min);
	}
	
	public SimpleIntegerProperty maxProperty() {
		return this.max;
	}
	
	public int getMax() {
		return this.maxProperty().get();
	}
	
	public void setMax(final int max) {
		this.maxProperty().set(max);
	}	
}