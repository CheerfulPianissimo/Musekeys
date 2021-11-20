package com.musekeys.midiplayer;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sound.midi.*;

import com.musekeys.keyboard.Key;

public class MiniKeyboard extends BorderPane implements Receiver{
	private List<Key> whiteKeys;
	private List<Key> blackKeys;
	private static double keyWidth=10;	
	private static double keyHeight=keyWidth;
	private static double padding=keyWidth/3; 
	private Color pressedPaint=Color.BLUE;
	private Color releasedPaint=Color.LIGHTBLUE;
	private List<Key> keys;
	
	public MiniKeyboard(){
		super();
		whiteKeys = new ArrayList<Key>(52);
		blackKeys = new ArrayList<Key>(36);	
		setupKeys();
		addKeys();
		/*this.widthProperty().addListener(
			(ObservableValue<? extends Number> obv,Number oldValue,Number newValue)->{				
					keyWidth=newValue.doubleValue()/whiteKeys.size();
					keyHeight=keyWidth;
					updateKeyPositions();
		});*/
	}	
	

	public  Paint getPressedPaint() {
		return pressedPaint;
	}


	public  void setPressedPaint(Color pressedPaint) {
		this.pressedPaint = pressedPaint;
	}


	public Paint getReleasedPaint() {
		return releasedPaint;
	}


	public void setReleasedPaint(Color releasedPaint) {
		this.releasedPaint = releasedPaint;
		for(Key key:whiteKeys)
			key.setFill(releasedPaint);
		for(Key key:blackKeys)
			key.setFill(releasedPaint);
	}


	private void setupKeys(){
		whiteKeys.add(new Key(21,0)); // Lowest note-A0
		blackKeys.add(new Key(22,1));
		whiteKeys.add(new Key(23,0));
		for (int noteNo = 24; noteNo < 108; noteNo = noteNo + 12) { // Adding the 7 octaves
			whiteKeys.add(new Key(noteNo,0));
			blackKeys.add(new Key(noteNo + 1,1));
			whiteKeys.add(new Key(noteNo + 2,0));
			blackKeys.add(new Key(noteNo + 3,1));
			whiteKeys.add(new Key(noteNo + 4,0));
			whiteKeys.add(new Key(noteNo + 5,0));
			blackKeys.add(new Key(noteNo + 6,1));
			whiteKeys.add(new Key(noteNo + 7,0));
			blackKeys.add(new Key(noteNo + 8,1));
			whiteKeys.add(new Key(noteNo + 9,0));
			blackKeys.add(new Key(noteNo + 10,1));
			whiteKeys.add(new Key(noteNo + 11,0));
		}
		whiteKeys.add(new Key(108,0));
		keys=new ArrayList<Key>();
		keys.addAll(whiteKeys);
		keys.addAll(blackKeys);
		Collections.sort(keys, new Comparator<Key>(){
			@Override
			public int compare(Key key1, Key key2) {
				if(key1.getNoteNo()<key2.getNoteNo())
				return -1;
				else if(key1.getNoteNo()>key2.getNoteNo())
					return 1;
				else return 0;
			}			
		});
	}
	
	private void addKeys() {
		Pane pane=new Pane();
		pane.getChildren().addAll(whiteKeys);
		pane.getChildren().addAll(blackKeys);
		updateKeyPositions();
		this.setCenter(pane);
	}
	
	private void updateKeyPositions(){
		for(int co=0;co<whiteKeys.size();co++){
			Key key=whiteKeys.get(co);
			key.setHeight(keyHeight);
			key.setWidth(keyWidth);
			key.setX(calculateX(key));	
			key.setY(keyWidth+padding);
		}
		for(int co=0;co<blackKeys.size();co++){
			Key key=blackKeys.get(co);
			key.setHeight(keyHeight);
			key.setWidth(keyWidth);
			key.setX(calculateX(key));	
			key.setY(0);
		}
	}

	private double calculateX(Key orginalKey){		
		double totalX=0;
		int noteNo=0;	
		Key key=orginalKey;
		if(key.getType()==1)			
			for(int co=0;co<whiteKeys.size();co++){
				Key white=whiteKeys.get(co);
				if(white.getNoteNo()==(key.getNoteNo()-1)){
					key=white;					
					noteNo=whiteKeys.indexOf(key);
					break;
				}			
		}
		else
		noteNo=whiteKeys.indexOf(key);
		totalX=(keyWidth)*noteNo;
		if(orginalKey.getType()==1)			
			totalX=totalX+(keyWidth/2);	
		totalX=totalX+(padding*noteNo);
		return totalX;
	}

	public void simpleNoteOn(int data1){
		Key key=keys.get(data1-21);
		key.setFill(pressedPaint);
	}
	
	public void simpleNoteOff(int data1){
		Key key=keys.get(data1-21);
		key.setFill(releasedPaint);
	}
	
	@Override
	public void send(MidiMessage message, long timeStamp) {						
			if(message instanceof ShortMessage){					
				ShortMessage shortMsg=(ShortMessage)message;					
				if(shortMsg.getCommand()==ShortMessage.NOTE_ON||shortMsg.getCommand()==ShortMessage.NOTE_OFF){
					if(shortMsg.getData1()>=21&&shortMsg.getData1()<=108){
						int msgNo=shortMsg.getData1();	
						Key key=keys.get(msgNo-21);
						Platform.runLater(new Runnable(){
							public void run(){
							if(shortMsg.getCommand()==ShortMessage.NOTE_ON){								
								if(shortMsg.getCommand()==ShortMessage.NOTE_ON&&shortMsg.getData2()==0)
									key.setFill(releasedPaint);
								else
								 key.setFill(pressedPaint);
							}
							else 
								key.setFill(releasedPaint);	
							}});
						}																		
					}							
				}	
			}

	@Override
	public void close() {
		
	}	
	
	class Key extends Rectangle{
		private int noteNo;
		private int type=0;		
		
		public int getNoteNo() {
			return noteNo;
		}
		public int getType(){
			return type;
		}
		public Key(int noteNo,int type){
			super();
			this.setHeight(keyWidth);
			this.setWidth(keyWidth);			
			this.setStroke(Color.BLACK);
			this.setFill(releasedPaint);
			this.noteNo=noteNo;
			this.type=type;
		}		
	}

}
