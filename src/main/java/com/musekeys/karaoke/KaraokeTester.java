package com.musekeys.karaoke;

import java.io.File;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import com.musekeys.midiplayer.MidiPlayerControl;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class KaraokeTester extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		MidiDevice synth=MidiSystem.getSynthesizer();
		synth.open();	
		FileChooser fileChooser=new FileChooser();		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MIDI files", "*.mid","*.kar"),				
				new FileChooser.ExtensionFilter("All Files", "*.*")								
				);
		fileChooser.setInitialDirectory(new File("G:/MIDI"));
		File file = fileChooser.showOpenDialog(primaryStage);
		primaryStage.setTitle("MuseKeys: "+file.getAbsolutePath());
		Sequence sequence=MidiSystem.getSequence(file);
		Sequencer sequencer=MidiSystem.getSequencer(false);				
		sequencer.open();
		sequencer.setSequence(sequence);
		LyricsViewer lyrics=new LyricsViewer(sequence,sequencer);
		MidiPlayerControl control=new MidiPlayerControl(sequencer);
		sequencer.getTransmitter().setReceiver(synth.getReceiver());
		BorderPane pane=new BorderPane();		
		pane.setTop(control);
		ScrollPane lyricsPane=new ScrollPane(lyrics);
		lyricsPane.setFitToWidth(true);
		pane.setCenter(lyrics);
		Scene scene=new Scene(pane);		
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setMaximized(true);		
		primaryStage.setOnCloseRequest(e -> {
			synth.close();
			System.exit(0);
		});
	}
	
	public static void main(String[] args) {
		launch();
	}
}
