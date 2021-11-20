package com.musekeys.waterfall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.musekeys.midi.MidiUtils;
import com.musekeys.midi.MidiUtils.TempoCache;
import com.musekeys.waterfall.WaterFall.Mode;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class NoteCreator {
	private Sequence sequence;
	private Mode mode;
	public NoteCreator(Sequence sequence,Mode mode) {
		this.sequence = sequence;
		this.mode=mode;
	}

	public SortedMap<Double, Note> createNotes() {
		TempoCache cache = new TempoCache(sequence);
		SortedMap<Double,Note> notes=Collections.synchronizedSortedMap(
			new TreeMap<Double,Note>(new Comparator<Double>(){
			@Override
			public int compare(Double o1, Double o2) {
				if(o1<o2)return -1;
				else if(o1>o2)return 1;
				else return 0;
			}			
		}));
		MidiEvent[][] noteMap = new MidiEvent[16][88];
		Track[] tracks = sequence.getTracks();
		for (int co1 = 0; co1 < tracks.length; co1++) {
			//String trackName = "Track-" + (co1 + 1);
			for (int co = 0; co < tracks[co1].size(); co++) {
				MidiEvent event = tracks[co1].get(co);
				MidiMessage message = event.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage shortMsg = (ShortMessage) message;
					/*try {
						shortMsg.setMessage(shortMsg.getCommand(), co1, shortMsg.getData1(), shortMsg.getData2());
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}*/
					if ((shortMsg.getCommand() == ShortMessage.NOTE_OFF)
							|| (shortMsg.getCommand() == ShortMessage.NOTE_ON)) {						
						int channel = shortMsg.getChannel();
						int noteNo = shortMsg.getData1();
						int pianoNoteNo = noteNo - 21;
						if (noteNo >= 21 && noteNo <= 108) {
							if ((shortMsg.getCommand() == ShortMessage.NOTE_OFF)
									|| (shortMsg.getCommand() == ShortMessage.NOTE_ON && shortMsg.getData2() == 0)) {
								if (noteMap[channel][pianoNoteNo] != null) {
									//long currentTick = event.getTick();
									MidiEvent noteOn = noteMap[channel][pianoNoteNo];
									long start,end;
									if(mode==Mode.MICROSECONDS){
									start=MidiUtils.tick2microsecond(sequence, noteOn.getTick(), cache);
									end=MidiUtils.tick2microsecond(sequence, event.getTick(), cache);
									}else{
										start=noteOn.getTick();
										end=event.getTick();
									}
									Note note = new Note(start,end);
									note.setStartEvent(noteOn);
									note.setEndEvent(event);									
									double c=start;
									while(true){
									Note n=notes.get(c);
									if(n==null){
										notes.put((double)c, note);
										break;
									}
									else{
										c=c+0.001;
									 }
									}
								}
							} else if (shortMsg.getCommand() == ShortMessage.NOTE_ON)
								noteMap[channel][pianoNoteNo] = event;
						}
					}
				} /*else if (message instanceof MetaMessage) {
					MetaMessage metaMsg = (MetaMessage) message;
					byte[] bytes = metaMsg.getData();
					if (bytes.length != 0)
						if (bytes[0] == 0x3) {
							char[] name = new char[bytes.length];
							for (int nameCo = 0; nameCo < name.length; nameCo++)
								name[nameCo] = (char) bytes[nameCo];
							trackName = name.toString();
						}
				}*/
			}
		}
		return notes;
	}
	
	public void findMeasures(){
		
	}
}
