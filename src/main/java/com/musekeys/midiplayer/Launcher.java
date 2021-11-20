package com.musekeys.midiplayer;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiDevice.Info;

import com.musekeys.keyboard.Keyboard;
import com.musekeys.playlist.PlaylistViewer;
import com.musekeys.waterfall.WaterFall;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Launcher extends Application {
	public static MidiDevice MIDIOUT;
	@Override
	public void start(Stage primaryStage) throws Exception {
		BorderPane initPane=new BorderPane();
		Label statusText=new Label("Loading...");
		statusText.setFont(Font.font(20));
		initPane.setCenter(statusText);
		Scene loadScene=new Scene(initPane);
		primaryStage.setScene(loadScene);
		primaryStage.setWidth(400);
		primaryStage.setHeight(400);
        statusText.setText("Setting Up MIDI Devices");
        primaryStage.show();
		//primaryStage.setMaximized(true);
		System.out.println("Setting Up MIDI Devices");
		initMidi();
		/*for(Instrument ins:bank.getInstruments())
			System.out.println(ins.getName()+" Bank: "+ins.getPatch().getBank()+" Program:"+ins.getPatch().getProgram());*/
		//setUserAgentStylesheet(STYLESHEET_CASPIAN);
		System.out.println("Setting Up MIDI Keyboard");
		Keyboard keyboard = new Keyboard();	
		keyboard.getTransmitter().setReceiver(MIDIOUT.getReceiver());
		/*FileChooser fileChooser=new FileChooser();		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Midi files(.kar & .mid)", "*.mid","*.kar"),
			
				new FileChooser.ExtensionFilter("All Files", "*.*")								
				);
		fileChooser.setInitialDirectory(new File("G:/MIDI"));
		File file = fileChooser.showOpenDialog(primaryStage);
		if(file==null)System.exit(0);
		primaryStage.setTitle(file.getAbsolutePath());*/
		System.out.println("Setting Up Sequencer");
		Sequencer sequencer=MidiSystem.getSequencer(false);				
		sequencer.open();
		/*Sequence sequence=MidiSystem.getSequence(file);
		sequencer.setSequence(sequence);*/		
		System.out.println("Setting Up Visualizer And Player");
		WaterFall waterFall= new WaterFall(sequencer);
		waterFall.visibleWhiteKeysProperty().bindBidirectional(keyboard.visibleWhiteKeysProperty());
		MidiPlayerControl control=new MidiPlayerControl(sequencer);
		PlaylistViewer playlist=new PlaylistViewer();
		control.setPlaylistViewer(playlist);
		/*if(new File("default.plst").exists()){
			playlist.load(new File("default.plst"));
		}*/
		System.out.println("Setting Up The MIDI Mixer");
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
		System.out.println("Initializing Connections...");
		sequencer.getTransmitter().setReceiver(viewer.getReceiver());		
		MidiPlayerMixer mixer=new MidiPlayerMixer(viewer,keyboard);
		sequencer.getTransmitter().setReceiver(mixer.getReceiver());
		mixer.getTransmitter().setReceiver(MIDIOUT.getReceiver());
		viewer.getTransmitter().setReceiver(MIDIOUT.getReceiver());
		viewer.setMidiVisualizer(keyboard);
		viewer.setMidiVisualizer2(waterFall);
		BorderPane pane=new BorderPane();		
		SplitPane topBox=new SplitPane(playlist,control);
		topBox.setOrientation(Orientation.VERTICAL);
		pane.setTop(topBox);
		ScrollPane viewerPane=new ScrollPane(viewer);
		viewerPane.setFitToWidth(true);
		viewerPane.setFitToHeight(true);
		SplitPane centerPane=new SplitPane(playlist,viewerPane,waterFall);

		/*Button open=new Button("Open");
		open.setOnAction((ActionEvent event)->{
		    playlist.addFile();
        });
        ((ToolBar)(((BorderPane)((VBox)((BorderPane)control).getCenter()).getChildren().get(0)).getTop()))
                .getItems().add(open);*/

		centerPane.setDividerPositions(0.1,0.4,1);
		centerPane.setOrientation(Orientation.VERTICAL);
		pane.setTop(control);
		pane.setCenter(centerPane);		
		pane.setBottom(keyboard);	
		System.out.println("Starting Up...");
		Scene scene=new Scene(pane);	
		/*scene.getStylesheets().add(
				this.getClass().getResource("/com/musekeys/midiplayer/Style.css").toExternalForm());*/

		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);

		/*primaryStage.setFullScreen(true);
		primaryStage.show();
		primaryStage.setFullScreen(false);
		primaryStage.setMaximized(false);*/
        //primaryStage.setMaximized(true);
		//sequencer.start();
		primaryStage.setOnCloseRequest(e -> {
			MIDIOUT.close();
			System.exit(0);
		});		
	}

	private void initMidi() throws InvalidMidiDataException, IOException, MidiUnavailableException {
		Preferences prefs=Preferences.userRoot();
		Preferences midiPref=prefs.node("musekeys/midi");
		String preferedDevice=midiPref.get("MIDI_OUT", "Gervill");
		
		Info[] devices=MidiSystem.getMidiDeviceInfo();
		for(int i=0;i<devices.length;i++){
			if(devices[i].getName().equals(preferedDevice))
				MIDIOUT=MidiSystem.getMidiDevice(devices[i]);
		}
		
		if(MIDIOUT==null)MIDIOUT=MidiSystem.getSynthesizer();
		
		//MIDIOUT=MidiSystem.getSynthesizer();
		//MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[10]);
		try{
		MIDIOUT.open();
		}catch(MidiUnavailableException e){
			System.out.println("Midi Device Not available.Using Default Synthesizer.\nException:");
			e.printStackTrace();
			MIDIOUT=MidiSystem.getSynthesizer();
			MIDIOUT.open();
		}
		if(MIDIOUT instanceof Synthesizer){		
			String soundfontPref=midiPref.get("SOUNDFONT", "");
			File file=(!soundfontPref.equals(""))?new File(soundfontPref):null;
			if(file!=null){
			Soundbank bank=MidiSystem.getSoundbank(file);
			//Soundbank bank=MidiSystem.getSoundbank(new File("G:/SoundFont/Stereo Piano.sf2"));
			//	synth.unloadAllInstruments(synth.getDefaultSoundbank());
			((Synthesizer) MIDIOUT).loadAllInstruments(bank);
			}
			//synth.unloadInstruments(bank1, patch);
			//synth.loadAllInstruments(bank);			
		}
	}	
	

	public static void main(String[] args) {
		launch(args);
	}

}
