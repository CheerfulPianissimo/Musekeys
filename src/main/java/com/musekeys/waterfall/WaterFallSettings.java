package com.musekeys.waterfall;

import java.io.IOException;

import com.musekeys.control.NumberTextField;
import com.musekeys.waterfall.WaterFall.Mode;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class WaterFallSettings extends BorderPane{
	private @FXML Slider zoomSlider,noteTransculenceSlider;
	private @FXML NumberTextField zoomTextField;
	private @FXML CheckBox noteTransculence,seekOnClick,useEffects;
	private @FXML ToggleButton tickMode,fixedMode;
	private @FXML ColorPicker colorPicker;
    private WaterFall waterfall;

	public WaterFallSettings(WaterFall waterfall){
		super();
		this.waterfall=waterfall;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WaterFallSettings.fxml"));
		fxmlLoader.setController(this);
		try {
			this.setCenter(fxmlLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		init();
	}
	
	private void init() {
		zoomSlider.valueProperty().bindBidirectional(waterfall.scaleFactorProperty());
		zoomTextField.minProperty().bind(zoomSlider.minProperty());
		zoomTextField.maxProperty().bind(zoomSlider.maxProperty());
		zoomTextField.valueProperty().bindBidirectional(zoomSlider.valueProperty());
		noteTransculenceSlider.valueProperty().bindBidirectional(waterfall.noteSensitivityProperty());
		zoomSlider.setMax((waterfall.getMode()==Mode.MICROSECONDS)?100000:1000);
		if(waterfall.getMode()==Mode.MICROSECONDS){
			fixedMode.setSelected(true);
			tickMode.setSelected(false);
		}else {
			tickMode.setSelected(true);
			fixedMode.setSelected(false);
		}
		waterfall.modeProperty().addListener(
				(ObservableValue<? extends Mode> obv,Mode oldVal,Mode newVal)->{
			zoomSlider.setMax((waterfall.getMode()==Mode.MICROSECONDS)?100000:1000);
			if(waterfall.getMode()==Mode.MICROSECONDS){
				fixedMode.setSelected(true);
				tickMode.setSelected(false);
			}else {
				tickMode.setSelected(true);
				fixedMode.setSelected(false);
			}
		});
		noteTransculence.selectedProperty().addListener(e->{
			if(noteTransculence.isSelected())
				noteTransculenceSlider.setDisable(false);
			else {
				noteTransculenceSlider.setValue(1);
				noteTransculenceSlider.setDisable(true);
			}
		});
		seekOnClick.selectedProperty().bindBidirectional(waterfall.seekOnClickProperty());
		useEffects.selectedProperty().bindBidirectional(waterfall.useEffectsProperty());
		tickMode.setOnAction(e->{
			if(tickMode.isSelected()){
				waterfall.setMode(Mode.TICKS);
			}else tickMode.setSelected(true);
		});
		fixedMode.setOnAction(e->{
			if(fixedMode.isSelected()){
				waterfall.setMode(Mode.MICROSECONDS);
			}else fixedMode.setSelected(true);
		});
		if(waterfall.getBackgroundColor() instanceof Color)
		colorPicker.setValue((Color)waterfall.getBackgroundColor());
		colorPicker.valueProperty().addListener(e->{
		    waterfall.setBackgroundColor(colorPicker.getValue());
        });
	}
}
