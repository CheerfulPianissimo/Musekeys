package com.musekeys.waterfall;

import javafx.geometry.Rectangle2D;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.SortedMap;

import javax.sound.midi.*;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

public class WaterFall extends StackPane implements com.musekeys.midiplayer.MidiVisualizer{
	private Sequence sequence;

	public Receiver getReceiver() {
		return receiver;
	}

	private Receiver receiver;
	private Deque<NoteRectangle> rectangles;
	private Pane pane,animationPane;
	private Rectangle cursor;
	private Line[] octaveLines;
	private long length;
	private double drawPosition;
	private int[] keyOrder = new int[88];
	private double[] keyX=new double[88];
	protected SortedMap<Double, Note> notes;
	protected Sequencer sequencer;
	private BooleanProperty	useEffects = new SimpleBooleanProperty(false),
			seekOnClick=new SimpleBooleanProperty(true);
	private DoubleProperty scaleFactor=new SimpleDoubleProperty(1000);
	private IntegerProperty noteSensitivity=new SimpleIntegerProperty(50),visibleWhiteKeys
			=new SimpleIntegerProperty(52);
	private double whiteKeyWidth=0;
	private double blackKeyWidth=0;
	private Color[] colors = { Color.ORANGE, Color.LIGHTBLUE, Color.RED, Color.BROWN, Color.GREEN, Color.YELLOW,
			Color.VIOLET, Color.KHAKI, Color.PURPLE, Color.DARKGRAY, Color.PLUM, Color.TURQUOISE, Color.SPRINGGREEN,
			Color.INDIGO, Color.TAN, Color.BURLYWOOD, Color.TOMATO };
	private Paint[] paint=new Paint[16];
	private Paint backgroundColor;
	private boolean shouldRun=true;
	private boolean[] shouldRender=new boolean[16];
	private ObjectProperty<Mode> mode=new SimpleObjectProperty<Mode>(Mode.MICROSECONDS);
	private Note[][] noteMap = new Note[16][88];
	
	public enum Mode{
		TICKS,MICROSECONDS
	}
	
	public WaterFall(Sequencer sequencer) {		
		this.sequencer = sequencer;		
		rectangles=new ArrayDeque<>();
		pane=new Pane();		
		animationPane=new Pane(pane);
		for(int i=0;i<16;i++)
			shouldRender[i]=true;
		this.setBackgroundColor(Color.rgb(94, 224, 130));
		Rectangle clip=new Rectangle();
		clip.heightProperty().bind(this.heightProperty());
		clip.widthProperty().bind(this.widthProperty());
		Image img;
		try {
			img=new Image(new FileInputStream("./bgimg.png"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		ImageView bgImg=new ImageView(img);
		//bgImg.fitHeightProperty().bind(this.heightProperty());
		this.getChildren().add(bgImg);

		this.setBlendMode(BlendMode.SRC_OVER);
		this.setClip(clip);
		this.setTranslateZ(20);
		this.getChildren().add(animationPane);
		cursor=new Rectangle(0,5,Color.GREEN);
		cursor.widthProperty().bind(this.widthProperty());
		cursor.setFill(Color.GREEN);
		cursor.setStrokeWidth(5);
		pane.getChildren().add(cursor);				
		setupKeyOrder();
		addOctaveLines();
		updateGradients();
		refreshSequenceFromSequencer();
		setupUpdateThread();

		/*Rotate rotate=new Rotate();
		rotate.setAxis(Rotate.Z_AXIS);
		rotate.setAngle(20);
		rotate.pivotYProperty().bind(this.heightProperty().divide(2));
		rotate.pivotXProperty().bind(this.widthProperty().divide(2));
		getTransforms().addAll(rotate);*/
		pane.setOnMouseClicked(e->{
			if(!this.isFocused())
			this.requestFocus();
			if(isSeekOnClick()&&e.getButton().equals(MouseButton.MIDDLE)){
				if(getMode()==Mode.MICROSECONDS)
					sequencer.setMicrosecondPosition((long) (length-(e.getY()*getScaleFactor())));
				else
					sequencer.setTickPosition((long) (length-(e.getY()*getScaleFactor())));
			}
			else if(e.getButton().equals(MouseButton.SECONDARY)){
				Stage stage=new Stage(StageStyle.UTILITY);
				stage.setAlwaysOnTop(true);
				stage.setResizable(false);
				WaterFallSettings setup=new WaterFallSettings(this);
				stage.setScene(new Scene(setup));
				stage.show();
			}	
		});
		mode.addListener(e->{
			setScaleFactor((getMode()==Mode.TICKS)?10:10000);
			refreshSequenceFromSequencer();
		});
		/*this.setOnMouseClicked(e->{
			if(e.getButton().equals(MouseButton.SECONDARY)){
				Stage stage=new Stage(StageStyle.UTILITY);
				stage.setAlwaysOnTop(true);
				stage.setResizable(false);
				WaterFallSettings setup=new WaterFallSettings(this);
				stage.setScene(new Scene(setup));
				stage.show();
			}				
		});*/
		this.widthProperty().addListener(
			(ObservableValue<? extends Number> obv, Number oldValue, Number newValue) -> {				
			whiteKeyWidth = newValue.doubleValue() / 52;
			blackKeyWidth = whiteKeyWidth / 2;
			bgImg.setFitWidth(newValue.doubleValue());

			for(int co=0;co<keyX.length;co++)
				keyX[co]=calculateX(co);
			refresh();
		});
		this.heightProperty().addListener(
				(ObservableValue<? extends Number> obv, Number oldValue, Number newValue) ->{
					bgImg.setFitHeight(newValue.doubleValue());
				}
		);
		scaleFactorProperty().addListener(
			(ObservableValue<? extends Number> obv, Number oldValue, Number newValue) -> {
				this.refresh();	
		});		
		useEffectsProperty().addListener(e->{
			updateGradients();
			refresh();			
		});
		noteSensitivityProperty().addListener(e->refresh());
		this.setOnKeyPressed(e->{
			int factor=(getMode()==Mode.MICROSECONDS)?250:3;
			if (e.getText().equals("-")) {
				if(getScaleFactor()+factor<((getMode()==Mode.MICROSECONDS)?200000:1000))
				this.setScaleFactor(this.getScaleFactor()+factor);
			}else if (e.getText().equals("+")){
				if(getScaleFactor()-factor>1)
				this.setScaleFactor(this.getScaleFactor()-factor);
			}else if(e.getText().equals("2")){
					sequencer.setMicrosecondPosition(sequencer.getMicrosecondPosition()+500000);
			}else if(e.getText().equals("1")){
				sequencer.setMicrosecondPosition(sequencer.getMicrosecondPosition()-500000);
		}
		});
		this.setOnScroll(e->{		
			if(!sequencer.isRunning())
				if(getMode()==Mode.MICROSECONDS)
					sequencer.setMicrosecondPosition((long)(sequencer.getMicrosecondPosition()+
					e.getDeltaY()*getScaleFactor()));
				else
					sequencer.setTickPosition((long)(sequencer.getTickPosition()+
							e.getDeltaY()*getScaleFactor()));
		});		
		this.visibleWhiteKeysProperty()
		.addListener((ObservableValue<? extends Number> obv, Number oldValue, Number newValue) -> {					
			whiteKeyWidth = this.getWidth() / getVisibleWhiteKeys();
			blackKeyWidth = whiteKeyWidth / 2;										
			for(int co=0;co<keyX.length;co++)
				keyX[co]=calculateX(co);
			refresh();			
			if (newValue.intValue() != 52) {
				ScrollPane container = new ScrollPane();						
				container.setContent(pane);
				this.getChildren().clear();
				this.getChildren().add(container);
			} else{
				this.getChildren().clear();
				this.getChildren().add(animationPane);
			}
		});

		receiver = new Receiver() {
			@Override
			public void send(MidiMessage message, long timeStamp) {
				try {
					if (message instanceof ShortMessage) {
						ShortMessage shortMsg = (ShortMessage) message;
						if ((shortMsg.getCommand() == ShortMessage.NOTE_OFF)
								|| (shortMsg.getCommand() == ShortMessage.NOTE_ON)) {
							int channel = shortMsg.getChannel();
							int noteNo = shortMsg.getData1();
							int pianoNoteNo = noteNo - 21;
							long pos;
							if(getMode()==Mode.TICKS)
								pos= sequencer.getTickPosition();
							else pos=sequencer.getMicrosecondPosition();
							if((shortMsg.getCommand() == ShortMessage.NOTE_OFF)&&noteMap[channel][pianoNoteNo]!=null){
								Note prevNote=noteMap[channel][pianoNoteNo];
								prevNote.setEndEvent(new MidiEvent(message,pos));
								prevNote.setEnd(pos);
								noteMap[channel][pianoNoteNo]=null;
								refresh();
							}else if(shortMsg.getCommand() == ShortMessage.NOTE_ON) {
								Note playNote = new Note(pos, -1);
								playNote.setStartEvent(new MidiEvent(message, pos));
								notes.put(drawPosition, playNote);
								noteMap[channel][pianoNoteNo]=playNote;
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void close() {

			}
		};
	}

	private void addOctaveLines() {
		octaveLines=new Line[8];	
		for(int co=0;co<8;co++){
			octaveLines[co]=new Line();
			octaveLines[co].setStroke(Color.WHITE);
			octaveLines[co].setStartY(0);
			octaveLines[co].endYProperty().bind(pane.heightProperty());
			octaveLines[co].setStrokeWidth(0.5);
			octaveLines[co].endXProperty().bind(octaveLines[co].startXProperty());
			pane.getChildren().add(octaveLines[co]);
		}
		updateOctaveLines();
	}

	private void updateOctaveLines() {
		for(int co=3;co<88;co=co+12){
			octaveLines[(co-3)/12].setStartX(keyX[co]);
		}
	}

	private void updateGradients() {
		for(int co=0;co<16;co++){
			Color color=(Color)colors[co];
			if(getUseEffects()){
			LinearGradient gradient=new LinearGradient(0, 1, 1, 1, true,
				CycleMethod.NO_CYCLE,new Stop(0, color),new Stop(1.0,color.darker()));
			paint[co]=gradient;
			}else paint[co]=color;
		}	
	}
	
	private void setupUpdateThread() {
		Thread updateThread=new Thread(()->{
			while(true){
				try {
					//Thread.sleep(10);
				 } catch (Exception e1) {
					e1.printStackTrace();
				}
			//if(shouldRun==false)continue;
			if(sequencer.getSequence()==null){
				if(pane.isVisible())
				pane.setVisible(false);				
				continue;			
			}
			/*if(sequencer.getSequence()!=sequence){
				shouldRun=false;		
				pane.setVisible(true);

				continue;
			}*/
			//Platform.runLater(()->refreshSequenceFromSequencer());

			long pos; 
			if(getMode()==Mode.TICKS)
			pos=sequencer.getTickPosition();
			else pos=sequencer.getMicrosecondPosition();
			//System.out.println((pos/sequencer.getSequence().getResolution()));
			double height=animationPane.getHeight();
			double nextPos=pos+(height*getScaleFactor());
			if(nextPos==drawPosition){
				continue;
			}
			if(nextPos<drawPosition||drawPosition*2<nextPos)
				drawPosition=pos;				
			SortedMap<Double, Note> current = notes.subMap(drawPosition,nextPos);
			drawPosition=nextPos;
			Platform.runLater(()->{			
			update(current,pos);
			cursor.setTranslateY(-drawPosition/getScaleFactor()-height/2.0);
			pane.setTranslateY(-(pos/getScaleFactor()-height));
			});			 
			}
		});		 
		updateThread.start();
	}

	/*private void updateLoops() {
		Rectangle loop=new Rectangle();
		if(getMode()==Mode.TICKS){
			loop.setY((length-sequencer.getLoopEndPoint())/getScaleFactor());
		}else loop.setY((length-MidiUtils.microsecond2tick(sequencer.getSequence(),
				sequencer.getLoopEndPoint(), null)/getScaleFactor()));
		
		loop.setFill(Color.DODGERBLUE);
		loop.setOpacity(0.5);
		pane.getChildren().add(loop);
		
	}*/

	protected void update(SortedMap<Double, Note> currentNotes,long currentTime) {
		double prevPos=currentTime-(animationPane.getHeight()*getScaleFactor());
		//System.out.println(rectangles.size());
		/*for(int co=0;co<pane.getChildren().size();co++){
			Node node=pane.getChildren().get(co);
			if(node instanceof NoteRectangle){
				NoteRectangle rect=(NoteRectangle)node;				
				if(rect.getNote().getEnd()<prevPos){
					pane.getChildren().remove(co);
					rectangles.add(rect);
				}//else if(rect.getNote().getStart()<currentTime&&rect.getNote().getEnd()>currentTime){
					//rect.setFill(((Color)rect.getFill()).deriveColor(0,1,1.3,1));
				//}
			}//else System.out.println(node);
		}*/
		for (Map.Entry<Double, Note> note: currentNotes.entrySet()) {
			Note value=note.getValue();
			if(shouldRender[value.getChannel()]==true)
			pane.getChildren().add(getRectangle(value));
		}		
		//System.out.println(pane.getChildren().size());
	}

	public void refresh(){
		shouldRun=false;
		if(getMode()==Mode.TICKS)
			drawPosition=sequencer.getTickPosition();
		else drawPosition=sequencer.getMicrosecondPosition();

		/*for(int co=0;co<pane.getChildren().size();co++){
			if(pane.getChildren().get(co) instanceof NoteRectangle){				
				rectangles.add((NoteRectangle) pane.getChildren().remove(co));
			}
		}*/
		pane.getChildren().clear();
		pane.getChildren().add(cursor);
		for(Line line:octaveLines){
			pane.getChildren().add(line);
		}
		updateOctaveLines();

		SortedMap<Double, Note> current = notes.subMap(
				drawPosition-(animationPane.getHeight()*getScaleFactor()),drawPosition);
		update(current,(long)drawPosition);
		shouldRun=true;
	}
	
	public Sequence getSequence() {
		return sequence;
	}

	public void refreshSequenceFromSequencer() {
		shouldRun=false;		
		rectangles.clear();
		this.sequence = sequencer.getSequence();
		NoteCreator noteCreator = new NoteCreator(sequence,this.getMode());
		notes = noteCreator.createNotes();	
		if(getMode()==Mode.TICKS)
		this.length = sequence.getTickLength();
		else this.length=sequencer.getMicrosecondLength();
		refresh();
		shouldRun=true;
	}

	private void setupKeyOrder() {
		keyOrder[0] = keyOrder[2] = 0;
		keyOrder[1] = 1;
		for (int i = 3; i < keyOrder.length - 1; i = i + 12) {
			keyOrder[i] = keyOrder[i + 2] = keyOrder[i
					+ 4] = keyOrder[i + 5] = keyOrder[i + 7] = keyOrder[i + 9] = keyOrder[i + 11] = 0;
			keyOrder[i + 1] =
					keyOrder[i + 3] = keyOrder[i + 6] = keyOrder[i + 8] = keyOrder[i + 10] = 1;
		}
		keyOrder[87] = 0;
	}

    /*private static Lighting lighting;
    static {
        Light.Distant light = new Light.Distant();
        light.setAzimuth(0);
        light.setElevation(100.0);
        lighting = new Lighting();
        lighting.setLight(light);
        lighting.setSurfaceScale(2.0);
    }*/
	private static DropShadow ds;
	static {
		ds = new DropShadow();
		ds.setOffsetY(3.0);
		ds.setOffsetX(3.0);
		ds.setColor(Color.GRAY);
	}

	private Rectangle getRectangle(Note note) {	 
		NoteRectangle rect;
		if(rectangles.isEmpty()){
			rect = new NoteRectangle();
            //rect.setEffect(ds);
            /*rect.setArcHeight(10);
            rect.setArcWidth(10);*/
            rectangles.add(rect);
		}
		rect=rectangles.remove();		
		/*if(rect.getEffect()==null){			
			rect.setEffect(shadow);
		}*/
		rect.setNote(note);
		rect.setWidth(whiteKeyWidth);
		if(note.getEnd()==-1){
			rect.setHeight(animationPane.getHeight()*2);
		}else{
			rect.setHeight((note.getEnd() - note.getStart()) / getScaleFactor());
		}

		rect.setFill(paint[note.getChannel()]);
		if(keyOrder[note.getNoteNo()] == 1){			
			rect.setWidth(blackKeyWidth);			
		}
		int strength=note.getStartMessage().getData2();		
		double opacity=(strength*100)/getNoteSensitivity();
		rect.setOpacity(opacity/100);
		rect.setX(keyX[note.getNoteNo()]);
		rect.setY((note.getStart()) / getScaleFactor());
		return rect;
	}

	private double calculateX(int noteNo) {
		double totalX = 0;
		int newNote = noteNo;
		if (keyOrder[noteNo] == 1)
			newNote = noteNo - 1;
		//int octaveNo=(noteNo-3)/12;
		int white = 0;
		for (int co = 0; co < newNote; co++)
			if (keyOrder[co] == 0)
				white++;
		newNote = white;
		totalX = (whiteKeyWidth) * newNote;
		if (keyOrder[noteNo] == 1)
			totalX = totalX + (whiteKeyWidth + blackKeyWidth) / 2;
		return totalX;
	}	
	
	public DoubleProperty scaleFactorProperty() {
		return this.scaleFactor;
	}
	

	public double getScaleFactor() {
		return this.scaleFactorProperty().get();
	}
	

	public void setScaleFactor(final double scaleFactor) {
		this.scaleFactorProperty().set(scaleFactor);
	}
	
	public BooleanProperty useEffectsProperty() {
		return this.useEffects;
	}
	

	public boolean getUseEffects() {
		return this.useEffectsProperty().get();
	}
	

	public void setUseEffects(final boolean useEffects) {
		this.useEffectsProperty().set(useEffects);
	}

	public IntegerProperty noteSensitivityProperty() {
		return this.noteSensitivity;
	}
	

	public int getNoteSensitivity() {
		return this.noteSensitivityProperty().get();
	}
	

	public void setNoteSensitivity(final int noteSensitivity) {
		this.noteSensitivityProperty().set(noteSensitivity);
	}

	public BooleanProperty seekOnClickProperty() {
		return this.seekOnClick;
	}

	public boolean isSeekOnClick() {
		return this.seekOnClickProperty().get();
	}
	

	public void setSeekOnClick(final boolean seekOnClick) {
		this.seekOnClickProperty().set(seekOnClick);
	}
	
	public void setChannelColor(int channel, Color paint) {
		colors[channel] = paint;
		updateGradients();
		refresh();
	}

	public Color getChannelColor(int channel) {
		return colors[channel];
	}

	public ObjectProperty<Mode> modeProperty() {
		return this.mode;
	}
	

	public com.musekeys.waterfall.WaterFall.Mode getMode() {
		return this.modeProperty().get();
	}
	

	public void setMode(final com.musekeys.waterfall.WaterFall.Mode mode) {
		this.modeProperty().set(mode);
	}

	public IntegerProperty visibleWhiteKeysProperty() {
		return this.visibleWhiteKeys;
	}
	

	public int getVisibleWhiteKeys() {
		return this.visibleWhiteKeysProperty().get();
	}
	

	public void setVisibleWhiteKeys(final int visibleWhiteKeys) {
		this.visibleWhiteKeysProperty().set(visibleWhiteKeys);
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {
		//Does nothing		
	}

	@Override
	public void close() {
		//Does nothing	
	}

	public boolean getShouldRender(int channel) {
		return shouldRender[channel];
	}

	public void setShouldRender(int channel,boolean shouldRender) {
		this.shouldRender[channel] = shouldRender;
		refresh();
	}

	public void setBackgroundColor(Paint color){
	    backgroundColor=color;
	    /*Background background=new Background(new BackgroundImage(
	            new Image("file:///C:/Users/USER/AppData/Roaming/OpenSpades/Resources/Screenshots/shot0007.jpg")
        ,BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.CENTER,BackgroundSize.DEFAULT));*/
        Background background=new Background(new BackgroundFill(color,null,null));
        this.setBackground(background);
    }

    public Paint getBackgroundColor() {
        return backgroundColor;
    }
}

class NoteRectangle extends Rectangle{
	private Note note;

	public Note getNote() {
		return note;
	}

	public void setNote(Note note) {
		this.note = note;
	}
}