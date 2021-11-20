package com.musekeys.keyboard;

import java.io.File;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import com.musekeys.waterfall.WaterFall;

public class Tester extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		//normalTest(primaryStage);	
		midiInputTest(primaryStage);
		//keyboardOnlyTest(primaryStage);
		//multipleKeyboardTest(primaryStage);
	}
	
	private void normalTest(Stage primaryStage) throws Exception{
		BorderPane pane=new BorderPane();
		Synthesizer synth=MidiSystem.getSynthesizer();
		//MidiDevice synth = MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[5]);
		synth.open();
		FileChooser fileChooser=new FileChooser();		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MIDI files", "*.mid"),
				new FileChooser.ExtensionFilter("All Files", "*.*")								
				);
		fileChooser.setInitialDirectory(new File("G:/MIDI"));
		File file = fileChooser.showOpenDialog(primaryStage);
		primaryStage.setTitle(file.getAbsolutePath());
		/*MidiDevice mididevice=MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[4]);
		mididevice.open();*/
		Keyboard keyboard = new Keyboard();	
		keyboard.setMidiThru(true);
		keyboard.getTransmitter().setReceiver(synth.getReceiver());
		/*MidiDevice mididevice2=MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[1]);
		mididevice2.open();
		mididevice2.getTransmitter().setReceiver(keyboard.getReceiver());*/
		//Soundbank bank=MidiSystem.getSoundbank(new File("G:/SoundFont/Yamaha XG Sound Set.sf2"));
        //Soundbank bank=MidiSystem.getSoundbank(new File("G:/SoundFont/Arachno_SoundFont_1.0.sf2"));
		//synth.loadAllInstruments(bank);
		Sequencer sequencer =MidiSystem.getSequencer(false);//(Sequencer) MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[4]);		
		Sequence sequence = MidiSystem.getSequence(file);
		sequencer.getTransmitter().setReceiver(keyboard.getReceiver());			
		sequencer.setSequence(sequence);
		sequencer.open();		
		//sequencer.setTempoFactor(1.0f);	
		pane.setBottom(keyboard);				
		// place the container in the scrollpane and adjust the pane's viewports as required.
		//Scale scaleTransform = new Scale(0.38, 0.60, 0, 0);
		//keyboard.getTransforms().add(scaleTransform);
		BorderPane borderpane=new BorderPane();
		BorderPane box= new BorderPane();
		Slider timeSlider=new Slider(0,sequence.getMicrosecondLength(),0);		
		KeyFrame keyFrame = new KeyFrame(Duration.millis(100), 
				event ->{					
			   	  timeSlider.setValue(sequencer.getMicrosecondPosition());
			});
		Timeline timeline = new Timeline(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
	    timeline.play();
	    timeSlider.valueChangingProperty().addListener(
	    		(ObservableValue<? extends Boolean> bool,Boolean oldVal,Boolean newVal)->{
	    			if(newVal.booleanValue()==true)
	    				timeline.pause();
	    			//else timeline.play();
	    		});
	    timeSlider.valueProperty().addListener((
	    		ObservableValue<? extends Number> ov,
	    		Number oldval, Number newval) -> {	  
	    			if(timeSlider.isValueChanging()){
	    			sequencer.setMicrosecondPosition(newval.longValue());
	    			timeline.play();
	    			if(!sequencer.isRunning())
	    				sequencer.start();
	    			}
	    	});
	    timeSlider.setOnMouseClicked(e->{
	    	double pos=e.getX()/timeSlider.getWidth();
	    	sequencer.setMicrosecondPosition((long)(pos*sequence.getMicrosecondLength()));
	    	if(!sequencer.isRunning())
				sequencer.start();
	    });	    
		box.setCenter(timeSlider);
		borderpane.setCenter(keyboard);
		borderpane.setTop(box);
		Scene scene = new Scene(borderpane);
		primaryStage.setScene(scene);
		primaryStage.show();
		//primaryStage.setMaximized(true);
		//primaryStage.setFullScreen(true);
		sequencer.start();
		primaryStage.setOnCloseRequest(e ->{
			System.exit(0);
			synth.close();
		});
	}
	
	private void midiInputTest(Stage primaryStage) throws Exception{
		MidiDevice input=MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[9]);
		input.open();
		Synthesizer synth=MidiSystem.getSynthesizer();
		//MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[12]);
				//MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[21]);
		Soundbank bank=MidiSystem.getSoundbank(new File("G:/SoundFont/Stereo Piano.sf2"));
		synth.unloadAllInstruments(synth.getDefaultSoundbank());
		synth.loadAllInstruments(bank);
		synth.open();
		Keyboard keyboard = new Keyboard();	
		keyboard.setMidiThru(true);
		keyboard.getTransmitter().setReceiver(synth.getReceiver());
		input.getTransmitter().setReceiver(keyboard.getReceiver());
		primaryStage.setScene(new Scene(keyboard));	
		primaryStage.show();
		//primaryStage.setMaximized(true);
		//primaryStage.setFullScreen(true);		
		primaryStage.setOnCloseRequest(e -> {
			System.exit(0);
			input.close();
		});
	}

	
	private void keyboardOnlyTest(Stage primaryStage) throws Exception{
		Synthesizer synth=MidiSystem.getSynthesizer();
		synth.open();
		Keyboard keyboard = new Keyboard();	
		keyboard.getTransmitter().setReceiver(synth.getReceiver());
		primaryStage.setScene(new Scene(new BorderPane(keyboard)));	
		primaryStage.show();
		primaryStage.setMaximized(true);
		//primaryStage.setFullScreen(true);
		//sequencer.start();
		primaryStage.setOnCloseRequest(e -> {
			System.exit(0);
			synth.close();
		});
	}
	
	private void multipleKeyboardTest(Stage primaryStage)throws Exception{
		Synthesizer synth=MidiSystem.getSynthesizer();
		synth.open();
		TilePane tilepane=new TilePane();		
		Keyboard[] keyboards=new Keyboard[16];
		for(Keyboard key:keyboards){
			key=new Keyboard();
			key.getTransmitter().setReceiver(synth.getReceiver());
			tilepane.getChildren().add(key);
		}
		tilepane.setPrefColumns(1);
		primaryStage.setScene(new Scene(new ScrollPane(tilepane)));	
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			System.exit(0);
			synth.close();
		});
	}
	
	public static void main(String[] args) {
		launch();
	}
}
