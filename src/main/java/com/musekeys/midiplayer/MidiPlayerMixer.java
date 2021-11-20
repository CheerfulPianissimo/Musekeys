package com.musekeys.midiplayer;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import com.musekeys.keyboard.Keyboard;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tooltip;

public class MidiPlayerMixer {
	private Receiver receiver;
	private Transmitter transmitter; 
	private List<Receiver> toDevices;
	private ChannelViewer viewer;
	private MidiVisualizer midiVisualizer;
	private SimpleBooleanProperty[] channelStat;
	private int solo=-1;
	
	public MidiPlayerMixer(){
		toDevices=new ArrayList<Receiver>();
		channelStat=new SimpleBooleanProperty[16];
		for(int co=0;co<16;co++)
			channelStat[co]=new SimpleBooleanProperty(true);
		setupMidi();
	}
	
	public MidiPlayerMixer(ChannelViewer viewer,MidiVisualizer visualizer){
		this();
		this.setViewer(viewer);
		this.setMidiVisualizer(visualizer);
	}

	public Transmitter getTransmitter() {
		return transmitter;
	}

	private void setupMidi() {
		receiver=new Receiver(){
			@Override
			public void send(MidiMessage message, long timeStamp) {				
				if(message instanceof ShortMessage){
					ShortMessage shortMsg=(ShortMessage)message;
					if(channelStat[shortMsg.getChannel()].get()==false)
						return;
					if(shortMsg.getCommand()==ShortMessage.NOTE_ON||
							shortMsg.getCommand()==ShortMessage.NOTE_OFF){
						if(midiVisualizer!=null)
						midiVisualizer.send(message, timeStamp);
					}else if(shortMsg.getCommand()==ShortMessage.PROGRAM_CHANGE||
							shortMsg.getCommand()==ShortMessage.CONTROL_CHANGE){
						if(viewer!=null)
						viewer.getReceiver().send(message, 0);
					}
				}
				for(Receiver toDevice:toDevices)
					toDevice.send(message, timeStamp);
			}

			@Override
			public void close() {				
			}			
		};
		transmitter=new Transmitter(){
			@Override
			public void setReceiver(Receiver receiver) {
				toDevices.add(receiver);
			}
			@Override
			public Receiver getReceiver() {
				return toDevices.get(0);
			}
			@Override
			public void close() {
				for(int co=0;co<toDevices.size();co++){
					toDevices.remove(co);
				}					
			}			
		};
	}
	
	public void setMuteChannel(int channel,boolean shouldMute){		
		if(shouldMute==false){
			try {
				for(int co=0;co<128;co++){
					ShortMessage noteOffMsg = new ShortMessage(ShortMessage.NOTE_OFF, 0, co, 127);
					this.receiver.send(noteOffMsg, -1);
				}					
				ShortMessage allSoundOff=new ShortMessage(ShortMessage.CONTROL_CHANGE,channel,123,127);
				this.receiver.send(allSoundOff, -1);				
			} catch (InvalidMidiDataException e) {}
		}	
		channelStat[channel].set(shouldMute);
	}
	
	public boolean getIsMute(int channel){
		return channelStat[channel].get();
	}
	
	public void setSoloChannel(int channel,boolean shouldSolo){
		for(int co=0;co<16;co++)
			if(shouldSolo)
			setMuteChannel(co,false);
			else setMuteChannel(co,true);
		channelStat[channel].set(true);;
		this.solo=channel;
	}
	
	public int getSoloChannel(){
		return solo;
	}
	
	public ChannelViewer getViewer() {
		return viewer;
	}

	public void setViewer(ChannelViewer viewer) {
		this.viewer = viewer;
		List<MidiChannelModel> channels=viewer.getChannels();
		for(int co=0;co<channels.size();co++){
			final int co2=co;
			channels.get(co).mutedProperty().addListener(
				(ObservableValue<? extends Boolean> observable,Boolean oldValue, Boolean newValue)->{
					this.setMuteChannel(co2, newValue);
				});
		}
	}

	public MidiVisualizer getMidiVisualizer() {
		return midiVisualizer;
	}

	public void setMidiVisualizer(MidiVisualizer visualizer) {
		this.midiVisualizer = visualizer;
		if(visualizer instanceof Keyboard&&viewer!=null)
			try {
				((Keyboard) visualizer).getTransmitter().setReceiver(viewer.getReceiver());
			} catch (MidiUnavailableException e) {				
				e.printStackTrace();
			}
	}

	public Receiver getReceiver(){
		return receiver;
	}
}
