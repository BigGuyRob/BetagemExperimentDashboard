package com.example.experimentdashboard;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.File;

import static com.example.experimentdashboard.Globals.savedDataPath;


public class Main extends Application {
	
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("DashboardView.fxml"));
			Scene scene = new Scene(root,650,650);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			File f = new File(savedDataPath +"certList.txt");
			f.createNewFile();
			primaryStage.show();
			primaryStage.setMaximized(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void stop(){
	    DashboardController.finalDisconnect();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
