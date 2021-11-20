package com.musekeys.midiplayer;

import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import com.musekeys.control.EditableProgressBar;
import com.musekeys.control.JumpBackBar;
import com.musekeys.control.NumberTextField;
import com.musekeys.midi.GMInstruments;
import com.musekeys.waterfall.WaterFall;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ChannelViewer extends BorderPane {
	private ArrayList<MidiChannelModel> channels;
	private Receiver toDevice;
	private ComboBox<String> controller1, controller2;
	private MidiVisualizer visualizer,visualizer2;
	private GridPane grid;
	private int padding=20;
	private StringBuffer instrumentPressed = new StringBuffer("0");
	private static ColumnConstraints labelConst,colorConst,checkConst,instrumentConst,controlConst,controlValue;
	private static RowConstraints rowConst;
	
	public ChannelViewer() {
		channels = new ArrayList<MidiChannelModel>();
		for (int co = 0; co < 16; co++)
			channels.add(new MidiChannelModel(co + 1));
		setupViewer();
	}

	private void setupViewer() {
		grid = new GridPane();
		grid.setMinSize(900, 400);		
		labelConst = new ColumnConstraints(0, 50, Double.MAX_VALUE);
		labelConst.setHalignment(HPos.CENTER);
		colorConst = new ColumnConstraints(0, 40, Double.MAX_VALUE);
		checkConst = new ColumnConstraints(0, 25, Double.MAX_VALUE);
		instrumentConst = new ColumnConstraints(0, 150, Double.MAX_VALUE);
		instrumentConst.setHalignment(HPos.CENTER);
		controlConst = new ColumnConstraints(0, 200, Double.MAX_VALUE);
		controlConst.setHalignment(HPos.CENTER);
		controlValue = new ColumnConstraints(0, 50, Double.MAX_VALUE);
		controlValue.setHalignment(HPos.CENTER);
		rowConst = new RowConstraints(0, 75, Double.MAX_VALUE);
		grid.getColumnConstraints().addAll(labelConst, checkConst, colorConst, instrumentConst,
				controlConst,controlValue, controlConst,controlValue, controlConst, controlValue);
		grid.getRowConstraints().add(rowConst);
		Label channel = new Label("Channel");
		grid.add(channel, 0, 0);		
		Label colorLabel = new Label("Color");
		grid.add(colorLabel, 2, 0);
		Label instrumentLabel = new Label("Instrument");
		grid.add(instrumentLabel, 3, 0);
		Label pitchLabel = new Label("Pitch Bend");		
		ContextMenu menu=new ContextMenu();
		MenuItem toDefault=new MenuItem("Set all channels to default value (8256)");
		menu.getItems().add(toDefault);
		toDefault.setOnAction(event->{			
			for(MidiChannelModel model:channels)
				model.setPitchBend(8256);
		});
		pitchLabel.setContextMenu(menu);		
		grid.add(pitchLabel, 4, 0);
		controller1 = new ComboBox<String>();
		controller2 = new ComboBox<String>();		
		controller1.setPrefWidth(controlConst.getPrefWidth()-padding);
		controller2.setPrefWidth(controlConst.getPrefWidth()-padding);
		ArrayList<String> controllerList = new ArrayList<String>();
		for (String controller : GMInstruments.controllers)
			if (!controller.equals("Unknown controller"))
				controllerList.add(controller);
		controller1.getItems().addAll(controllerList);
		controller2.getItems().addAll(controllerList);
		controller1.valueProperty().addListener((ObservableValue<? extends String> obv, String oldValue,
				String newValue) -> changeController(newValue, 0));
		controller2.valueProperty().addListener((ObservableValue<? extends String> obv, String oldValue,
				String newValue) -> changeController(newValue, 1));
		grid.add(controller1, 6, 0);
		grid.add(controller2, 8, 0);
		for (int co = 1; co <= 16; co++) {
			setupChannel(co);
		}
		setupAllChannel();
		controller1.setValue("Volume");		
		controller2.setValue("Pan position");
		this.setCenter(grid);
	}

	private void setupAllChannel() {
		int row=17;
		Label channelNo=new Label("All");
		grid.add(channelNo, 0, row);
		CheckBox on = new CheckBox();
		on.setSelected(true);
		on.setOnAction(e->{
			if(!on.isSelected())
				for(MidiChannelModel model:channels){
					model.setMuted(false);
				}
			else
				for(MidiChannelModel model:channels){
					model.setMuted(true);
				}			
		});
		grid.add(on, 1, row);		
		Hyperlink instrument = new Hyperlink("<All Channels>");		
		instrument.setOnAction(e -> {
			instrument.setVisited(false);
			if (instrumentPressed.toString().equals("1"))
				return;
			Stage pop = new Stage(StageStyle.UTILITY);
			pop.setAlwaysOnTop(true);
			pop.setTitle("Select A Program");
			InstrumentChooser chooser = new InstrumentChooser(-1);
			pop.setScene(new Scene(chooser));
			pop.show();
			pop.showingProperty().addListener(event -> {
				pop.hide();
				instrumentPressed.setCharAt(0, '0');
			});
			chooser.instrumentProperty().addListener(
					(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
						for(int co=0;co<channels.size();co++)
							if(co!=9)
								channels.get(co).setInstrument(newValue.intValue());						
					});
			chooser.okPressedProperty().addListener(
					(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
						pop.hide();
					});
			instrumentPressed.setCharAt(0, '1');
		});
		grid.add(instrument, 3, row);
		EditableProgressBar pitch = new JumpBackBar(16383,8256);
		pitch.valueProperty().addListener(
				(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
					for(MidiChannelModel model:channels){
						/*int newVal=0;
						if(oldValue.intValue()<newValue.intValue()){
							newVal=(int)((newValue.doubleValue()-oldValue.doubleValue())/100*16383);
							newVal=model.getPitchBend()+newVal;
						}
						else {
							newVal=(int)((oldValue.doubleValue()-newValue.doubleValue())
									/100*16383);
							newVal=model.pitchBendProperty().get()-newVal;
						}										
						if(newVal<=16383&&newVal>=0)
							model.setPitchBend(newVal);*/
						model.setPitchBend(newValue.intValue());
					}
				});
		NumberTextField pitchValue=new NumberTextField(0,16383);
		pitchValue.setValue(8256);
		pitchValue.valueProperty().bindBidirectional(pitch.valueProperty());
		pitch.setPrefWidth(controlConst.getPrefWidth()-padding);
		pitch.setOnContextMenuRequested(e->{
			ContextMenu menu=new ContextMenu();
			MenuItem toDefault=new MenuItem("Set all channels to default value (8129)");
			menu.getItems().add(toDefault);
			toDefault.setOnAction(event->{
				pitch.setValue(50);
				for(MidiChannelModel model:channels)
					model.setPitchBend(8129);
			});
			menu.show(pitch, e.getSceneX(), e.getSceneY());
		});
		grid.add(pitch, 4, row);
		grid.add(pitchValue, 5, row);
		for (int i = 0, gridValue = 6; i < 2; i++) {
			int i2=i;
			EditableProgressBar bar = new EditableProgressBar(127);
			bar.setValue(127);
			bar.setPrefWidth(controlConst.getPrefWidth()-padding);
			bar.valueProperty().addListener(
					(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
						for(MidiChannelModel model:channels){
							/*int newVal=0;
							if(oldValue.intValue()<newValue.intValue()){
								newVal=(int)((newValue.doubleValue()-oldValue.doubleValue())/100*127);
								newVal=model.getControllerForColumn(i2).get()+newVal;
							}
							else {
								newVal=(int)((oldValue.doubleValue()-newValue.doubleValue())
										/100*127);
								newVal=model.getControllerForColumn(i2).get()-newVal;
							}
							if(newVal<=127&&newVal>=0)
								model.getControllerForColumn(i2).set(newVal);*/
							model.getControllerForColumn(i2).set(newValue.intValue());
						}
					});
			NumberTextField controllerValue = new NumberTextField(0,127);
			controllerValue.valueProperty().bindBidirectional(bar.valueProperty());
			grid.add(bar, gridValue, row);
			grid.add(controllerValue, ++gridValue, row);
			gridValue++;
		}
	}

	private void setupChannel(int co) {
		final int co2 = co;
		final MidiChannelModel model = channels.get(co - 1);
		grid.getRowConstraints().add(rowConst);
		Label channelNo = new Label(model.getChannelNo().toString());
		grid.add(channelNo, 0, co);
		ContextMenu channelMenu=new ContextMenu();
		CheckMenuItem solo=new CheckMenuItem("Solo");
		solo.selectedProperty().bindBidirectional(model.soloProperty());
		model.soloProperty().addListener(
				(ObservableValue<? extends Boolean> obv,Boolean oldValue,Boolean newValue)->{
					if(newValue.booleanValue()){
						for(MidiChannelModel channel:channels)
							channel.setMuted(false);
						model.setMuted(true);
					}else
						for(MidiChannelModel channel:channels)
							channel.setMuted(true);
				});
		channelMenu.getItems().add(solo);
		channelNo.setContextMenu(channelMenu);
		CheckBox on = new CheckBox();
		on.selectedProperty().bindBidirectional(model.mutedProperty());
		model.mutedProperty().addListener(
				(ObservableValue<? extends Boolean> obv,Boolean oldValue,Boolean newValue)->{
					if (visualizer2 != null && visualizer2 instanceof WaterFall){
						WaterFall waterfall=(WaterFall)visualizer2;
						waterfall.setShouldRender(co-1,model.mutedProperty().get());
					}
				});
		grid.add(on, 1, co);
		ColorPicker colorSelect = new ColorPicker();
		colorSelect.valueProperty().bindBidirectional(model.colorProperty());
		model.colorProperty().addListener(e -> {
			if (visualizer != null)
				visualizer.setChannelColor(co2 - 1, model.colorProperty().get());
			if (visualizer2 != null)
				visualizer2.setChannelColor(co2 - 1, model.colorProperty().get());
		});
		grid.add(colorSelect, 2, co);	
		if(co==10){
			Hyperlink instrument = new Hyperlink("Drum Kit");
			instrument.setOnAction(e -> 
				instrument.setVisited(false));
			grid.add(instrument, 3, co);
		}
		else{
		Hyperlink instrument = new Hyperlink("<No Instrument>");
		model.instrumentProperty()
				.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
					if (newValue.intValue() == 128) {
						instrument.setText("Drum Kit");
						return;
					}
					ShortMessage msg = new ShortMessage();
					try {
						msg.setMessage(ShortMessage.PROGRAM_CHANGE, co2 - 1, newValue.intValue(), 0);
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
					if (toDevice != null)
						toDevice.send(msg, -1);
					instrument.setText(GMInstruments.Instruments[newValue.intValue()]);
				});
		instrument.setOnAction(e -> {
			instrument.setVisited(false);
			if (instrumentPressed.toString().equals("1"))
				return;
			Stage pop = new Stage(StageStyle.UTILITY);
			pop.setAlwaysOnTop(true);
			pop.setTitle("Select A Program");			
			InstrumentChooser chooser = new InstrumentChooser(model.instrumentProperty().get());
			/*pop.setDetachedTitle("Select Instrument");
			pop.setContentNode(chooser);
			pop.show(instrument);*/
			pop.setScene(new Scene(chooser));
			pop.show();
			pop.showingProperty().addListener(event -> {
				pop.hide();
				instrumentPressed.setCharAt(0, '0');
			});
			chooser.instrumentProperty().addListener(
					(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
						model.setInstrument(newValue.intValue());
					});
			chooser.okPressedProperty().addListener(
					(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
						pop.hide();
					});
			instrumentPressed.setCharAt(0, '1');
		});
		grid.add(instrument, 3, co);
		}
		EditableProgressBar pitch = new JumpBackBar(16383,8256);
		pitch.setPrefWidth(controlConst.getPrefWidth()-padding);
		pitch.valueProperty().bindBidirectional(model.pitchBendProperty());
		model.pitchBendProperty().addListener(
				(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
					ShortMessage msg = new ShortMessage();
					try {
						msg.setMessage(ShortMessage.PITCH_BEND, co2 - 1,
								newValue.intValue()%128,newValue.intValue()/128);							
						} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
					if (toDevice != null)
						toDevice.send(msg, -1);
				});
		NumberTextField pitchValue=new NumberTextField(0,16383);
		pitchValue.valueProperty().bindBidirectional(model.pitchBendProperty());
		grid.add(pitch, 4, co);
		grid.add(pitchValue, 5, co);
		for (int i = 0, gridValue = 6; i < 2; i++) {
			EditableProgressBar bar = new EditableProgressBar(127);
			final int i2 = i;
			bar.valueProperty().bindBidirectional(model.getControllerForColumn(i));
			bar.setPrefWidth(controlConst.getPrefWidth()-padding);
			model.getControllerForColumn(i).addListener(
					(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
						ShortMessage msg = new ShortMessage();
						try {
							msg.setMessage(ShortMessage.CONTROL_CHANGE, co2 - 1,
									model.getControllerValueForColumn(i2).get(), newValue.intValue());
						} catch (InvalidMidiDataException e) {
							e.printStackTrace();
						}
						if (toDevice != null)
							toDevice.send(msg, -1);
					});
			NumberTextField controllerValue = new NumberTextField(0,127);
			controllerValue.valueProperty().bindBidirectional(model.getControllerForColumn(i));
			grid.add(bar, gridValue, co);
			grid.add(controllerValue, ++gridValue, co);
			gridValue++;
		}		
	}

	private void changeController(String controller, int column) {
		int midControlNo = 0;
		for (int co = 0; co < GMInstruments.controllers.length; co++)
			if (GMInstruments.controllers[co].equals(controller)){
				midControlNo = co;
				break;
			}
		for (MidiChannelModel model : channels)
			model.setControllerForColumn(midControlNo, column);
	}

	public void refresh(){
		for (MidiChannelModel model : channels)
			model.refresh();
	}
	
	public ArrayList<MidiChannelModel> getChannels() {
		return channels;
	}

	public Receiver getReceiver() {
		return receiver;
	}

	private Receiver receiver = new Receiver() {
		@Override
		public void send(MidiMessage message, long timeStamp) {
			if (message instanceof ShortMessage) {
				ShortMessage shortMsg = (ShortMessage) message;
				int channelNo = shortMsg.getChannel();
				Platform.runLater(new Runnable() {
					public void run() {
						if (shortMsg.getCommand() == ShortMessage.PROGRAM_CHANGE) {
							if (channelNo == 9)
								channels.get(channelNo).setInstrument(128);
							else
								channels.get(channelNo).setInstrument(shortMsg.getData1());
						} else if (shortMsg.getCommand() == ShortMessage.CONTROL_CHANGE) {
							channels.get(channelNo).setController(shortMsg.getData1(), shortMsg.getData2());
						}else if(shortMsg.getCommand()==ShortMessage.PITCH_BEND){
							channels.get(channelNo).setPitchBend((shortMsg.getData2()*128)+shortMsg.getData2());
						}
					}
				});
			}
		}

		@Override
		public void close() {

		}
	};

	private Transmitter transmitter = new Transmitter() {
		@Override
		public void setReceiver(Receiver receiver) {
			toDevice = receiver;
		}

		@Override
		public Receiver getReceiver() {
			return toDevice;
		}

		@Override
		public void close() {
		}
	};

	public Transmitter getTransmitter() {
		return transmitter;
	}

	public MidiVisualizer getMidiVisualizer() {
		return visualizer;
	}

	public void setMidiVisualizer(MidiVisualizer visualizer) {
		this.visualizer = visualizer;
		for (int co = 0; co < 16; co++) {
			channels.get(co).setColor(visualizer.getChannelColor(co));
		}
	}
	
	public MidiVisualizer getMidiVisualizer2() {
		return visualizer2;
	}

	public void setMidiVisualizer2(MidiVisualizer visualizer) {
		this.visualizer2 = visualizer;
		for (int co = 0; co < 16; co++) {
			channels.get(co).setColor(visualizer.getChannelColor(co));
		}
	}
}