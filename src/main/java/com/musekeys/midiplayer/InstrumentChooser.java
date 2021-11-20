package com.musekeys.midiplayer;

import com.musekeys.midi.GMInstruments;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class InstrumentChooser extends BorderPane{
	private ListView<String> category,program;
	private @FXML SplitPane listPane;
	private @FXML TextField search;
	private int categoryNo;
	private SimpleIntegerProperty instrument;
	private SimpleBooleanProperty okPressed;
	public InstrumentChooser(){
		super();
		category=new ListView<String>();
		program=new ListView<String>();
		instrument=new SimpleIntegerProperty(0);
		okPressed=new SimpleBooleanProperty(false);
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("InstrumentChooser.fxml"));
		fxmlLoader.setController(this);
		try {
			this.setCenter(fxmlLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		setup();
	}
	
	public InstrumentChooser(int programNo){
		this();
		setInstrument(-1);
		setInstrument(programNo);
	}
	
	private void setup() {			
		category.getItems().addAll(GMInstruments.instrumentCategeories);
		category.getSelectionModel().selectedIndexProperty().addListener(
			(ObservableValue<? extends Number> value,Number oldValue,Number newValue)->{
				int rangeLower=newValue.intValue()*8,rangeUpper=rangeLower+8;	
				program.getItems().clear();
				categoryNo=newValue.intValue();				
				for(int co=rangeLower;co<rangeUpper;co++){
					program.getItems().add(co+": "+ GMInstruments.Instruments[co]);
				}				
				if(getInstrument()/8==categoryNo)
					program.getSelectionModel().clearAndSelect(getInstrument()%8);
			});
		program.getSelectionModel().selectedIndexProperty().addListener(
				(ObservableValue<? extends Number> value,Number oldValue,Number newValue)->{
					if(program.getItems().size()!=0)
					instrument.set((categoryNo*8)+newValue.intValue());
				});
		listPane.getItems().addAll(category,program);
		instrumentProperty().addListener(
			(ObservableValue<? extends Number> value,Number oldValue,Number newValue)->{
				category.getSelectionModel().clearAndSelect(newValue.intValue()/8);
				program.getSelectionModel().clearAndSelect(newValue.intValue()%8);
			});
		category.getSelectionModel().clearAndSelect(0);
		program.getSelectionModel().clearAndSelect(0);
		search.textProperty().addListener((ObservableValue<? extends String> value,String oldValue,String newValue)->{
		    for(int i = 0; i< GMInstruments.Instruments.length; i++){
		        if((String.valueOf(i)+ GMInstruments.Instruments[i].toLowerCase())
                                    .contains(newValue.toLowerCase())){
                    category.getSelectionModel().clearAndSelect(i/8);
                    program.getSelectionModel().clearAndSelect(i%8);
                    search.setStyle("-fx-text-fill:blue");
                    return;
                }
            }
            search.setStyle("-fx-text-fill:red");
        });
		this.visibleProperty().addListener(
		        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)->
                search.requestFocus());
	}

	public final SimpleIntegerProperty instrumentProperty() {
		return this.instrument;
	}
	

	public final int getInstrument() {
		return this.instrumentProperty().get();
	}
	

	public final void setInstrument(int instrument) {
		this.instrumentProperty().set(instrument);
	}
	
	@FXML protected void okPressed(ActionEvent event){
		okPressed.set(true);
	}

	public SimpleBooleanProperty okPressedProperty() {
		return this.okPressed;
	}
	

	public boolean isOkPressed() {
		return this.okPressedProperty().get();
	}
	

	public void setOkPressed(final boolean okPressed) {
		this.okPressedProperty().set(okPressed);
	}
	
	
}
