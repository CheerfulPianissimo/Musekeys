package com.musekeys.midiplayer;

import java.io.File;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MiniKeyboardTester extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		normalTest(primaryStage);
		//inputTest(primaryStage);
	}

	private void normalTest(Stage primaryStage)throws Exception{
		MultiChannelVisualizer keyboard=new MultiChannelVisualizer();
		MidiDevice synth=MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[10]);		
		synth.open();
		FileChooser fileChooser=new FileChooser();		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MIDI files", "*.mid"),
				new FileChooser.ExtensionFilter("All Files", "*.*")								
				);
		fileChooser.setInitialDirectory(new File("G:/MIDI"));
		File file = fileChooser.showOpenDialog(primaryStage);
		Sequencer sequencer =MidiSystem.getSequencer(false);	
		Sequence sequence = MidiSystem.getSequence(file);
		sequencer.getTransmitter().setReceiver(keyboard);	
		sequencer.getTransmitter().setReceiver(synth.getReceiver());
		sequencer.setSequence(sequence);
		sequencer.open();
		sequencer.start();		
		BorderPane pane=new BorderPane();
		pane.setCenter(keyboard);
		pane.setTop(new MidiPlayerControl(sequencer));
		primaryStage.setTitle(file.getAbsolutePath());
		primaryStage.setScene(new Scene(pane));
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			synth.close();
			System.exit(0);			
		});
	}
	
	private void inputTest(Stage primaryStage)throws Exception{		
		MultiChannelVisualizer keyboard=new MultiChannelVisualizer();
		MidiDevice synth=MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[10]);		
		synth.open();		
		MidiDevice out=MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[1]);
		out.open();
		out.getTransmitter().setReceiver(keyboard);
		out.getTransmitter().setReceiver(synth.getReceiver());
		BorderPane pane=new BorderPane();
		pane.setCenter(keyboard);
		primaryStage.setScene(new Scene(pane));
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			synth.close();
			System.exit(0);			
		});
	}
	
	public static void main(String[] args) {
		launch();
	}

}
