package com.musekeys.playlist;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import java.io.File;

import com.musekeys.midiplayer.MidiPlayerControl;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlaylistTester extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		PlaylistViewer playlist = new PlaylistViewer();
		MidiDevice synth = MidiSystem.getSynthesizer();
		synth.open();
		Sequencer sequencer = MidiSystem.getSequencer(false);
		sequencer.open();
		sequencer.getTransmitter().setReceiver(synth.getReceiver());
		playlist.currentFileProperty().addListener((ObservableValue<? extends File> obv, File oldVal, File newVal) -> {
			if(newVal==null)return;
			Sequence sequence;
			try {
				sequence = MidiSystem.getSequence(newVal);
				sequencer.setSequence(sequence);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		MidiPlayerControl control = new MidiPlayerControl(sequencer);
		control.setPlaylistViewer(playlist);
		BorderPane box = new BorderPane(playlist);
		box.setBottom(control);
		Scene scene = new Scene(box);
		primaryStage.setScene(scene);		
		primaryStage.setFullScreen(true);
		primaryStage.show();
		primaryStage.setFullScreen(false);
		primaryStage.setOnCloseRequest(e -> {
			synth.close();
			System.exit(0);
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
}
