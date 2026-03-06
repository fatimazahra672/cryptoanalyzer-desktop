package com.app.CryptoAnalyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

public class  Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/CryptoAnalyser/View/main.fxml"));

        // Obtenir les dimensions de l'écran
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Utiliser 90% de la largeur et hauteur de l'écran
        double width = screenBounds.getWidth() * 0.9;
        double height = screenBounds.getHeight() * 0.9;

        Scene scene = new Scene(loader.load(), width, height);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("CryptoAnalyser");
        stage.setScene(scene);

        // Centrer la fenêtre
        stage.setX((screenBounds.getWidth() - width) / 2);
        stage.setY((screenBounds.getHeight() - height) / 2);

        // Permettre le redimensionnement
        stage.setResizable(true);

        // Définir les dimensions minimales
        stage.setMinWidth(1024);
        stage.setMinHeight(768);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
