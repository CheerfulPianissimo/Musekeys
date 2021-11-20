package com.musekeys.playlist;

import java.io.File;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class MidiFileModel {
	private SimpleStringProperty fileName;
	private SimpleStringProperty folder,size;
	private SimpleBooleanProperty playing;
	private File file;
	
	public MidiFileModel(){
		fileName=new SimpleStringProperty();
		folder=new SimpleStringProperty();
		size=new SimpleStringProperty();
		playing=new SimpleBooleanProperty(false);
	}

	public SimpleStringProperty fileNameProperty() {
		return this.fileName;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileName() {
		return this.fileNameProperty().get();
	}
	

	public void setFileName(final java.lang.String fileName) {
		this.fileNameProperty().set(fileName);
	}
	

	public SimpleStringProperty folderProperty() {
		return this.folder;
	}
	

	public String getFolder() {
		return this.folderProperty().get();
	}
	

	public void setFolder(String folder) {
		this.folderProperty().set(folder);
	}

	public SimpleBooleanProperty playingProperty() {
		return this.playing;
	}
	

	public boolean isPlaying() {
		return this.playingProperty().get();
	}
	

	public void setPlaying(final boolean playing) {
		this.playingProperty().set(playing);
	}

	public SimpleStringProperty sizeProperty() {
		return this.size;
	}
	

	public java.lang.String getSize() {
		return this.sizeProperty().get();
	}
	

	public void setSize(final java.lang.String size) {
		this.sizeProperty().set(size);
	}
	
		
}
