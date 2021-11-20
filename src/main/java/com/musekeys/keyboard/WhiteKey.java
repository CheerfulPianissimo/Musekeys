package com.musekeys.keyboard;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class WhiteKey extends Key {	
	public WhiteKey(int noteNo, Receiver receiver) throws MidiUnavailableException, InvalidMidiDataException {
		super(noteNo,receiver);	
		setupKeyAppearence();
	}

	private void setupKeyAppearence() {			
		setFill(Color.WHITE);
		setStroke(Color.BLACK);	
	}

	@Override
	public void pressKey() {	
		pressKey(Color.rgb(150, 150, 150));
	}

	@Override
	public void releaseKey() {			
		pressKey(Color.WHITE);		
	}

	@Override
	public void pressKey(Paint color) {
		this.setFill(color);
	}

	@Override
	public int getType() {
		return 0;
	}	
}