package com.musekeys.waterfall;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

public class Note {
    private long start, end;
    /*private int noteNo;
    private byte channel;*/
    private MidiEvent startEvent, endEvent;

    public Note(/*int noteNo,byte channel,*/long start, long end) {
        /*this.channel=channel;
		this.noteNo=noteNo;*/
        this.start = start;
        this.end = end;
    }

    public int getChannel() {
        return ((ShortMessage) startEvent.getMessage()).getChannel();
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getNoteNo() {
        return ((ShortMessage) startEvent.getMessage()).getData1() - 21;
    }

    public MidiEvent getStartEvent() {
        return startEvent;
    }

    public void setStartEvent(MidiEvent startEvent) {
        this.startEvent = startEvent;
    }

    public MidiEvent getEndEvent() {
        return endEvent;
    }

    public void setEndEvent(MidiEvent endEvent) {
        this.endEvent = endEvent;
    }

    public ShortMessage getStartMessage() {
        return ((ShortMessage) startEvent.getMessage());
    }

    public ShortMessage getEndMessage() {
        return ((ShortMessage) endEvent.getMessage());
    }
}
