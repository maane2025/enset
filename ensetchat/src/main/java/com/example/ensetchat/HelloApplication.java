package com.example.ensetchat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat-view.fxml")); // Assurez-vous que le nom du fichier FXML est correct
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("EnsetChat"); // Ou le titre que vous souhaitez
        stage.setScene(scene);



        // Ajout du fichier css : Assurez vous d'avoir un fichier avec le nom styles.css
        scene.getStylesheets().add(HelloApplication.class.getResource("styles.css").toExternalForm());


        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}