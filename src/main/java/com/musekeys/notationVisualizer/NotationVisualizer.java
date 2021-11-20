package com.musekeys.notationVisualizer;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class NotationVisualizer extends Pane {
	private List<Line> lines;
	private static double spacing=20;
	private Receiver receiver=new Receiver(){
		@Override
		public void send(MidiMessage message, long timeStamp) {						
				if(message instanceof ShortMessage){					
					ShortMessage shortMsg=(ShortMessage)message;					
					if(shortMsg.getCommand()==ShortMessage.NOTE_ON||shortMsg.getCommand()==ShortMessage.NOTE_OFF){
						if(shortMsg.getData1()>=21&&shortMsg.getData1()<=108){
							int msgNo=shortMsg.getData1();	
							Note key=new Note();
							}																		
						}							
					}
				}
		@Override
		public void close() {
		}
	};
	
	public NotationVisualizer(){
		super();
		lines=new ArrayList<Line>(16);
		for(int co=0;co<16;co++){
			Line line=new Line();
			if(co==16||co==10||co<4)line.setStroke(Color.LIGHTGRAY);
			line.setStartX(0);
			line.setStartY(spacing*(co+1));
			line.endXProperty().bind(this.widthProperty());
			line.setEndY(line.getStartY());
			line.setStrokeWidth(2.0);
			lines.add(line);
			this.getChildren().add(line);
		}		
		/*ImageView treble=new ImageView();
		treble.setImage(new Image(this.getClass().getResourceAsStream("treble.png")));
		ImageView bass=new ImageView();
		bass.setImage(new Image(this.getClass().getResourceAsStream("bass.png")));
		treble.setY((spacing*5)+spacing);
		this.getChildren().addAll(treble,bass);*/
	}	
	
	private double findY(int noteNo){
		int octaveNo=(noteNo/12)-1;
		int keyNo=noteNo%12;
		
		return noteNo;		
	}
	
	String getNoteLetter(int octNo){
		int note=octNo;
		boolean isSharp=isSharp(octNo);
		if(isSharp)
			note=octNo-1;
		char character=(char)(note+68);
		String letter=null;
		if(isSharp)
			letter=character+"#";
		else
			letter=String.valueOf(character);
		return letter;		
	}
	
	boolean isSharp(int octNote){
		if(octNote==1||octNote==3||octNote==6||octNote==8||octNote==9)
			return true;
		else return false;
	}
	
}

class Note extends StackPane{
	public Note(){
		super();
		Circle circle=new Circle(5,Color.BLACK);
		this.getChildren().add(circle);
	}
}
