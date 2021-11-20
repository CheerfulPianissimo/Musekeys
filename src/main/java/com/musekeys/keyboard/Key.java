package com.musekeys.keyboard;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * This abstract class is the basis of a key.
 * It produces a sound when clicked. 
 * A key that extends this class just has to define its appearance.
 * The sound and effects already been defined.
 */
public abstract class Key extends Rectangle {		
	private Receiver receiver;
	
	private static EventHandler<MouseEvent> mousePressed=new EventHandler<MouseEvent>(){		
		@Override
		public void handle(MouseEvent event) {
			Key key=(Key)event.getSource();
			key.pressKey();
			key.playNote();
		}		
	};
	
	private static EventHandler<MouseEvent> mouseReleased=new EventHandler<MouseEvent>(){		
		@Override
		public void handle(MouseEvent event) {
			Key key=(Key)event.getSource();
			key.releaseKey();
			key.stopNote();
		}		
	};
	
	private byte noteNo,channel,transpose;	

	public byte getTranspose() {
		return transpose;
	}

	public void setTranspose(byte transpose) {
		this.transpose = transpose;
	}

	public void setNoteNo(byte noteNo) {
		this.noteNo = noteNo;
	}

	public byte getChannel() {
		return channel;
	}

	public void setChannel(byte channel) {
		this.channel = channel;
	}

	public Key(int noteNo,Receiver receiver) throws MidiUnavailableException, InvalidMidiDataException{	
		super();	       
		if(receiver!=null)
		this.receiver=receiver;
		else
			this.receiver=new Receiver(){
				@Override
				public void send(MidiMessage message, long timeStamp) {
					// DO nothing					
				}
				@Override
				public void close() {
										
				}			
		};	
		this.noteNo=(byte) noteNo;		
		setupKeySound();		
	}		
	
	private void setupKeySound() {		
		this.setOnMousePressed(mousePressed);		
		this.setOnMouseReleased(mouseReleased);		
		this.setOnMouseDragEntered(mousePressed);		
		this.setOnMouseDragExited(mouseReleased);
	}	
	
	public abstract void pressKey();
	
	public abstract void releaseKey();	
	
	public int getNoteNo(){
		return noteNo;
	}
	
	public void playNote(int strength) throws InvalidMidiDataException{		
		receiver.send(new ShortMessage(ShortMessage.NOTE_ON, channel, noteNo+transpose, strength),-1);
	}
	
	public void playNote(){			
		ShortMessage noteOnMsg=null;	
		try {
			noteOnMsg = new ShortMessage(ShortMessage.NOTE_ON, channel, noteNo+transpose, 100);
		} catch (InvalidMidiDataException e) {			
		}		
		receiver.send(noteOnMsg, -1);		
	}
	
	public void stopNote(){	
		ShortMessage noteOffMsg=null;	
		try {
			noteOffMsg = new ShortMessage(ShortMessage.NOTE_OFF, channel, noteNo+transpose, 100);
		} catch (InvalidMidiDataException e) {			
		}
		receiver.send(noteOffMsg, -1);	
	}

	public void setReceiver(Receiver receiver){
		this.receiver=receiver;
	}
	
	abstract public void pressKey(Paint color);
	
	abstract public int getType();
}	
