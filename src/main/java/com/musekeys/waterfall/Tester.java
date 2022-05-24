package com.musekeys.waterfall;

import java.io.File;

import javax.sound.midi.*;

import com.musekeys.karaoke.LyricsViewer;
import com.musekeys.keyboard.Keyboard;
import com.musekeys.midiplayer.MidiPlayerControl;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Tester extends Application {
	static double sceneHeight=0;
	@Override
	public void start(Stage primaryStage) throws Exception {
		reverseTest(primaryStage);
		/*normalTest(primaryStage);
		simpleTest(primaryStage);*/
	}

	private static void reverseTest(Stage primaryStage) throws Exception{
		BorderPane pane=new BorderPane();
		MidiDevice synth=MidiSystem.getSynthesizer();
		//MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[10]);
		synth.open();
		Keyboard keyboard=new Keyboard();
		keyboard.setMidiThru(false);
		//keyboard.setUseEffects(false);
		keyboard.getTransmitter().setReceiver(synth.getReceiver());

		//File file = new File("/mnt/D/midi/.mid");
		FileChooser fileChooser=new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MIDI files", "*.mid","*.kar"),
				new FileChooser.ExtensionFilter("All Files", "*.*")
		);
		//fileChooser.setInitialDirectory(new File("/mnt/D/midi/"));
		File file = fileChooser.showOpenDialog(primaryStage);
		Sequence sequence=MidiSystem.getSequence(file);
		for (Track track: sequence.getTracks()){
			//sequence.deleteTrack(track);
		}

		//Sequence sequence=new Sequence(Sequence.PPQ,1);
		Track recTrack=sequence.createTrack();
		Sequencer sequencer =//new JavaSequencer(null);
				MidiSystem.getSequencer(false);
		sequencer.getTransmitter().setReceiver(keyboard.getReceiver());
		sequencer.getTransmitter().setReceiver(synth.getReceiver());
		//keyboard.getTransmitter().setReceiver(sequencer.getReceiver());
		sequencer.setSequence(sequence);
		sequencer.open();
		sequencer.recordEnable(recTrack,-1);
		//sequencer.startRecording();
		sequencer.start();


		WaterFall waterFall = new WaterFall(sequencer);
		keyboard.getTransmitter().setReceiver(waterFall.getReceiver());
		waterFall.visibleWhiteKeysProperty().bindBidirectional(keyboard.visibleWhiteKeysProperty());
		waterFall.setOnMouseClicked(e->{

		});
		/*waterFall.setOnMouseClicked(e->{
			if(e.getButton()==MouseButton.SECONDARY){
			Stage player=new Stage(StageStyle.UTILITY);
			player.initOwner(primaryStage);
			player.setScene(new Scene(new BorderPane(new MidiPlayerControl(sequencer))));
			player.show();
			}
		});*/
		/*long length=sequencer.getMicrosecondLength();
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.006),
			event ->{
			  waterFall.relocate(0,
					-((length-sequencer.getMicrosecondPosition())/WaterFall.SCALEFACTOR)+sceneHeight);
	     });
	    Timeline timeline = new Timeline(keyFrame);
	    timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();*/
		//StackPane waterFallpane =new StackPane(/*new LyricsViewer(sequence, sequencer),*/waterFall);
		BorderPane waterFallPane=new BorderPane();
		waterFallPane.setCenter(waterFall);
		waterFallPane.setBottom(keyboard);
		keyboard.visibleWhiteKeysProperty().addListener(e->{
			//if(((BorderPane)keyboard).getCenter() instanceof ScrollPane){
			((ScrollPane)((BorderPane)keyboard).getCenter()).hvalueProperty().bindBidirectional(
					((ScrollPane)((StackPane)waterFall).getChildren().get(0)).hvalueProperty());
			//}
		});
		pane.setCenter(waterFallPane);
		pane.setTop(new MidiPlayerControl(sequencer));
		/*waterFall.setCache(true);
		waterFall.setCacheHint(CacheHint.SPEED);*/
		Scene scene=new Scene(pane);
		primaryStage.setScene(scene);
		//primaryStage.setFullScreen(false);
		primaryStage.show();
		//primaryStage.setMaximized(true);
		/*sceneHeight=waterFallpane.getHeight();
		waterFallpane.heightProperty().addListener(e->{sceneHeight=waterFallpane.getHeight();});*/
		primaryStage.setOnCloseRequest(e -> {
			synth.close();
			System.exit(0);
		});
	}

	private static void normalTest(Stage primaryStage) throws Exception{
		BorderPane pane=new BorderPane();		
		MidiDevice synth=MidiSystem.getSynthesizer();
				//MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[10]);
		synth.open();
		Keyboard keyboard=new Keyboard();
		//keyboard.setUseEffects(false);
		keyboard.getTransmitter().setReceiver(synth.getReceiver());
		FileChooser fileChooser=new FileChooser();		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MIDI files", "*.mid","*.kar"),
				new FileChooser.ExtensionFilter("All Files", "*.*")					
				);
		fileChooser.setInitialDirectory(new File("/mnt/D/midi/"));
		File file = fileChooser.showOpenDialog(primaryStage);
		Sequence sequence=MidiSystem.getSequence(file);
		Sequencer sequencer =//new JavaSequencer(null);
		MidiSystem.getSequencer(false);			
		sequencer.getTransmitter().setReceiver(keyboard.getReceiver());
		sequencer.getTransmitter().setReceiver(synth.getReceiver());
		sequencer.setSequence(sequence);
		sequencer.open();	    
		WaterFall waterFall = new WaterFall(sequencer);
		waterFall.visibleWhiteKeysProperty().bindBidirectional(keyboard.visibleWhiteKeysProperty());
		/*waterFall.setOnMouseClicked(e->{
			if(e.getButton()==MouseButton.SECONDARY){
			Stage player=new Stage(StageStyle.UTILITY);
			player.initOwner(primaryStage);
			player.setScene(new Scene(new BorderPane(new MidiPlayerControl(sequencer))));
			player.show();
			}
		});*/
		/*long length=sequencer.getMicrosecondLength();			
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.006), 
			event ->{			   	 	
			  waterFall.relocate(0,
					-((length-sequencer.getMicrosecondPosition())/WaterFall.SCALEFACTOR)+sceneHeight);
	     });
	    Timeline timeline = new Timeline(keyFrame);
	    timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();*/		
		//StackPane waterFallpane =new StackPane(/*new LyricsViewer(sequence, sequencer),*/waterFall);
		BorderPane waterFallPane=new BorderPane();
		waterFallPane.setCenter(waterFall);
		waterFallPane.setBottom(keyboard);
		keyboard.visibleWhiteKeysProperty().addListener(e->{
			//if(((BorderPane)keyboard).getCenter() instanceof ScrollPane){
				((ScrollPane)((BorderPane)keyboard).getCenter()).hvalueProperty().bindBidirectional(
						((ScrollPane)((StackPane)waterFall).getChildren().get(0)).hvalueProperty());
			//}
		});
		pane.setCenter(waterFallPane);
		pane.setTop(new MidiPlayerControl(sequencer));
		/*waterFall.setCache(true);
		waterFall.setCacheHint(CacheHint.SPEED);*/
		Scene scene=new Scene(pane);		
		primaryStage.setScene(scene);		
		primaryStage.setFullScreen(false);	
		primaryStage.show();		
		primaryStage.setMaximized(true);				
		/*sceneHeight=waterFallpane.getHeight();
		waterFallpane.heightProperty().addListener(e->{sceneHeight=waterFallpane.getHeight();});*/
		primaryStage.setOnCloseRequest(e -> {
			synth.close();
			System.exit(0);
		});
	}

	private static void simpleTest(Stage primaryStage) throws Exception{
		BorderPane pane=new BorderPane();		
		Synthesizer synth=MidiSystem.getSynthesizer();
		synth.open();
		Keyboard keyboard=new Keyboard();
		keyboard.getTransmitter().setReceiver(synth.getReceiver());
		FileChooser fileChooser=new FileChooser();		
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MIDI files", "*.mid"),
				new FileChooser.ExtensionFilter("All Files", "*.*")					
				);
		fileChooser.setInitialDirectory(new File("/mnt/D/midi"));
		File file = fileChooser.showOpenDialog(primaryStage);
	    Sequence sequence=MidiSystem.getSequence(file);
	    Sequencer sequencer=MidiSystem.getSequencer(false);
	    sequencer.getTransmitter().setReceiver(synth.getReceiver());
	    sequencer.setSequence(sequence);
	    sequencer.open();
		WaterFall waterFall = new WaterFall(sequencer);

		pane.setCenter(waterFall);
		//pane.setBottom(keyboard);
		Scene scene=new Scene(pane);		
		primaryStage.setScene(scene);		
		primaryStage.setFullScreen(false);	
		primaryStage.show();
		sequencer.start();
		primaryStage.setMaximized(true);

		primaryStage.setOnCloseRequest(e -> {
			System.exit(0);
		});
	}
	
	public static void main(String[] args) {
		launch();
	}	
}


