package com.musekeys.midiplayer;


import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class MidiChannelModel{
	private SimpleIntegerProperty channelNo;
	private SimpleIntegerProperty instrument,pitchBend;
	private SimpleIntegerProperty[] controllers;
	private List<SimpleIntegerProperty> controllerBinders;
	private List<SimpleIntegerProperty> controllerBindValue;
	private SimpleBooleanProperty muted,solo;
	private SimpleObjectProperty<Color> color;
	
	public Color getColor() {
		return color.get();
	}
	public void setColor(Color color) {
		this.color.set(color);
	}
	
	public SimpleObjectProperty<Color> colorProperty(){
		return color;
	}
	public IntegerProperty channelNoProperty(){
		return channelNo;
	}
	public IntegerProperty instrumentProperty(){
		return instrument;
	}
	public IntegerProperty controllerProperty(int co){
		if(controllers[co]==null){
			if(co==7)
				controllers[co]=new SimpleIntegerProperty(100);
			else if(co==11)
				controllers[co]=new SimpleIntegerProperty(127);
			else if(co==10)
				controllers[co]=new SimpleIntegerProperty(64);
			else
			controllers[co]=new SimpleIntegerProperty(0);
		}
		return controllers[co];
	}	
	
	public void refresh(){
		setInstrument(0);
		setPitchBend(8256);
		for(int co=0;co<127;co++){
			if(controllers[co]!=null){
				if(co==7)
					controllers[co].set(100);
				else if(co==11)
					controllers[co].set(127);
				else if(co==10)
					controllers[co].set(64);
				else
				controllers[co].set(0);
			}
		}
	}
	
	public MidiChannelModel(int channel){
		this.channelNo=new SimpleIntegerProperty(channel);
		this.instrument=new SimpleIntegerProperty(-1);		
		controllers=new SimpleIntegerProperty[128];
		controllerBinders=new ArrayList<SimpleIntegerProperty>();
		controllerBindValue=new ArrayList<SimpleIntegerProperty>();
		for(int co=0;co<2;co++){
			controllerBinders.add(new SimpleIntegerProperty(0));
			controllerBindValue.add(new SimpleIntegerProperty(0));
		}			
		muted=new SimpleBooleanProperty(true);
		solo=new SimpleBooleanProperty(false);
		color=new SimpleObjectProperty<Color>();
		pitchBend=new SimpleIntegerProperty(8256);
	}
	public Integer getChannelNo() {
		return channelNo.get();
	}
	public void setChannelNo(Integer channelNo) {
		this.channelNo.set(channelNo);
	}	
	public void setController(int controllerNo,int controllerValue){
		controllerProperty(controllerNo).set(controllerValue);		
	}
	public Integer getControllerValue(int controllerNo){
		return controllerProperty(controllerNo).get();
	}
	public Integer getInstrument() {
		return instrument.get();
	}
	public void setInstrument(Integer instrument) {
		this.instrument.set(instrument);
	}	
	
	public void setControllerForColumn(int controller,int column) {			
		controllerProperty(getControllerValueForColumn(column).get()).unbindBidirectional(
				getControllerForColumn(column));
		this.getControllerForColumn(column).unbindBidirectional(
				controllerProperty(getControllerValueForColumn(column).get()));
		this.getControllerValueForColumn(column).set(controller);
		this.getControllerForColumn(column).set(controllerProperty(controller).get());
		this.getControllerForColumn(column).bindBidirectional(controllerProperty(controller));
	}
		
	public  SimpleIntegerProperty getControllerForColumn(int col){
		return controllerBinders.get(col);
	}
	public  SimpleIntegerProperty getControllerValueForColumn(int col){
		return controllerBindValue.get(col);
	}	
	public SimpleBooleanProperty mutedProperty() {
		return this.muted;
	}
	
	public boolean getMuted() {
		return this.mutedProperty().get();
	}
	
	public void setMuted(boolean isEnabled) {
		this.mutedProperty().set(isEnabled);
	}
	public SimpleIntegerProperty pitchBendProperty() {
		return this.pitchBend;
	}
	
	public int getPitchBend() {
		return this.pitchBendProperty().get();
	}
	
	public void setPitchBend(final int pitchBend) {
		this.pitchBendProperty().set(pitchBend);
	}
	public SimpleBooleanProperty soloProperty() {
		return this.solo;
	}
	
	public boolean isSolo() {
		return this.soloProperty().get();
	}
	
	public void setSolo(final boolean solo) {
		this.soloProperty().set(solo);
	}
	
	
	
	
}
