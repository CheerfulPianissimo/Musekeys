package com.musekeys.midiplayer;

import java.io.IOException;
import java.net.URL;

import javax.sound.midi.Sequencer;

import com.musekeys.control.NumberTextField;
import com.musekeys.midi.MidiUtils.TempoCache;
import com.musekeys.playlist.PlaylistViewer;
import com.musekeys.midi.MidiUtils;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

public class MidiPlayerControl extends BorderPane {
	private Sequencer sequencer;
	private long forwardOrRewindFactor = 10000000,length;
	private int tempoUnit = 0; // 0-percent,1-BPM
	private int sliderAddAtEnd = 20;
	private SVGPath playPath, pausePath;
	private boolean shouldUpdateSlider = true;
	@FXML private ToggleButton playbackButton, loopA, loopB;
	@FXML private Slider timeSlider, tempoSlider;
	@FXML private Label currentTime, duration, bpmLabel;
	@FXML private TextField tempoTextfield, bpmTextfield,loopAMin, loopBMin, loopASec, loopBSec;
	@FXML private ChoiceBox<String> tempoChoice;
	@FXML private ToolBar toolBar;
	@FXML private Pane sliderPane;
	private Rectangle loopBPointer,loopAPointer;
	private PlaylistViewer playlistViewer;

	public PlaylistViewer getPlaylistViewer() {
		return playlistViewer;
	}

	public void setPlaylistViewer(PlaylistViewer viewer) {
		this.playlistViewer = viewer;
	}

	public MidiPlayerControl(Sequencer sequencer) {
		super();
		/*ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL ur=this.getClass().getResource("/main/java/com/musekeys/midiplayer/PlayerControl.fxml");
		System.out.println(ur.toString());*/
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PlayerControl.fxml"));
		fxmlLoader.setController(this);
		this.sequencer = sequencer;
		try {
			this.setCenter(fxmlLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		playPath = new SVGPath();
		playPath.setContent("M3 2l10 6-10 6z");
		pausePath = new SVGPath();
		pausePath.setContent("M2 2h5v12h-5zM9 2h5v12h-5z");
		playbackButton.setGraphic(playPath);
		playbackButton.setTooltip(new Tooltip("Play"));
		setupTempoData();
		setupLoop();
		setupDataUpdater();
	}

	private Rectangle createPointer() {
		Rectangle rect=new Rectangle();
		rect.heightProperty().bind(sliderPane.heightProperty());
		rect.setWidth(5);
		rect.setOnMouseDragged(e->{
			if (e.getX() > timeSlider.getWidth() || e.getX() < 0)
				return;			
			rect.setX(e.getX());
			e.consume();
		});		
		return rect;
	}

	private void setupLoop() {
		loopAPointer=createPointer();
		loopBPointer=createPointer();
		sliderPane.setOnMouseClicked(e->{
			if(e.getX()!=loopAPointer.getX()&&e.getX()!=loopBPointer.getX())
			timeSlider.fireEvent(e);
		});
		loopAPointer.xProperty().addListener(
				(ObservableValue<? extends Number> num, Number old, Number newValue)->{
					double pos = newValue.doubleValue() / timeSlider.getWidth();
					long microsecPos=(long) (pos * length);
					long tick=MidiUtils.microsecond2tick(sequencer.getSequence(),
							microsecPos, null);
					loopAMin.setText(String.valueOf(
							(int)(microsecPos/sequencer.getTempoFactor() / 1000000) / 60));
					loopASec.setText(String.valueOf(
							(int)(microsecPos/sequencer.getTempoFactor()/ 1000000) % 60));
					sequencer.setLoopStartPoint(tick);
				});
		loopBPointer.xProperty().addListener(
				(ObservableValue<? extends Number> num, Number old, Number newValue)->{
					double pos = newValue.doubleValue() / timeSlider.getWidth();
					long microsecPos=(long) (pos * length);
					long tick=MidiUtils.microsecond2tick(sequencer.getSequence(),
							microsecPos, null);
					loopBMin.setText(String.valueOf(
							(int)(microsecPos/sequencer.getTempoFactor() / 1000000/ 60)));
					loopBSec.setText(String.valueOf(
							(int)(microsecPos/sequencer.getTempoFactor()/ 1000000 % 60)));
					sequencer.setLoopEndPoint(tick);
				});
		loopAPointer.fillProperty().bind(loopA.textFillProperty());
		loopBPointer.fillProperty().bind(loopB.textFillProperty());
		loopA.selectedProperty()
				.addListener((ObservableValue<? extends Boolean> obv, Boolean oldVal, Boolean newVal) -> {
					if (newVal == true) {
						sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
						long tickPos = sequencer.getTickPosition();
						sequencer.setLoopStartPoint(tickPos);
						long microsecPos = sequencer.getMicrosecondPosition();
						loopAMin.setText(String.valueOf((microsecPos / 1000000) / 60));
						loopASec.setText(String.valueOf((microsecPos / 1000000) % 60));
						loopAPointer.setX(
								(microsecPos*timeSlider.getWidth())/length);
						//micro=(x/width)*length ==> micro/length=x/width
						// 				==> x=micro*width/length
						sliderPane.toFront();
						if(!sliderPane.getChildren().contains(loopAPointer))
						sliderPane.getChildren().add(loopAPointer);
					} else {
						sliderPane.toBack();
						sequencer.setLoopCount(0);
						loopAMin.setText("");
						loopASec.setText("");
						loopBSec.setText("");
						loopBMin.setText("");
						loopB.setSelected(false);
						sliderPane.getChildren().remove(loopAPointer);
					}
				});
		loopB.selectedProperty()
				.addListener((ObservableValue<? extends Boolean> obv, Boolean oldVal, Boolean newVal) -> {
					if (newVal == true) {
						long tickPos = sequencer.getTickPosition();
						sequencer.setLoopEndPoint(tickPos);
						long microsecPos = sequencer.getMicrosecondPosition();
						loopBMin.setText(String.valueOf((microsecPos / 1000000) / 60));
						loopBSec.setText(String.valueOf((microsecPos / 1000000) % 60));
						loopBPointer.setX(
								(microsecPos*timeSlider.getWidth())/length);
						if(!sliderPane.getChildren().contains(loopBPointer))
						sliderPane.getChildren().add(loopBPointer);
					} else {
						sequencer.setLoopEndPoint(-1);						
						loopBSec.setText("");
						loopBMin.setText("");
						sliderPane.getChildren().remove(loopBPointer);
					}
				});		
	}

	private void setupTempoData() {
		tempoChoice.getItems().addAll("Percent", "BPM");
		tempoChoice.getSelectionModel().select(0);
		tempoChoice.getSelectionModel().selectedIndexProperty()
				.addListener((ObservableValue<? extends Number> num, Number old, Number newValue) -> {
					tempoUnit = newValue.intValue();
					if (tempoUnit == 0) {
						toolBar.getItems().removeAll(bpmLabel, bpmTextfield, tempoTextfield);
						toolBar.getItems().add(10, tempoTextfield);
						toolBar.getItems().add(12, bpmLabel);
						toolBar.getItems().add(13, bpmTextfield);
						tempoSlider.setValue(sequencer.getTempoFactor() * 100);
						if (sequencer.getTempoFactor() * 100 >= tempoSlider.getMax()) {
							tempoSlider.setMax(newValue.doubleValue() + sliderAddAtEnd);
						} else
							tempoSlider.setMax(200);
					} else if (tempoUnit == 1) {
						toolBar.getItems().removeAll(bpmLabel, bpmTextfield, tempoTextfield);
						toolBar.getItems().add(10, bpmLabel);
						toolBar.getItems().add(11, bpmTextfield);
						toolBar.getItems().add(13, tempoTextfield);
						if (sequencer.getTempoInBPM() >= tempoSlider.getMax()) {
							tempoSlider.setMax(newValue.doubleValue() + sliderAddAtEnd);
						}
					}
				});
		tempoSlider.setMax(200);
		tempoSlider.setValue(100);
		tempoSlider.valueProperty()
				.addListener((ObservableValue<? extends Number> num, Number old, Number newValue) -> {
					if (tempoUnit == 0) {
						if (sequencer.getTempoFactor() * 100 >= tempoSlider.getMax()) {
							tempoSlider.setMax(newValue.doubleValue() + sliderAddAtEnd);
						}
						sequencer.setTempoFactor(newValue.intValue() / 100.0f);
						tempoTextfield.setText(newValue.intValue() + "%");
					} else if (tempoUnit == 1) {
						float tempoFactor = newValue.floatValue() / sequencer.getTempoInBPM();
						sequencer.setTempoFactor(tempoFactor);
						if (newValue.doubleValue() >= tempoSlider.getMax()) {
							tempoSlider.setMax(newValue.doubleValue() + sliderAddAtEnd);
						}
					}
				});
		tempoTextfield.setText((int) tempoSlider.getValue() + "%");
	}

	private void setupDataUpdater() {
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), e -> {
			length=sequencer.getMicrosecondLength();
			float currentPosition = sequencer.getMicrosecondPosition() / sequencer.getTempoFactor();
			float realLength = length/ sequencer.getTempoFactor();
			if ((sequencer.isRunning()) && (playbackButton.isSelected() == false)) {
				playbackButton.setSelected(true);
				playbackButton.setTooltip(new Tooltip("Pause"));
				playbackButton.setGraphic(pausePath);
			} else if ((sequencer.isRunning() == false || currentPosition == length)
					&& playbackButton.isSelected()) {
				playbackButton.setSelected(false);
				playbackButton.setTooltip(new Tooltip("Play"));
				playbackButton.setGraphic(playPath);
			}
			if(currentPosition == realLength&&playlistViewer!=null){
				playlistViewer.next();
				if(sequencer.getSequence()!=null)
				sequencer.start();
			}
			updateTempo();			
			if (sequencer.getSequence() != null) {
				duration.setText(microsecToString((long) (realLength)));
				if (shouldUpdateSlider)
					timeSlider.setValue((currentPosition * 100) / realLength);
				currentTime.setText(microsecToString((long) (currentPosition)));
			}
		});
		Timeline timeline = new Timeline(keyFrame);
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
		timeSlider.valueProperty().addListener(
				(ObservableValue<? extends Number> ov, Number oldval, Number newval) -> {
			if (timeSlider.isValueChanging()) {
				sequencer.setMicrosecondPosition(newval.longValue() / 100 * length);
			}
		});
		timeSlider.valueChangingProperty()
				.addListener((ObservableValue<? extends Boolean> obv, Boolean oldVal, Boolean newVal) -> {
					if (newVal) {
						shouldUpdateSlider = false;
						sequencer.stop();
					} else {
						sequencer.start();
						shouldUpdateSlider = true;
					}
				});
		timeSlider.setOnMousePressed(e -> shouldUpdateSlider = false);
		timeSlider.setOnMouseClicked(e -> {
			double pos = e.getX() / timeSlider.getWidth();
			shouldUpdateSlider = true;
			sequencer.setMicrosecondPosition((long) (pos * length));
		});
		timeSlider.setOnMouseDragged(e -> {
			if (e.getX() > timeSlider.getWidth() || e.getX() < 0)
				return;
			double pos = e.getX() / timeSlider.getWidth();
			sequencer.setMicrosecondPosition((long) (pos * length));
		});
		timeSlider.setOnMouseMoved(e -> {
			if (e.getX() > timeSlider.getWidth() || e.getX() < 0)
				return;
			double pos = e.getX() / timeSlider.getWidth();
			long microsec = (long) (pos * length);
			timeSlider.getTooltip().setText("Jump To " + microsecToString(microsec));
		});
		/*sequencer.addMetaEventListener(e->{
			if(e.getMessage()[1]==0x58){
				for(byte i:e.getMessage())
				System.out.print(i+" ");
				System.out.println();
			}			
		});*/
	}

	private String microsecToString(long microSecs) {
		return String.format("%d:%2d", (microSecs / 1000000 / 60),(microSecs / 1000000 % 60) );				
	}

	private void updateTempo() {
		float currentTempo = sequencer.getTempoInBPM();
		bpmTextfield.setText((int) (currentTempo * sequencer.getTempoFactor()) + "");
		tempoTextfield.setText((int) (sequencer.getTempoFactor() * 100) + "%");
		if (tempoUnit == 1) {
			if (tempoSlider.getValue() <= currentTempo)
				tempoSlider.setMax(currentTempo + sliderAddAtEnd);
			tempoSlider.setValue(currentTempo * sequencer.getTempoFactor());
		} else if (tempoUnit == 0)
			if (sequencer.getTempoFactor() * 100 >= tempoSlider.getMax()) {
				tempoSlider.setMax(tempoSlider.getMax() + sliderAddAtEnd);
			}
	}

	public void setSequencer(Sequencer sequencer) {
		this.sequencer = sequencer;
	}

	@FXML
	protected void settingsPressed(ActionEvent event) {
		Settings.openInWindow();
	}
	
	@FXML
	protected void stopButtonPressed(ActionEvent event) {
		sequencer.stop();
		sequencer.setMicrosecondPosition(0);
		playbackButton.setSelected(false);
		playbackButton.setTooltip(new Tooltip("Play"));
		playbackButton.setGraphic(playPath);
	}

	@FXML
	protected void playbackButtonPressed(ActionEvent event) {
		if (playbackButton.isSelected()) {
			if (sequencer.getSequence() != null) {
				if (sequencer.getMicrosecondPosition() == length)
					sequencer.setMicrosecondPosition(0);
				sequencer.start();
				playbackButton.setTooltip(new Tooltip("Pause"));
				playbackButton.setGraphic(pausePath);
			} else
				playbackButton.setSelected(false);
		} else {
			sequencer.stop();
			playbackButton.setTooltip(new Tooltip("Play"));
			playbackButton.setGraphic(playPath);
		}
	}

	@FXML
	protected void previous(ActionEvent event) {
		if(playlistViewer==null)sequencer.setMicrosecondPosition(0);
		else playlistViewer.previous();
	}
	
	@FXML
	protected void forward(ActionEvent event) {
		if(playlistViewer==null)sequencer.setMicrosecondPosition(length);
		else playlistViewer.next();
	}

	@FXML
	protected void forwardPressed(ActionEvent event) {
		long currentTime = sequencer.getMicrosecondPosition();
		sequencer.setMicrosecondPosition(currentTime + forwardOrRewindFactor);
	}

	@FXML
	protected void rewindPressed(ActionEvent event) {
		long currentTime = sequencer.getMicrosecondPosition();
		sequencer.setMicrosecondPosition(currentTime - forwardOrRewindFactor);
	}
}
