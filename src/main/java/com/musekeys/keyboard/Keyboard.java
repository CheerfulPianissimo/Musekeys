package com.musekeys.keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import com.musekeys.midiplayer.MidiVisualizer;
import com.musekeys.waterfall.WaterFallSettings;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Keyboard extends BorderPane implements MidiDevice, MidiVisualizer {
	private List<WhiteKey> whiteKeys;
	private List<BlackKey> blackKeys;
	private List<Key> keys; 
	private Receiver receiver;
	private Transmitter transmitter;
	private Receiver toDevice;
	private List<Receiver> toDevices;
	private boolean isOpen;
	private SimpleBooleanProperty midiThru = new SimpleBooleanProperty(true),
			useEffects = new SimpleBooleanProperty(false);
	private Pane keyboardPane;
	private SimpleIntegerProperty octaveNo = new SimpleIntegerProperty(5), visibleWhiteKeys, transpose, outChannel;
	private double whiteKeyWidth = 26.5;
	private double blackKeyWidth = whiteKeyWidth / 2;
	private double whiteWidthToHeight = 6;
	private double blackWidthToHeight =  whiteWidthToHeight*1.3;
	private Color[] colors = { Color.ORANGE, Color.LIGHTBLUE, Color.RED, Color.BROWN, Color.GREEN, Color.YELLOW,
			Color.VIOLET, Color.KHAKI, Color.PURPLE, Color.LIGHTGRAY, Color.PLUM, Color.TURQUOISE, Color.SPRINGGREEN,
			Color.INDIGO, Color.TAN, Color.BURLYWOOD, Color.TOMATO };
	private Paint[] paints = new Paint[16];

	public Keyboard() throws MidiUnavailableException, InvalidMidiDataException {
		super();
		whiteKeys = new ArrayList<WhiteKey>(52);
		blackKeys = new ArrayList<BlackKey>(36);
		visibleWhiteKeys = new SimpleIntegerProperty(52);
		transpose = new SimpleIntegerProperty(0);
		outChannel = new SimpleIntegerProperty(0);
		toDevices=new ArrayList<Receiver>();
		transpose.addListener((ObservableValue<? extends Number> obv, Number oldValue, Number newValue) -> {
			for (Key key : whiteKeys)
				key.setTranspose(newValue.byteValue());
			for (Key key : blackKeys)
				key.setTranspose(newValue.byteValue());
		});
		setupKeys();
		addKeys();
		setupKeypress();
		updateGradients();
		receiver = new Receiver() {
			@Override
			public void send(MidiMessage message, long timeStamp) {
				try {
					if (getMidiThru())
						transmitter.getReceiver().send(message, timeStamp);
					if (message instanceof ShortMessage) {
						ShortMessage shortMsg = (ShortMessage) message;
						if (shortMsg.getCommand() == ShortMessage.NOTE_ON
								|| shortMsg.getCommand() == ShortMessage.NOTE_OFF) {
							sendSimple(shortMsg);
						}
					}
				} catch (Exception e) {
				}
			}

			@Override
			public void close() {
				transmitter.getReceiver().close();
			}
		};
		transmitter = new Transmitter() {
			@Override
			public void setReceiver(Receiver receiver) {
				toDevices.add(receiver);
			}
			@Override
			public Receiver getReceiver() {
				return toDevice;
			}
			@Override
			public void close() {
			}
		};
		toDevice = new Receiver() {
			@Override
			public void send(MidiMessage message, long timeStamp) {
				for(Receiver to:toDevices)
					to.send(message, timeStamp);
			}
			@Override
			public void close() {				
			}
			};
		for (Key key : whiteKeys)
			key.setReceiver(toDevice);
		for (Key key : blackKeys)
			key.setReceiver(toDevice);
		this.setOnDragDetected(e -> this.startFullDrag());
		this.widthProperty().addListener(
				(ObservableValue<? extends Number> obv, Number oldValue, Number newValue) -> {
			updateKeyPositions();
		});
		this.visibleWhiteKeysProperty()
				.addListener((ObservableValue<? extends Number> obv, Number oldValue, Number newValue) -> {					
					whiteKeyWidth = this.getWidth() / getVisibleWhiteKeys();
					blackKeyWidth = whiteKeyWidth / 2;										
					if(newValue.intValue()!=52&&this.getHeight()!=0){
					whiteWidthToHeight=this.getHeight()/whiteKeyWidth;
					blackWidthToHeight=whiteWidthToHeight*1.3;
					}else {
						whiteWidthToHeight=6;
						blackWidthToHeight=whiteWidthToHeight*1.3;
					}
					updateKeyPositions();
					if (newValue.intValue() != whiteKeys.size()) {
						ScrollPane container = new ScrollPane();						
						container.setContent(keyboardPane);
						this.setCenter(container);
					} else
						this.setCenter(keyboardPane);
				});
		outChannel.addListener(e -> {
			for (Key key : whiteKeys)
				key.setChannel((byte) getOutChannel());
			for (Key key : blackKeys)
				key.setChannel((byte) getOutChannel());
		});
		useEffects.addListener(e -> {
			updateGradients();
			if (getUseEffects() == false) {
				for (WhiteKey key : whiteKeys)
					key.setEffect(null);
				for (BlackKey key : blackKeys) {
					key.setEffect(null);
					key.setUseEffects(false);
				}
			} else {
				Light.Distant light = new Light.Distant();
				light.setAzimuth(0);
				light.setElevation(80.0);
				Lighting lighting = new Lighting();
				lighting.setLight(light);
				lighting.setSurfaceScale(2.0);
				for (WhiteKey key : whiteKeys)
					key.setEffect(lighting);
				for (BlackKey key : blackKeys) {
					key.setEffect(lighting);
					key.setUseEffects(true);
				}
			}
		});			
		setUseEffects(true);
	}

	private void updateGradients() {
		for (int co = 0; co < paints.length; co++) {
			Color color = colors[co];
			if(getUseEffects()){
				LinearGradient gradient=new LinearGradient(0, 0, 0, 1, true,
						CycleMethod.NO_CYCLE, new Stop(0, color),
						new Stop(0.91,color.deriveColor(0,1,6,1)),new Stop(0.94,color.deriveColor(0,1,16,1))
						,new Stop(0.95,color.deriveColor(0, 1, 0.5, 1)),new Stop(0.96,color)
						,new Stop(0.97,color.deriveColor(0,1,9.5, 1)),new Stop(1.0,color));
			paints[co]=gradient;
			}else paints[co]=color;
		}
	}

	private void sendSimple(ShortMessage shortMsg) {
		if (shortMsg.getData1() >= 21 && shortMsg.getData1() <= 108) {
			int msgNo = shortMsg.getData1();
			Key key = keys.get(msgNo - 21);
			if (shortMsg.getCommand() == ShortMessage.NOTE_ON) {
				Platform.runLater(() -> {
					Paint channelColor = (key.getType()==1)?paints[shortMsg.getChannel()]:colors[shortMsg.getChannel()];
					key.pressKey(channelColor);
				});
				if (shortMsg.getData2() == 0)
					Platform.runLater(() -> key.releaseKey());
			} else
				Platform.runLater(() -> key.releaseKey());
		}
	}

	private void setupKeypress() {
		this.setOnMousePressed(me -> {		
			requestFocus();
			me.consume();
			if(me.getButton()==javafx.scene.input.MouseButton.SECONDARY){
				Stage stage=new Stage(StageStyle.UTILITY);
				stage.setAlwaysOnTop(true);
				stage.setResizable(false);
				KeyboardSettings setup=new KeyboardSettings(this);
				stage.setScene(new Scene(setup));
				stage.show();				
			}
		});
		BooleanWrapper[] isPressed = new BooleanWrapper[127];
		for (int co = 0; co < isPressed.length; co++)
			isPressed[co] = new BooleanWrapper();
		this.setOnKeyPressed(e -> {
			int noteNo = getNoteNumber(e.getText().toUpperCase(), getOctaveNo());
			if (noteNo != -1) {
				if (isPressed[noteNo].getValue() == false) {
					if (noteNo < 109)
						pressKey(noteNo);
					isPressed[noteNo].setValue(true);
				}
			} else if (e.getText().equals(" "))
				try {
					this.toDevice.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 64, 127), -1);
				} catch (Exception e1) {
				}
		});
		this.setOnKeyReleased(e -> {
			int noteNo = getNoteNumber(e.getText().toUpperCase(), getOctaveNo());
			if (noteNo != -1) {
				if (noteNo < 109)
					releaseKey(noteNo);
				isPressed[noteNo].setValue(false);
			} else if (e.getText().equals(" "))
				try {
					this.toDevice.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 64, 0), -1);
				} catch (Exception e1) {
				}
		});
		this.setOnKeyTyped(e -> {
			if (e.getCharacter().equals("+")) {
				if (getOctaveNo() < 9)
					setOctaveNo(getOctaveNo() + 1);
			} else if (e.getCharacter().equals("-")) {
				if (getOctaveNo() > 2)
					setOctaveNo(getOctaveNo() - 1);
			}
		});

	}

	private class BooleanWrapper {
		private boolean value = false;

		public boolean getValue() {
			return value;
		}

		public void setValue(boolean isPressed) {
			this.value = isPressed;
		}
	}

	private void pressKey(int msgNo) {
		Key key = keys.get(msgNo - 21);
		key.pressKey();
		key.playNote();
	}

	private void releaseKey(int msgNo) {
		Key key = keys.get(msgNo - 21);
		key.releaseKey();
		key.stopNote();
	}

	private int getNoteNumber(String character, int octaveNo) {
		String[] letters = { "A", "W", "S", "E", "D", "F", "T", "G", "Y", "H", "U", "J", "K", "O", "L", "P", ";", "'",
				"]" };
		int addNo = 12 * octaveNo;
		for (int co = 0; co < letters.length; co++)
			if (letters[co].equals(character)) {
				return addNo + co;
			}
		return -1;
	}

	private void setupKeys() throws MidiUnavailableException, InvalidMidiDataException {
		whiteKeys.add(new WhiteKey(21, receiver)); // Lowest note-A0
		blackKeys.add(new BlackKey(22, receiver));
		whiteKeys.add(new WhiteKey(23, receiver));
		for (int noteNo = 24; noteNo < 108; noteNo = noteNo + 12) { // Adding the 7 octaves
			whiteKeys.add(new WhiteKey(noteNo, receiver));
			blackKeys.add(new BlackKey(noteNo + 1, receiver));
			whiteKeys.add(new WhiteKey(noteNo + 2, receiver));
			blackKeys.add(new BlackKey(noteNo + 3, receiver));
			whiteKeys.add(new WhiteKey(noteNo + 4, receiver));
			whiteKeys.add(new WhiteKey(noteNo + 5, receiver));
			blackKeys.add(new BlackKey(noteNo + 6, receiver));
			whiteKeys.add(new WhiteKey(noteNo + 7, receiver));
			blackKeys.add(new BlackKey(noteNo + 8, receiver));
			whiteKeys.add(new WhiteKey(noteNo + 9, receiver));
			blackKeys.add(new BlackKey(noteNo + 10, receiver));
			whiteKeys.add(new WhiteKey(noteNo + 11, receiver));
		}
		whiteKeys.add(new WhiteKey(108, receiver));
		keys = new ArrayList<Key>();
		keys.addAll(whiteKeys);
		keys.addAll(blackKeys);
		Collections.sort(keys, new Comparator<Key>() {
			@Override
			public int compare(Key key1, Key key2) {
				if (key1.getNoteNo() < key2.getNoteNo())
					return -1;
				else if (key1.getNoteNo() > key2.getNoteNo())
					return 1;
				else
					return 0;
			}
		});
	}

	private void addKeys() {
		keyboardPane = new Pane();
		keyboardPane.getChildren().addAll(whiteKeys);
		keyboardPane.getChildren().addAll(blackKeys);
		updateKeyPositions();
		this.setCenter(keyboardPane);
	}

	private void updateKeyPositions() {
		whiteKeyWidth = this.getWidth() / getVisibleWhiteKeys();
		blackKeyWidth = whiteKeyWidth / 2;
		for (int co = 0; co < whiteKeys.size(); co++) {
			WhiteKey key = whiteKeys.get(co);
			key.setWidth(whiteKeyWidth);
			key.setHeight(whiteKeyWidth * whiteWidthToHeight);
			key.setX(calculateX(key));
		}
		for (int co = 0; co < blackKeys.size(); co++) {
			BlackKey key = blackKeys.get(co);
			key.setWidth(blackKeyWidth);
			key.setHeight(blackKeyWidth * blackWidthToHeight);
			key.setX(calculateX(key));
		}
		this.setMaxHeight(whiteKeyWidth*whiteWidthToHeight);
		this.setPrefHeight(whiteKeyWidth*whiteWidthToHeight);
	}

	private double calculateX(Key orginalKey) {
		double totalX = 0;
		int noteNo = 0;
		Key key = orginalKey;
		if (key instanceof BlackKey)
			for (int co = 0; co < whiteKeys.size(); co++) {
				WhiteKey white = whiteKeys.get(co);
				if (white.getNoteNo() == (key.getNoteNo() - 1)) {
					key = white;
					noteNo = co;
					break;
				}
			}
		else
			noteNo = whiteKeys.indexOf(key);
		totalX = (whiteKeyWidth) * noteNo;
		if (orginalKey instanceof BlackKey)
			totalX = totalX + (whiteKeyWidth + blackKeyWidth) / 2;
		return totalX;
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {
		this.receiver.send(message, timeStamp);
	}

	public void setChannelColor(int channel, Color paint) {
		colors[channel] = paint;
		updateGradients();
	}

	public Color getChannelColor(int channel) {
		return colors[channel];
	}

	@Override
	public Info getDeviceInfo() {
		return new KeyboardInfo();
	}

	@Override
	public void open() throws MidiUnavailableException {
		isOpen = true;
	}

	@Override
	public void close() {
		isOpen = false;
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public long getMicrosecondPosition() {
		return -1;
	}

	@Override
	public int getMaxReceivers() {
		return 1;
	}

	@Override
	public int getMaxTransmitters() {
		return 1;
	}

	@Override
	public Receiver getReceiver() throws MidiUnavailableException {
		return receiver;
	}

	@Override
	public List<Receiver> getReceivers() {
		List<Receiver> list = new ArrayList<Receiver>();
		list.add(receiver);
		return list;
	}

	@Override
	public Transmitter getTransmitter() throws MidiUnavailableException {
		return transmitter;
	}

	@Override
	public List<Transmitter> getTransmitters() {
		List<Transmitter> list = new ArrayList<Transmitter>();
		list.add(transmitter);
		return list;
	}

	public boolean getMidiThru() {
		return midiThruProperty().get();
	}

	public void setMidiThru(boolean midiThru) {
		this.midiThruProperty().set(midiThru);
		;
	}

	public BooleanProperty midiThruProperty() {
		return midiThru;
	}

	private static class KeyboardInfo extends MidiDevice.Info {

		private static final String name = "MuseKeys Virtual Keyboard";
		private static final String vendor = "The MuseKeys project";
		private static final String description = "Virtual Keyboard";
		private static final String version = "Version 1.0";

		private KeyboardInfo() {
			super(name, vendor, description, version);
		}
	}

	public SimpleIntegerProperty octaveNoProperty() {
		return this.octaveNo;
	}

	public int getOctaveNo() {
		return this.octaveNoProperty().get();
	}

	public void setOctaveNo(final int octaveNo) {
		this.octaveNoProperty().set(octaveNo);
	}

	public SimpleIntegerProperty visibleWhiteKeysProperty() {
		return this.visibleWhiteKeys;
	}

	public int getVisibleWhiteKeys() {
		return this.visibleWhiteKeysProperty().get();
	}

	public void setVisibleWhiteKeys(final int visibleWhiteKeys) {
		this.visibleWhiteKeysProperty().set(visibleWhiteKeys);
	}

	public SimpleIntegerProperty transposeProperty() {
		return this.transpose;
	}

	public int getTranspose() {
		return this.transposeProperty().get();
	}

	public void setTranspose(final int transpose) {
		this.transposeProperty().set(transpose);
	}

	public SimpleIntegerProperty outChannelProperty() {
		return this.outChannel;
	}

	public int getOutChannel() {
		return this.outChannelProperty().get();
	}

	public void setOutChannel(final int outChannel) {
		this.outChannelProperty().set(outChannel);
	}

	public SimpleBooleanProperty useEffectsProperty() {
		return this.useEffects;
	}

	public boolean getUseEffects() {
		return this.useEffectsProperty().get();
	}

	public void setUseEffects(final boolean useEffects) {
		this.useEffectsProperty().set(useEffects);
	}
}
