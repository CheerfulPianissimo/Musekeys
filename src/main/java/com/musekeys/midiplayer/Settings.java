package com.musekeys.midiplayer;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Settings extends BorderPane{
	private @FXML TableView<MidiInfoModel> midiOutTable;
	private @FXML TableColumn<MidiInfoModel,String> nameOut,vendorOut,versionOut,desOut;
	private @FXML TableColumn<MidiInfoModel,Boolean> selectedOut;
	private @FXML TextField soundFont; 
	public Settings(){
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Settings.fxml"));
		fxmlLoader.setController(this);
		try {
			this.setCenter(fxmlLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		init();
	}

	private void init() {	
		Preferences prefs=Preferences.userRoot();
		Preferences midiPref=prefs.node("musekeys/midi");
		String currentDevice=midiPref.get("MIDI_OUT", "Gervill");
		
		Info[] devices=MidiSystem.getMidiDeviceInfo();
		for(int i=0;i<devices.length;i++){			
			Info device=devices[i];
			/*MidiDevice out=null;
			try {
				out=MidiSystem.getMidiDevice(device);
				out.open();
			} catch (MidiUnavailableException e1) {
				e1.printStackTrace();
			}
			if(!(out.getMaxReceivers()>0))continue;*/
			MidiInfoModel model=new MidiInfoModel();
			model.setName(device.getName());
			model.setVendor(device.getVendor());
			model.setVersion(device.getVersion());
			model.setDescription(device.getDescription());
			
			if(currentDevice==device.getName()) model.setSelected(true);
			model.selectedProperty().addListener(e->{
				if(model.isSelected())midiPref.put("MIDI_OUT", model.getName());
			});
			
			midiOutTable.getItems().add(model);			
		}
		
		nameOut.setCellValueFactory(new PropertyValueFactory<MidiInfoModel, String>("name"));
		vendorOut.setCellValueFactory(new PropertyValueFactory<MidiInfoModel, String>("vendor"));
		versionOut.setCellValueFactory(new PropertyValueFactory<MidiInfoModel, String>("version"));
		desOut.setCellValueFactory(new PropertyValueFactory<MidiInfoModel, String>("description"));
		selectedOut.setCellValueFactory(new PropertyValueFactory<MidiInfoModel, Boolean>("selected"));
		
		selectedOut.setCellFactory(CheckBoxTableCell.forTableColumn(selectedOut));
		selectedOut.setSortable(false);
		
		String soundfontPref=midiPref.get("SOUNDFONT", "");
		soundFont.setText(soundfontPref);
		soundFont.setEditable(false);
	}
	
	@FXML protected void openSoundFont(ActionEvent e){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("./"));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SoundFont2","*.sf2"),
				new FileChooser.ExtensionFilter("Downloadable Sounds Format","*.dls"));
		File file=fileChooser.showOpenDialog(null);
		if(file!=null){
			Preferences midiPref=Preferences.userRoot().node("musekeys/midi");
			midiPref.put("SOUNDFONT", file.getAbsolutePath());
		}
		soundFont.setText(file.getAbsolutePath());
		
		Preferences.userRoot().node("musekeys/midi").put("MIDI_OUT", "Gervill");
	}

	public static void openInWindow() {
		Settings settings=new Settings();
		Stage stage=new Stage(StageStyle.UTILITY);
		stage.setScene(new Scene(settings));
		stage.show();
		stage.setOnCloseRequest(e->{
			stage.close();
		});
	}
}
