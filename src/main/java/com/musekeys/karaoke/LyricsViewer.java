package com.musekeys.karaoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class LyricsViewer extends BorderPane{
	private Text done,next;
	private String fullLyrics="";
	private ArrayList<LyricPosition> lyrics;
	private Sequence sequence;
	private ScrollPane lyricsPane;
	
	public LyricsViewer(Sequence sequence,Sequencer sequencer){
		this.sequence=sequence;
		lyrics=new ArrayList<LyricPosition>();
		next=new Text();
		done=new Text();
		findLyrics();
		Collections.sort(lyrics, new Comparator<LyricPosition>(){
			@Override
			public int compare(LyricPosition o1, LyricPosition o2) {
				if(o1.getTick()<o2.getTick())return -1;
				else if(o1.getTick()>o1.getTick())return 1;
				else return 0;
			}			
		});
		done.setFill(Color.RED);
		next.setFill(Color.BLUE);
		Font font=Font.font(50);
		done.setFont(font);
		next.setFont(font);
		setup();
		sequencer.addMetaEventListener(message->{			
				if(message.getMessage()[1]<7){					
					long currentTick=sequencer.getTickPosition();				
					final String doneTxt;	
					LyricPosition event;
					int position;
					for(int co=0;;co++){
						event=lyrics.get(co);
						if(event.tick<=currentTick);
						else {
							position=event.getPosition();
							break;
						}
					}		
					doneTxt=fullLyrics.substring(0,position);					
					Platform.runLater(()->{
						done.setText(doneTxt);
						next.setText(fullLyrics.substring(position, fullLyrics.length()));
						updatePosition();
					});					
				}
		});
	}
	
	private void updatePosition(){
		double height=done.getBoundsInLocal().getMaxY();
		lyricsPane.setVmax(next.getBoundsInLocal().getHeight()+done.getBoundsInLocal().getHeight());
		//+this.getHeight()/2;		
		lyricsPane.setVvalue(height);
		/*long length=fullLyrics.length();
		long pos=done.getText().length();
		double percent=(pos*100)/length;
		lyricsPane.setVvalue(percent);*/
	}
	
	private void setup() {
		TextFlow lyrics=new TextFlow(done,next);
		lyrics.setTextAlignment(TextAlignment.CENTER);
		lyricsPane=new ScrollPane();
		//lyricsPane.setVmax(90);		
		lyricsPane.setFitToWidth(true);		
		lyricsPane.setContent(lyrics);
		this.setCenter(lyricsPane);
	}
	
	private void findLyrics() {
		Track[] tracks=sequence.getTracks();
		for (int co1 = 0; co1 < tracks.length; co1++) {			
			for (int co = 0; co < tracks[co1].size(); co++) {
				MidiEvent event = tracks[co1].get(co);
				MidiMessage message = event.getMessage();
				if(message instanceof MetaMessage){
					if(message.getMessage()[1]<7){
						String lyric=getLyric((MetaMessage)message);
						lyrics.add(new LyricPosition(event.getTick(),fullLyrics.length()));
						fullLyrics=fullLyrics+lyric;
						next.setText(next.getText()+lyric);
					}
				}
			}
		}
	}
	
	private String getLyric(MetaMessage meta){		
		String lyric="";
		if(meta.getMessage()[3]=='@')return ""; //skip info
		for(int co=3;co<meta.getMessage().length;co++){
			if((char)meta.getMessage()[co]=='/'){ //new line
				lyric=lyric+"\n";continue;
			}else
			if((char)meta.getMessage()[co]=='\\'){ //new paragraph
				lyric=lyric+"\n\n";continue;
			}else
			lyric=lyric+(char)meta.getMessage()[co];
		}		
		return lyric;
	}
	

	class LyricPosition{
		public int getPosition(){
			return position;
		}
		public long getTick() {
			return tick;
		}
		private int position;
		private long tick;
		LyricPosition(long tick,int position){
			this.tick=tick;
			this.position=position;
		}
	}
}
