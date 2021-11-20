package com.musekeys.control;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public class EditableProgressBar extends ProgressBar {
	private IntegerProperty value, max;

	public EditableProgressBar() {
		super();
		value = new SimpleIntegerProperty(0);
		max = new SimpleIntegerProperty(100);		
		//this.setMaxWidth(Double.MAX_VALUE);
		this.setOnMousePressed(e -> {
			if(e.getButton().equals(MouseButton.SECONDARY)) return;
			this.requestFocus();
			double pos = e.getX() / this.getWidth();
			setProgress(pos);
		});
		this.setOnMouseDragged(e -> {
			if (e.getX() > this.getWidth() || e.getX() < 0)
				return;
			double pos = e.getX() / this.getWidth();
			setProgress(pos);
		});
		this.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.RIGHT)) {
				if (this.getProgress() + 0.01 <= 1.0)
					this.setProgress(this.getProgress() + 0.01);
			} else if (e.getCode().equals(KeyCode.LEFT)) {
				if (this.getProgress() - 0.01 >= 0)
					this.setProgress(this.getProgress() - 0.01);
			}
		});
		this.progressProperty()
				.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
					double percent = newValue.doubleValue() * 100;					
					int newVal = (int) (percent / 100 * getMax());
					setValue(newVal);
				});
		this.valueProperty()
				.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
					double percent = (newValue.doubleValue() * 100) / getMax();
					this.setProgress(percent / 100);
				});
	}

	public EditableProgressBar(int max) {
		this();		
		setMax(max);
	}

	public IntegerProperty valueProperty() {
		return this.value;
	}

	public int getValue() {
		return this.valueProperty().get();
	}

	public void setValue(final int value) {
		this.valueProperty().set(value);
	}

	public IntegerProperty maxProperty() {
		return this.max;
	}

	public int getMax() {
		return this.maxProperty().get();
	}

	public void setMax(final int max) {
		this.maxProperty().set(max);
	}
}