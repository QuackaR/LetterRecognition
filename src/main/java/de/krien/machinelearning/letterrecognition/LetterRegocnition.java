package de.krien.machinelearning.letterrecognition;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LetterRegocnition extends Application {

	public static void main(String[] args) {
		LetterRegocnition.launch(LetterRegocnition.class, args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/gui/gui.fxml"));

		stage.setTitle("FXML Welcome");
		stage.setScene(new Scene(root, 1440, 900));
		stage.show();
	}
}