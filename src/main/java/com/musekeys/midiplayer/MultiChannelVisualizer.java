package com.musekeys.midiplayer;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class MultiChannelVisualizer extends VBox implements MidiVisualizer{
	private MiniKeyboard[] keyboards;
	private Color[] colors = { Color.ORANGE, Color.LIGHTBLUE, Color.RED, Color.BROWN, Color.GREEN, Color.YELLOW,
			Color.VIOLET, Color.KHAKI, Color.PURPLE, Color.LIGHTGRAY, Color.PLUM, Color.TURQUOISE, Color.SPRINGGREEN,
			Color.INDIGO, Color.TAN, Color.BURLYWOOD, Color.TOMATO };	
	public MultiChannelVisualizer(){
		super();
		keyboards=new MiniKeyboard[16];	
		this.setSpacing(10);
		for(int co=0;co<16;co++){				
			keyboards[co]=new MiniKeyboard();			
			this.getChildren().add(keyboards[co]);
		}
		refreshColors();
	}
	@Override
	public void send(MidiMessage message, long timeStamp) {
		if(message instanceof ShortMessage){					
			ShortMessage shortMsg=(ShortMessage)message;					
			if(shortMsg.getCommand()==ShortMessage.NOTE_ON||shortMsg.getCommand()==ShortMessage.NOTE_OFF){
				if(shortMsg.getData1()>=21&&shortMsg.getData1()<=108){
					int msgNo=shortMsg.getData1();
					int channelNo=shortMsg.getChannel();
					MiniKeyboard keyboard=keyboards[channelNo];
					Platform.runLater(new Runnable(){
						public void run(){
						if(shortMsg.getCommand()==ShortMessage.NOTE_ON){								
							if(shortMsg.getData2()==0)
								keyboard.simpleNoteOff(msgNo);
							else
							 keyboard.simpleNoteOn(msgNo);
						}
						else 
							keyboard.simpleNoteOff(msgNo);
					}});					
				}
			}
		}
	}
	@Override
	public void close() {
		
	}
	@Override
	public Color getChannelColor(int channelNo) {
		return colors[channelNo];
	}
	@Override
	public void setChannelColor(int channelNo, Color paint) {
		colors[channelNo]=paint;
		refreshColors();
	}
	
	private void refreshColors(){
		for(int co=0;co<keyboards.length;co++){
			keyboards[co].setReleasedPaint(Color.DODGERBLUE);
			keyboards[co].setPressedPaint(colors[co]);
		}
	}
	public Receiver getReceiver() {
		return this;
	}
	
}
