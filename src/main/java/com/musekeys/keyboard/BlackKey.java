package com.musekeys.keyboard;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

public class BlackKey extends Key {
	private static LinearGradient releasedGradient=getReleasedGradient(Color.BLACK);
	private boolean useEffects=false;

	public boolean getUseEffects() {
		return useEffects;
	}

	public void setUseEffects(boolean useEffects) {
		this.useEffects = useEffects;
		setFill(Color.BLACK);
	}
	
	public BlackKey(int noteNo,Receiver receiver) throws MidiUnavailableException, InvalidMidiDataException {
		super(noteNo, receiver);	
		setupKeyAppearence();
	}

	private void setupKeyAppearence() {				
		setStroke(Color.BLACK);	
		setFill(Color.BLACK);		
	}

	public void setFill(Color color){
		if(useEffects){
		this.setFill(getReleasedGradient(color));
		}else super.setFill(color);
	}
	
	static LinearGradient getReleasedGradient(Color color){
		LinearGradient gradient=new LinearGradient(0, 0, 0, 1, true,
				CycleMethod.NO_CYCLE, new Stop(0, color),
				new Stop(0.89,color.deriveColor(0,1,6,1)),new Stop(0.92,color.deriveColor(0,1,16,1))
				,new Stop(0.93,color.deriveColor(0, 1, 0.5, 1)),new Stop(0.94,color)
				,new Stop(0.95,color.deriveColor(0,1,9.5, 1)),new Stop(0.99,color));
		return gradient;
	}
	
	@Override
	public void pressKey() {		
		pressKey(Color.rgb(20, 20, 20));
	}
	
	
	public void pressKey(Color color) {	
		if(useEffects){
		LinearGradient gradient=new LinearGradient(0, 0, 0, 1, true,
				CycleMethod.NO_CYCLE, new Stop(0, color),
				new Stop(0.91,color.deriveColor(0,1,6,1)),new Stop(0.94,color.deriveColor(0,1,16,1))
				,new Stop(0.95,color.deriveColor(0, 1, 0.5, 1)),new Stop(0.96,color)
				,new Stop(0.97,color.deriveColor(0,1,9.5, 1)),new Stop(1.0,color));
		this.setFill(gradient);
		}else super.setFill(color);
	}

	public void pressKey(Paint paint) {	
		super.setFill(paint);
	}
	
	@Override
	public void releaseKey() {	
		if(useEffects)
		setFill(releasedGradient);
		else setFill(Color.BLACK);
	}			
	
	@Override
	public int getType() {
		return 1;
	}
}
