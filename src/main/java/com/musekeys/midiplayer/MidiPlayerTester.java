package com.musekeys.midiplayer;

import java.io.File;

import javax.sound.midi.*;

import com.musekeys.keyboard.Keyboard;
import com.musekeys.playlist.PlaylistViewer;
import com.musekeys.waterfall.WaterFall;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class MidiPlayerTester extends Application{	
	@Override
	public void start(Stage primaryStage) throws Exception {
		normalTest(primaryStage);
		//simplePlayerTest(primaryStage);
	}
	
	private void simplePlayerTest(Stage primaryStage) throws Exception {
		MidiDevice synth=MidiSystem.getSynthesizer();
				//MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[10]);
		synth.open();	
		FileChooser fileChooser=new FileChooser();		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MIDI files", "*.mid","*.kar"),
				new FileChooser.ExtensionFilter("All Files", "*.*")								
				);
		fileChooser.setInitialDirectory(new File("G:/MIDI"));
		File file = fileChooser.showOpenDialog(primaryStage);
		if(file==null)System.exit(0);
		primaryStage.setTitle(file.getAbsolutePath());
		Sequencer sequencer=MidiSystem.getSequencer(false);				
		sequencer.open();
		sequencer.getTransmitter().setReceiver(synth.getReceiver());
		Sequence sequence=MidiSystem.getSequence(file);
		sequencer.setSequence(sequence);
		MidiPlayerControl control=new MidiPlayerControl(sequencer);
		primaryStage.setScene(new Scene(control));
		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> {
			synth.close();
			System.exit(0);
		});
	}

	private void normalTest(Stage primaryStage) throws Exception{
		MidiDevice synth=//MidiSystem.getSynthesizer();
				MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[10]);
		synth.open();		
		/*if(new File("./default.sf2").exists()){
		System.out.println("Loading Soundfont...");
		Soundbank bank=MidiSystem.getSoundbank(new File("./default.sf2"));
		//Soundbank bank=MidiSystem.getSoundbank(new File("G:/SoundFont/Stereo Piano.sf2"));
		//synth.unloadAllInstruments(synth.getDefaultSoundbank());
		synth.loadAllInstruments(bank);
		//synth.unloadInstruments(bank1, patch);
		//synth.loadAllInstruments(bank);
		System.out.println("Starting..");
		}*/
		/*for(Instrument ins:bank.getInstruments())
			System.out.println(ins.getName()+" Bank: "+ins.getPatch().getBank()+" Program:"+ins.getPatch().getProgram());*/
		//setUserAgentStylesheet(STYLESHEET_CASPIAN);		
		Keyboard keyboard = new Keyboard();	
		keyboard.getTransmitter().setReceiver(synth.getReceiver());
		/*FileChooser fileChooser=new FileChooser();		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Midi files(.kar & .mid)", "*.mid","*.kar"),
			
				new FileChooser.ExtensionFilter("All Files", "*.*")								
				);
		fileChooser.setInitialDirectory(new File("G:/MIDI"));
		File file = fileChooser.showOpenDialog(primaryStage);
		if(file==null)System.exit(0);
		primaryStage.setTitle(file.getAbsolutePath());*/
		Sequencer sequencer=MidiSystem.getSequencer(false);				
		sequencer.open();
		/*Sequence sequence=MidiSystem.getSequence(file);
		sequencer.setSequence(sequence);*/		
		WaterFall waterFall= new WaterFall(sequencer);
		waterFall.visibleWhiteKeysProperty().bindBidirectional(keyboard.visibleWhiteKeysProperty());
		MidiPlayerControl control=new MidiPlayerControl(sequencer);
		PlaylistViewer playlist=new PlaylistViewer();
		control.setPlaylistViewer(playlist);
		/*if(new File("default.plst").exists()){
			playlist.load(new File("default.plst"));
		}*/
		ChannelViewer viewer=new ChannelViewer();
		playlist.currentFileProperty().addListener((ObservableValue<? extends File> obv, File oldVal, File newVal) -> {
			if(newVal!=null){
				primaryStage.setTitle("MuseKeys "+newVal.getAbsolutePath());
				Sequence sequence;
				try {
					sequence = MidiSystem.getSequence(newVal);
					sequencer.setSequence(sequence);
					viewer.refresh();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				}else {
					sequencer.stop();				
				}
		});		
		sequencer.getTransmitter().setReceiver(viewer.getReceiver());		
		MidiPlayerMixer mixer=new MidiPlayerMixer(viewer,keyboard);
		sequencer.getTransmitter().setReceiver(mixer.getReceiver());
		mixer.getTransmitter().setReceiver(synth.getReceiver());
		viewer.getTransmitter().setReceiver(synth.getReceiver());
		viewer.setMidiVisualizer(keyboard);
		viewer.setMidiVisualizer2(waterFall);
		BorderPane pane=new BorderPane();		
		/*SplitPane topBox=new SplitPane(playlist,control);
		topBox.setOrientation(Orientation.VERTICAL);
		pane.setTop(topBox);*/
		ScrollPane viewerPane=new ScrollPane(viewer);
		viewerPane.setFitToWidth(true);
		viewerPane.setFitToHeight(true);
		SplitPane centerPane=new SplitPane(playlist,viewerPane,waterFall);
		centerPane.setDividerPositions(0.1,0.4,1);
		centerPane.setOrientation(Orientation.VERTICAL);
		pane.setTop(control);
		pane.setCenter(centerPane);		
		pane.setBottom(keyboard);	
		Scene scene=new Scene(pane);	
		/*scene.getStylesheets().add(
				this.getClass().getResource("/com/musekeys/midiplayer/Style.css").toExternalForm());*/
		primaryStage.setScene(scene);
		primaryStage.setFullScreen(true);
		primaryStage.show();
		//primaryStage.setFullScreen(false);
		primaryStage.setMaximized(true);		
		//sequencer.start();
		primaryStage.setOnCloseRequest(e -> {
			synth.close();
			System.exit(0);
		});
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
