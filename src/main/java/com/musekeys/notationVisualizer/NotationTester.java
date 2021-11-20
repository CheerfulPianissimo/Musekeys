package com.musekeys.notationVisualizer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class NotationTester extends Application{
	@Override
	public void start(Stage primaryStage) throws Exception {	
		NotationVisualizer visualizer=new NotationVisualizer();
		BorderPane pane=new BorderPane();		
		pane.setCenter(visualizer);
		Scene scene=new Scene(pane);		
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setMaximized(true);		
	}
	
	public static void main(String[] args) {
		launch();
	}
}
