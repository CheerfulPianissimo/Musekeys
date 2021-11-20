package com.musekeys.midiplayer;

import javax.sound.midi.Receiver;

import javafx.scene.paint.Color;

public interface MidiVisualizer extends Receiver{	
	Color getChannelColor(int channelNo);	
	void setChannelColor(int channelNo,Color paint);
}
