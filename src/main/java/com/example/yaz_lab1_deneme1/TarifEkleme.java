package com.example.yaz_lab1_deneme1;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class TarifEkleme implements Initializable {
    @FXML
    private AnchorPane tarifIslemleriAnchorPane;

    @FXML
    private TextField birimFiyati;

    @FXML
    private TextField hazirlanmaSuresi;

    @FXML
    private TextField kategori;

    @FXML
    private TextField malzemeAdi;

    @FXML
    private TextField malzemeAdiGiriniz;

    @FXML
    private TextField malzemeBirimi;

    @FXML
    private ListView<String> malzemeListView;

    @FXML
    private TextField malzemeMiktari;

    @FXML
    private TextArea tarif;

    @FXML
    private TextField tarifAdi;

    @FXML
    private TextField toplamMiktar;

    @FXML
    private TextField yeniMalzemeAdi;

    @FXML
    private Label malzemeEklemeUyari;


    ArrayList<String> malzemeListesi = new ArrayList<>();
    String suAnkiMalzeme;
    int malzemeAdiAramaSayac = 0;
    int tarifeMalzemeEklemeSayac = 0;
    int tarifId;


    // ilk başta her zaman bu method çalışır
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        malzemeAdiAra(null);

        malzemeListView.getItems().addAll(malzemeListesi);
        malzemeListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                suAnkiMalzeme = malzemeListView.getSelectionModel().getSelectedItem();
                malzemeAdi.setText(suAnkiMalzeme);
            }
        });
    }


    @FXML
    void malzemeAdiAra(ActionEvent event) {
        if(malzemeAdiGiriniz.getText().equals("")){
            malzemeListesi.clear(); // Listeyi temizliyoruz
            try{
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM MALZEMELER");

                while(resultSet.next()){
                    malzemeListesi.add(resultSet.getString("MalzemeAdi"));
                }
            }catch(SQLException exception){
                exception.printStackTrace();
            }
            // tekrardan malzemeAdiArama alanını boş bir şekilde çalıştırırsam bütün malzeme listesini versin diye bu işlemi yaptım
            if(malzemeAdiAramaSayac > 0){
                malzemeListView.getItems().clear();
                malzemeListView.getItems().addAll(malzemeListesi);
            }
            malzemeAdiAramaSayac++;

        } else {
            // Eğer kullanıcı bir malzeme adı girmişse, o malzeme adını içerenleri bul
            malzemeListesi.clear(); // Listeyi temizle
            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                suAnkiMalzeme = malzemeAdiGiriniz.getText();

                // LIKE sorgusu için '%suAnkiMalzeme%' oluşturulmalı
                String sql = "SELECT * FROM MALZEMELER WHERE MalzemeAdi LIKE ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, "%" + suAnkiMalzeme + "%");

                ResultSet resultSet2 = preparedStatement.executeQuery();

                while (resultSet2.next()) {
                    malzemeListesi.add(resultSet2.getString("MalzemeAdi"));
                }
                malzemeListView.getItems().setAll(malzemeListesi); // ListView'i güncelle
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    @FXML
    void tarifeMalzemeEkle(ActionEvent event) {
        String tarifAd = tarifAdi.getText();
        String tarifKategori = kategori.getText();
        String tarifHazirlanis = tarif.getText();
        String malzemeAd = malzemeAdi.getText();
        int malzemeId = -1;

        try{
            if(tarifeMalzemeEklemeSayac <= 0){
                // extra malzeme eklerken bize sıkıntı çıkarmasın diye bi üst tarafa koymadık !!
                tarifId = -1;

                // Veritabanına bağlantısı oluşturuyorum
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                // Malzeme adını kullanarak malzeme ID'sini alıyorum
                String sql = "SELECT MalzemeID FROM MALZEMELER WHERE MalzemeAdi = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, malzemeAd);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    malzemeId = resultSet.getInt("MalzemeID");
                }


                if(malzemeId != -1){
                    if(hazirlanmaSuresi.getText() == null || hazirlanmaSuresi.getText().trim().isEmpty() || malzemeMiktari.getText() == null || malzemeMiktari.getText().trim().isEmpty()){
                        malzemeEklemeUyari.setText("Lütfen Tüm Alanları Doldurunuz !!!");
                        return;
                    }

                    int hazirlanmaSure = Integer.parseInt(hazirlanmaSuresi.getText());
                    float malzemeMiktar = Float.parseFloat(malzemeMiktari.getText());

                    if(tarifAd == null || tarifAd.trim().isEmpty() || tarifKategori == null || tarifKategori.trim().isEmpty() || tarifHazirlanis == null || tarifHazirlanis.trim().isEmpty() || malzemeAd == null || malzemeAd.trim().isEmpty() || hazirlanmaSure <=0 || malzemeMiktar <=0){
                        malzemeEklemeUyari.setText("Lütfen Tüm Alanları Doldurunuz !!!");
                        return;
                    }

                    // Veritabanına bağlantısı oluşturuyorum
                    Connection connection2 = DriverManager.getConnection(
                            "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                            "springstudent",
                            "springstudent"
                    );

                    // SQL INSERT komutunu hazırlıyorum
                    String sql2 = "INSERT INTO TARIFLER (TarifAdi, Kategori, Talimatlar, HazirlamaSuresi) VALUES (?, ?, ?, ?)";

                    PreparedStatement preparedStatement2 = connection2.prepareStatement(sql2);
                    // Değerleri SQL komutuna ekliyorum
                    preparedStatement2.setString(1, tarifAd);
                    preparedStatement2.setString(2, tarifKategori);
                    preparedStatement2.setString(3, tarifHazirlanis);
                    preparedStatement2.setFloat(4, hazirlanmaSure);

                    // Yeni Tarif Eklemeden önce tarif veritabanında var mı diye duplicate kontrolü yapıyoruz.
                    if(duplicateKontrolu()){
                        malzemeEklemeUyari.setText("Bu Tarif Adı Zaten Mevcut !!");
                        return;
                    }

                    // Sorguyu çalıştırıyorum
                    int rowsAffected = preparedStatement2.executeUpdate();

                    String sql4 = "SELECT TarifID FROM TARIFLER WHERE TarifAdi = ?";

                    PreparedStatement preparedStatement3 = connection.prepareStatement(sql4);
                    preparedStatement3.setString(1, tarifAd);

                    ResultSet resultSet2 = preparedStatement3.executeQuery();

                    if (resultSet2.next()) {
                        tarifId = resultSet2.getInt("TarifID");
                    }

                    if (rowsAffected > 0) {
                        String sql3 = "INSERT INTO TARIFMALZEMELER (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";

                        PreparedStatement preparedStatement4 = connection.prepareStatement(sql3);

                        // Değerleri SQL komutuna ekliyorum
                        preparedStatement4.setInt(1, tarifId);
                        preparedStatement4.setInt(2, malzemeId);
                        preparedStatement4.setFloat(3, malzemeMiktar);

                        // Sorguyu çalıştırıyorum
                        int rowsAffected2 = preparedStatement4.executeUpdate();

                        if(rowsAffected2 > 0){
                            malzemeEklemeUyari.setText("Tarif Başarı İle Eklendi Başka Malzeme Varsa Girebilirsiniz !!!");

                            // Butona her bastığımızda kullanıcının yeni malzeme girmesi için textfield ları hazır hale getiriyorum
                            malzemeAdi.clear();
                            malzemeMiktari.clear();
                        }
                    }

                    // Bağlantıyı kapatıyoruz
                    preparedStatement.close();
                    connection.close();

                    malzemeId = -1;
                    tarifeMalzemeEklemeSayac++;
                }else{
                    malzemeEklemeUyari.setText("Lütfen Geçerli Bir Malzeme Giriniz");
                }
            }else{
                // Veritabanına bağlantı oluşturuyoruz
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                // Malzeme adını kullanarak malzeme ID'sini alıyoruz
                String sql = "SELECT MalzemeID FROM MALZEMELER WHERE MalzemeAdi = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, malzemeAd);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    malzemeId = resultSet.getInt("MalzemeID");
                }

                System.out.println(malzemeId);

                if(malzemeId != -1){
                    if(malzemeMiktari.getText() == null || malzemeMiktari.getText().trim().isEmpty()){
                        malzemeEklemeUyari.setText("Lütfen Tüm Alanları Doldurunuz!!!");
                        return;
                    }

                    float malzemeMiktar = Float.parseFloat(malzemeMiktari.getText());

                    if(malzemeAd == null || malzemeAd.trim().isEmpty() || malzemeMiktar <=0){
                        malzemeEklemeUyari.setText("Lütfen Tüm Alanları Doğru Şekilde Doldurunuz!!");
                        return;
                    }

                    String sql2 = "INSERT INTO TARIFMALZEMELER (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";

                    PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

                    // Değerleri SQL komutuna ekliyorum
                    preparedStatement2.setInt(1, tarifId);
                    preparedStatement2.setInt(2, malzemeId);
                    preparedStatement2.setFloat(3, malzemeMiktar);

                    System.out.println(tarifId);

                    // Sorguyu çalıştırıyorum
                    int rowsAffected = preparedStatement2.executeUpdate();

                    if(rowsAffected > 0){
                        malzemeEklemeUyari.setText("Tarif Başarı İle Eklendi Başka Malzeme Varsa Girebilirsiniz !!!");

                        // Butona her bastığımızda kullanıcının yeni malzeme girmesi için textfield ları hazır hale getiriyorum
                        malzemeAdi.clear();
                        malzemeMiktari.clear();
                    }
                }else{
                    System.out.println("Lütfen Geçerli Bir Malzeme Giriniz");
                }
            }
        }catch (SQLException exception){
            exception.printStackTrace();
        }
    }

    
    boolean duplicateKontrolu(){
        boolean mevcutmu = false;
        ArrayList<String> mevcutTarifler = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                    "springstudent",
                    "springstudent"
            );

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM TARIFLER");

            while(resultSet.next()){
                mevcutTarifler.add(resultSet.getString("TarifAdi"));
            }

            for(String tarifler : mevcutTarifler){
                if(tarifler.toLowerCase().equals(tarifAdi.getText().toLowerCase())){
                    mevcutmu = true;
                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return mevcutmu;
    }


    @FXML
    void yeniMalzemeEkle(ActionEvent event) {
        // TextField lardan yeni malzeme alıyorum
        String adi = yeniMalzemeAdi.getText();
        String toplamMiktari = toplamMiktar.getText();
        String malzemeBirim = malzemeBirimi.getText();

        if(birimFiyati.getText() == null || birimFiyati.getText().trim().isEmpty()){
            malzemeEklemeUyari.setText("Lütfen Tüm Alanları Doldurunuz !!!");
            return;
        }

        Float birimFiyat = Float.parseFloat(birimFiyati.getText());

        // Eğer yeni yeni malzeme eklerken eksik alan varsa uyarı verdiriyorum
        if (adi == null || adi.trim().isEmpty() || toplamMiktari == null || toplamMiktari.trim().isEmpty() || malzemeBirim == null || malzemeBirim.trim().isEmpty() || birimFiyat == null || birimFiyat <= 0) {
            malzemeEklemeUyari.setText("Lütfen Tüm Alanları Doldurunuz !!!");
            return;
        }

        try {
            // Veritabanına bağlantısı oluşturuyorum
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                    "springstudent",
                    "springstudent"
            );

            boolean malzemeVar = malzemeListesi.stream()
                    .anyMatch(malzeme -> malzeme.equalsIgnoreCase(yeniMalzemeAdi.getText()));

            if (malzemeVar) {
                malzemeEklemeUyari.setText("Bu malzeme zaten mevcut. Lütfen olmayana bir malzeme giriniz");
                toplamMiktar.clear();
                malzemeBirimi.clear();
                birimFiyati.clear();
                return;
            }


            // SQL INSERT komutunu hazırlıyorum
            String sql = "INSERT INTO MALZEMELER (MalzemeAdi, ToplamMiktar, MalzemeBirim, BirimFiyat) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            // Değerleri SQL komutuna ekliyorum
            preparedStatement.setString(1, adi);
            preparedStatement.setString(2, toplamMiktari);
            preparedStatement.setString(3, malzemeBirim);
            preparedStatement.setFloat(4, birimFiyat);

            // Sorguyu çalıştırıyorum
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                malzemeEklemeUyari.setText("Yeni malzeme başarıyla eklendi !!!");
                // Yeni malzemeyi listeye ekleyip ListView'i güncelleyebiliriz
                malzemeListesi.add(adi);
                malzemeListView.getItems().setAll(malzemeListesi);

                // TextField'ların içini temizliyorum
                yeniMalzemeAdi.clear();
                toplamMiktar.clear();
                malzemeBirimi.clear();
                birimFiyati.clear();
            }

            // Bağlantıyı kapatıyoruz
            preparedStatement.close();
            connection.close();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    void malzemeEklemeyiBitir(ActionEvent event) {
        tarifAdi.clear();
        kategori.clear();
        hazirlanmaSuresi.clear();
        tarif.clear();
        malzemeAdi.clear();
        malzemeMiktari.clear();
        tarifeMalzemeEklemeSayac = 0;
        malzemeEklemeUyari.setText("");
    }

    @FXML
    void anaEkranaDonus(ActionEvent event) throws IOException {
        new SceneSwitch(tarifIslemleriAnchorPane, "ana-ekran.fxml");
    }

    @FXML
    void tarifGuncellemeSilme(ActionEvent event) throws IOException {
        new SceneSwitch(tarifIslemleriAnchorPane, "guncelleme-silme-ekran.fxml");
    }
}
