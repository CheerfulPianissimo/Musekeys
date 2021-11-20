package com.musekeys.keyboard;

import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import com.musekeys.control.NumberTextField;
import com.musekeys.midi.GMInstruments;
import com.musekeys.midiplayer.InstrumentChooser;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class KeyboardSettings extends BorderPane{	
	private Keyboard keyboard;
	@FXML private NumberTextField visibleKeys,transpose,channel;
	@FXML private CheckBox midithru,useEffects;
	@FXML private Hyperlink program;
	public KeyboardSettings(Keyboard keyboard){
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("KeyboardSettings.fxml"));
		fxmlLoader.setController(this);
		this.keyboard=keyboard;
		try {
			this.setCenter(fxmlLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		setup();
	}

	private void setup() {
		transpose.setMin(-12);
		transpose.setMax(12);
		visibleKeys.valueProperty().bindBidirectional(keyboard.visibleWhiteKeysProperty());
		transpose.valueProperty().bindBidirectional(keyboard.transposeProperty());
		midithru.selectedProperty().bindBidirectional(keyboard.midiThruProperty());
		channel.valueProperty().bindBidirectional(keyboard.outChannelProperty());
		useEffects.selectedProperty().bindBidirectional(keyboard.useEffectsProperty());
		final StringBuffer instrumentPressed=new StringBuffer("0");
		program.setOnAction(e -> {
			program.setVisited(false);
			if (instrumentPressed.toString().equals("1"))
				return;
			Stage pop = new Stage(StageStyle.UTILITY);
			pop.setAlwaysOnTop(true);
			pop.setTitle("Select A Program");			
			InstrumentChooser chooser = new InstrumentChooser();
			pop.setScene(new Scene(chooser));
			pop.show();
			pop.showingProperty().addListener(event -> {
				pop.hide();
				instrumentPressed.setCharAt(0, '0');
			});
			chooser.instrumentProperty().addListener(
					(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
						ShortMessage msg = new ShortMessage();
						try {
							msg.setMessage(ShortMessage.PROGRAM_CHANGE,keyboard.getOutChannel()
									, newValue.intValue(), 0);
							keyboard.getReceiver().send(msg, -1);
						} catch (Exception exc) {
							exc.printStackTrace();
						}						
					});
			chooser.okPressedProperty().addListener(
					(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
						pop.hide();
					});
			instrumentPressed.setCharAt(0, '1');
		});
	}
	
	@FXML protected void visibleKeysMinus(ActionEvent e){
		visibleKeys.setValue(keyboard.getVisibleWhiteKeys()-1);
	}
	
	@FXML protected void visibleKeysPlus(ActionEvent e){
		visibleKeys.setValue(keyboard.getVisibleWhiteKeys()+1);
	}
	
	@FXML protected void transposePlus(ActionEvent e){
		transpose.setValue(keyboard.getTranspose()+1);
	}
	
	@FXML protected void transposeMinus(ActionEvent e){
		transpose.setValue(keyboard.getTranspose()-1);
	}
}
