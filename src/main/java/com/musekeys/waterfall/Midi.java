package com.musekeys.waterfall;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.musekeys.midi.GMInstruments;
import com.musekeys.midi.MidiUtils;

public class Midi {

	private static Sequence sequence;
		
	public static void main(String[] args) throws Exception {				
		List<List<MidiEvent>> noteMap = new ArrayList<List<MidiEvent>>(16);
		for (int co = 0; co < 16; co++) {
			noteMap.add(new ArrayList<MidiEvent>(127));
			for(int co1=0;co1<127;co1++)
				noteMap.get(co).add(null);
		}
		//sequence = MidiSystem.getSequence(new File("G:/MIDI/JKeyboardExp/pitch_bend.mid"));
		sequence = MidiSystem.getSequence(new File("G:/MIDI/Harry_Potter_And_The_Chamber_Of_Secrets__Hedwig's_Theme.mid"));
		Track[] tracks = sequence.getTracks();
		for (int co1 = 0; co1 < tracks.length; co1++) {
			System.out.println("New Track: "+co1);
			for (int co = 0; co < sequence.getTracks()[co1].size(); co++) {
				MidiEvent event = tracks[co1].get(co);
				MidiMessage message = event.getMessage();
				if (message instanceof ShortMessage) {
					ShortMessage shortMsg = (ShortMessage) message;
					int channel = shortMsg.getChannel();
					int noteNo = shortMsg.getData1();
					if (shortMsg.getCommand() == ShortMessage.NOTE_OFF
							|| (shortMsg.getCommand() == ShortMessage.NOTE_ON && shortMsg.getData2() == 0)) {						
						if (noteMap.get(channel).get(noteNo) != null) {
							MidiEvent onMsg = noteMap.get(channel).get(noteNo);
							long currentTick = event.getTick();
							long noteOnTick = onMsg.getTick();
							long difference = currentTick - noteOnTick;
							long differenceMicrosecs=
									MidiUtils.tick2microsecond(sequence, difference, null);							
							noteMap.get(channel).add(noteNo, null);
							//doSomethingWithDifference(difference);
							System.out.println("Note " + noteNo + " on " + channel + " has been released at tick "
									+ tracks[co1].get(co).getTick() + " with size " + difference+
									" and in MicroSeconds "+differenceMicrosecs);
						}
					} else if (shortMsg.getCommand() == ShortMessage.NOTE_ON) {						
						noteMap.get(channel).add(noteNo, event);
						long noteOnTickTime=MidiUtils.tick2microsecond(sequence, event.getTick(), null);
						System.out.println("Note " + noteNo + " on channel " + channel + " has been pressed at tick "
								+ tracks[co1].get(co).getTick()+" at time "+noteOnTickTime);
					} else if(shortMsg.getCommand()==ShortMessage.PROGRAM_CHANGE){
						System.out.println("Program change: Instrument:"+
								GMInstruments.Instruments[shortMsg.getData1()]+
								" :Data 1: "+shortMsg.getData1()+" Data 2:"+shortMsg.getData2());
					} else if(shortMsg.getCommand()==ShortMessage.PITCH_BEND){
						System.out.println("Pitch bend "
								+ "Data 1: "+shortMsg.getData1()+" Data 2:"+shortMsg.getData2());
						}
				else if(message instanceof MetaMessage){
					MetaMessage metaMsg=(MetaMessage)message;
					byte[] bytes=metaMsg.getData();
					System.out.print("Meta message: Data "+" Type: "+metaMsg.getType()+" Data: ");
					for(byte data:bytes)
						System.out.print((char)data);
					System.out.println();
				}
			}
		}
	}	
}
}
