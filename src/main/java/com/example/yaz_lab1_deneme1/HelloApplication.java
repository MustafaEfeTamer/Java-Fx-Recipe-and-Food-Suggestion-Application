package com.example.yaz_lab1_deneme1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("giris-ekran.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Ana Ekran");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            File soundFile = new File("C:\\JavaIntellijKodlarım\\RecipeAndFoodSuggestionApplication\\upbeat-funk-commercial-advertising-music-253434.wav");

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            // Müziği sonsuz döngüye alır ve müzik bittikçe çalar
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch();
    }
}