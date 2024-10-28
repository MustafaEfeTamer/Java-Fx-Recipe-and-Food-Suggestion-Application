package com.example.yaz_lab1_deneme1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class GirisEkrani {
    @FXML
    private TextField userName;

    @FXML
    private PasswordField passWord;

    @FXML
    private Label result;

    @FXML
    ImageView myImageView;

    @FXML
    private AnchorPane girisEkraniAnchorPane;

    Image myImage = new Image(getClass().getResourceAsStream("/resimler/yemek_tarifi.jpg")); // resmi resources'a koymak lazım !!!!!!!


    @FXML
    void login(ActionEvent event) throws IOException {
        if (userName.getText().equals("admin") && passWord.getText().equals("admin123")){
            result.setText("Giriş Başarılı");

            new SceneSwitch(girisEkraniAnchorPane, "ana-ekran.fxml");
        }else{
            result.setText("Kullanıcı Adı veya Şifre Hatalı");
        }
    }
}
