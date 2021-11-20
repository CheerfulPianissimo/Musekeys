package com.musekeys.playlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class PlaylistViewer extends VBox {
	private ObservableList<MidiFileModel> files = FXCollections.observableArrayList();
	@FXML
	private TableView<MidiFileModel> table;
	@FXML
	private TableColumn<MidiFileModel, String> fileName, folder, size;
	@FXML
	private TableColumn<MidiFileModel, Boolean> playing;
	@FXML
	private Button addFile, removeFile, up, down;
	@FXML 
	private ToggleButton repeatButton;
	@FXML
	private ObjectProperty<File> currentFile;
	private BooleanProperty repeat;

	public PlaylistViewer() {
		super();
		currentFile = new SimpleObjectProperty<File>();
		repeat = new SimpleBooleanProperty(true);
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Playlist.fxml"));
		fxmlLoader.setController(this);
		try {
			this.getChildren().add(fxmlLoader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		init();
		repeatButton.selectedProperty().bindBidirectional(repeat);
	}

	private void init() {
		table.setItems(files);
		table.setSortPolicy(null);
		Hyperlink placeholder = new Hyperlink("Add Files");
		placeholder.setOnAction(e -> addFile());
		table.setPlaceholder(placeholder);
		fileName.setCellValueFactory(new PropertyValueFactory<MidiFileModel, String>("fileName"));
		folder.setCellValueFactory(new PropertyValueFactory<MidiFileModel, String>("folder"));
		size.setCellValueFactory(new PropertyValueFactory<MidiFileModel, String>("size"));
		playing.setCellValueFactory(new PropertyValueFactory<MidiFileModel, Boolean>("playing"));
		playing.setSortable(false);
		playing.setCellFactory(CheckBoxTableCell.forTableColumn(playing));
		addFile.setOnAction(e -> addFile());		
		removeFile.setOnMousePressed(e -> {
			int index = table.getSelectionModel().getSelectedIndex();
			if (index != -1) {
				if (files.remove(index).isPlaying())
					next();
				if (files.size() == 0)
					setCurrentFile(null);
			}
		});
		up.setOnMousePressed(e -> {
			int index = table.getSelectionModel().getSelectedIndex();
			if (index != -1 && index != 0) {
				MidiFileModel model = files.get(index - 1);
				MidiFileModel model2 = files.get(index);
				files.set(index - 1, model2);
				files.set(index, model);
			}
		});
		down.setOnMousePressed(e -> {
			int index = table.getSelectionModel().getSelectedIndex();
			if (index != -1 && index + 1 < files.size()) {
				MidiFileModel model = files.get(index + 1);
				MidiFileModel model2 = files.get(index);
				files.set(index + 1, model2);
				files.set(index, model);
			}
		});
		
		this.setOnDragOver(e->{
			if(e.getDragboard().hasFiles()){
				e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
		});
		this.setOnDragDropped(e->{
			if(e.getDragboard().hasFiles()){			
				for(File file:e.getDragboard().getFiles())
					if(file.getAbsolutePath().endsWith(".mid")||file.getAbsolutePath().endsWith(".kar"))
						this.addFile(file);				
			}
			e.consume();
		});
	}
	
	private void refreshSelection(){
		for (int co = 0; co < files.size(); co++) {
			MidiFileModel model2 = files.get(co);
			if (model2.getFile().equals(getCurrentFile())==false)
				model2.setPlaying(false);
			else model2.setPlaying(true);
		}
	}
	
	public void next() {
		int index = -1;
		for (int co = 0; co < files.size(); co++) {
			if (files.get(co).isPlaying() == true)
				index = co;
		}
		if (files.isEmpty())
			return;
		if (index + 1 < files.size())
			files.get(index + 1).setPlaying(true);
		else if (files.size() == index + 1&&repeat.get())
			files.get(0).setPlaying(true);
	}

	public void previous() {
		int index = 0;
		for (int co = 0; co < files.size(); co++) {
			if (files.get(co).isPlaying() == true)
				index = co;
		}
		if (files.isEmpty())
			return;
		if (index - 1 != -1)
			files.get(index - 1).setPlaying(true);
		else if (-1 == index - 1)
			files.get(files.size() - 1).setPlaying(true);
	}

	public void addFile() {
		FileChooser fileChooser = new FileChooser();
		Preferences prefs=Preferences.userRoot();
		Preferences filePref=prefs.node("musekeys/filechooser");
		String lastFile=filePref.get("LAST_OPENED_PLAYLIST_DIRECTORY", "./");
		File init=new File(lastFile);
		if(init.exists())
		fileChooser.setInitialDirectory(init);
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Midi files(.kar & .mid)", "*.mid", "*.kar"),
				new FileChooser.ExtensionFilter("All Files", "*.*"));
		//fileChooser.setInitialDirectory(new File("G:/MIDI"));
		List<File> files = fileChooser.showOpenMultipleDialog(null);
		if (files == null)
			return;
		for (File file : files) {
			addFile(file);
		}		
		if(files.get(0)!=null)
		filePref.put("LAST_OPENED_PLAYLIST_DIRECTORY", files.get(0).getParent());
	}

	public void addFile(File file) {
		if (file == null)
			return;
		if (!file.isFile())
			return;
		MidiFileModel model = new MidiFileModel();
		model.setFileName(file.getName());
		model.setFolder(file.getParent());
		model.setSize(String.valueOf(file.length() / 1024) + " KB");
		model.setFile(file);
		model.playingProperty()
				.addListener((ObservableValue<? extends Boolean> obv, Boolean oldVal, Boolean newVal) -> {
					if (newVal == true) {						
						table.getSelectionModel().clearAndSelect(files.indexOf(model));
						table.getSelectionModel().focus(files.indexOf(model));
						setCurrentFile(model.getFile());						
					} 
					refreshSelection();
				});		
		files.add(model);
	}

	public ObjectProperty<File> currentFileProperty() {
		return this.currentFile;
	}

	public File getCurrentFile() {
		return this.currentFileProperty().get();
	}

	public void setCurrentFile(File currentFile) {
		this.currentFileProperty().set(currentFile);
	}
	
	public ObservableList<MidiFileModel> getFiles() {
		return files;
	}

	public void setFiles(ObservableList<MidiFileModel> files) {
		this.files = files;
	}
	
	public void save(File playlistOut) throws FileNotFoundException{
		PrintWriter writer=new PrintWriter(playlistOut);	
		for(MidiFileModel model:getFiles()){
			File file=model.getFile();
			writer.println(file.getAbsolutePath());
		}
		writer.close();
	}
	
	public void load(File playlistFile) throws FileNotFoundException{
		Scanner scanner=new Scanner(playlistFile);
		while(scanner.hasNextLine()){
			File file=new File(scanner.nextLine());
			addFile(file);
		}
		scanner.close();
	}
	
	public void clearAndLoad(File playlistFile) throws FileNotFoundException{
		files.clear();
		load(playlistFile);
	}
	
	@FXML protected void savePressed(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("./"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlist file","*.plst"));
		File file=fileChooser.showSaveDialog(null);
		if(file==null)return;
		try {
			save(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@FXML protected void loadPressed(){
		FileChooser fileChooser = new FileChooser();				
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Playlist file","*.plst"));
		File file=fileChooser.showOpenDialog(null);		
		try {
			load(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@FXML protected void clearAll(){
		files.clear();
	}	

	public BooleanProperty repeatProperty() {
		return this.repeat;
	}
	

	public boolean isRepeat() {
		return this.repeatProperty().get();
	}
	

	public void isRepeat(boolean isRepeat) {
		this.repeatProperty().set(isRepeat);
	}
	
}
