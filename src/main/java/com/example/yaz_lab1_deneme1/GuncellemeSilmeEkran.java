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


public class GuncellemeSilmeEkran implements Initializable {

    @FXML
    private AnchorPane guncellemeSilmeEkranAnchorPane;

    @FXML
    private TextField kategoriGuncellemekIçin;

    @FXML
    private TextField malzemeAdiGiriniz;

    @FXML
    private TextField malzemeAdiGuncellemekIçin;

    @FXML
    private TextField malzemeMiktariGuncellemekIçin;

    @FXML
    private ListView<String> malzemelerListView;

    @FXML
    private TextField suresiGuncellemekIçin;

    @FXML
    private TextField tarifAdiGiriniz;

    @FXML
    private TextField tarifAdiGuncellemekIçin;

    @FXML
    private TextField tarifAdiSilmekIçin;

    @FXML
    private TextArea tarifiGuncellemekIçin;

    @FXML
    private ListView<String> tariflerListView;

    @FXML
    private ListView<String> tariftekiMalzemelerListView;

    @FXML
    private Label uyariEkrani;


    ArrayList<String> tarifAdiListesi = new ArrayList<>();
    ArrayList<String> tumMalzemelerListesi = new ArrayList<>();
    ArrayList<Integer> malzemeIdListesi = new ArrayList<>();
    ArrayList<String> malzemelerListesi = new ArrayList<>();
    int tarifAdiAramaSayac = 0;
    int tumMalzemeAdiAramaSayac = 0;
    String suAnkiTarif;
    String suAnkiMalzeme;
    String suAnkiTumdenMalzeme;
    int suAnkiMalzemeId;
    int suAnkiTumdenMalzemeId;
    float suAnkiMalzemeMiktari;
    String tarifKategori;
    String hazirlamaSuresi;
    String tarif;
    int tarifId;


    // ilk başta her zaman bu method çalışır
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tarifAra(null);
        tariflerListView.getItems().addAll(tarifAdiListesi);
        tariflerListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                suAnkiTarif = tariflerListView.getSelectionModel().getSelectedItem();

                try {
                    Connection connection = DriverManager.getConnection(
                            "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                            "springstudent",
                            "springstudent"
                    );

                    String sql = "SELECT TarifID, Kategori, HazirlamaSuresi, Talimatlar FROM TARIFLER WHERE TarifAdi = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, suAnkiTarif);

                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        tarifKategori = resultSet.getString("Kategori");
                        hazirlamaSuresi = resultSet.getString("HazirlamaSuresi");
                        tarif = resultSet.getString("Talimatlar");
                        tarifId = resultSet.getInt("TarifID");
                    }

                    tarifAdiSilmekIçin.setText(suAnkiTarif);
                    tarifAdiGuncellemekIçin.setText(suAnkiTarif);
                    kategoriGuncellemekIçin.setText(tarifKategori);
                    suresiGuncellemekIçin.setText(hazirlamaSuresi);
                    tarifiGuncellemekIçin.setText(tarif);

                    // Tarif ID sine göre tarifmalzemeler tablosundan o tarifin malzemeID lerini alıyorum
                    sql = "SELECT MalzemeID FROM TARIFMALZEMELER WHERE TarifID = ?";
                    preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setInt(1, tarifId);
                    resultSet = preparedStatement.executeQuery();

                    // listeden yeni tarif seçilince mevcut malzemeler listesini boşaltmak için
                    malzemeIdListesi.clear();
                    // Tarifin her bir malzemesinin ID sini arraylist e atıyorum
                    while (resultSet.next()) {
                        malzemeIdListesi.add(resultSet.getInt("MalzemeID"));
                    }

                    // listeden yeni tarif seçilince mevcut malzemeler listesini boşaltır
                    malzemelerListesi.clear();
                    // Malzeme ID sine göre her bir malzemenin adını alıp bir arrayliste atıyorum
                    for (Integer id : malzemeIdListesi) {
                        String malzemeSql = "SELECT MalzemeAdi FROM MALZEMELER WHERE MalzemeID = ?";
                        PreparedStatement malzemePreparedStatement = connection.prepareStatement(malzemeSql);
                        malzemePreparedStatement.setInt(1, id);
                        ResultSet malzemeResultSet = malzemePreparedStatement.executeQuery();

                        if (malzemeResultSet.next()) {
                            malzemelerListesi.add(malzemeResultSet.getString("MalzemeAdi"));
                        }
                    }
                    // tarifin mevcut malzemelerini tabloda gösterir ve seçtiğimiz herhangi birini malzeme adında gösterir
                    tariftekiMalzemelerListView.getItems().setAll(malzemelerListesi);
                    tariftekiMalzemelerListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                            suAnkiMalzeme = tariftekiMalzemelerListView.getSelectionModel().getSelectedItem();

                            // aşağıdaki try kod bloğu şu anki malzeme Id sini bulmak için
                            try{
                                String sql2 = "SELECT MalzemeID FROM MALZEMELER WHERE MalzemeAdi = ?";
                                PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                                preparedStatement2.setString(1, suAnkiMalzeme);
                                ResultSet resultSet2 = preparedStatement2.executeQuery();

                                if (resultSet2.next()) {
                                    suAnkiMalzemeId = resultSet2.getInt("MalzemeID");
                                }

                                sql2 = "SELECT MalzemeMiktar FROM TARIFMALZEMELER WHERE TarifId = ? AND MalzemeID = ?";
                                preparedStatement2 = connection.prepareStatement(sql2);
                                preparedStatement2.setInt(1, tarifId);
                                preparedStatement2.setInt(2, suAnkiMalzemeId);
                                resultSet2 = preparedStatement2.executeQuery();

                                if (resultSet2.next()) {
                                    suAnkiMalzemeMiktari = resultSet2.getFloat("MalzemeMiktar");
                                }

                                malzemeAdiGuncellemekIçin.setText(suAnkiMalzeme);
                                malzemeMiktariGuncellemekIçin.setText(String.valueOf(suAnkiMalzemeMiktari));
                            }catch (Exception exception){
                                exception.printStackTrace();
                            }
                        }
                    });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        // Tüm malzemeleri arama listesi içinden seçmek için initialize
        malzemeAra(null);
        malzemelerListView.getItems().addAll(tumMalzemelerListesi);
        malzemelerListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                suAnkiMalzeme = malzemelerListView.getSelectionModel().getSelectedItem();

                malzemeAdiGuncellemekIçin.clear();
                malzemeMiktariGuncellemekIçin.clear();
                malzemeAdiGuncellemekIçin.setText(suAnkiMalzeme);

                try {
                    Connection connection = DriverManager.getConnection(
                            "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                            "springstudent",
                            "springstudent"
                    );

                    String sql = "SELECT MalzemeID FROM MALZEMELER WHERE MalzemeAdi = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, suAnkiMalzeme);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        suAnkiTumdenMalzemeId = resultSet.getInt("MalzemeID");
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });
    }


    @FXML
    void malzemeAra(ActionEvent event) {
        if(malzemeAdiGiriniz.getText().equals("")){
            tumMalzemelerListesi.clear(); // Listeyi temizliyoruz
            try{
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM MALZEMELER");

                while(resultSet.next()){
                    tumMalzemelerListesi.add(resultSet.getString("MalzemeAdi"));
                }
            }catch(SQLException exception){
                exception.printStackTrace();
            }
            // tekrardan malzeme Adı Arama alanını boş bir şekilde çalıştırırsam bütün malzeme listesini versin diye bu işlemi yaptım
            if(tumMalzemeAdiAramaSayac > 0){
                malzemelerListView.getItems().clear();
                malzemelerListView.getItems().addAll(tumMalzemelerListesi);
            }
            tumMalzemeAdiAramaSayac++;

        }else{
            // Eğer kullanıcı bir malzeme adı girmişse, o malzeme adını içerenleri bul
            tumMalzemelerListesi.clear(); // Listeyi temizle
            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                suAnkiTumdenMalzeme = malzemeAdiGiriniz.getText();

                // LIKE sorgusu için '%suAnkiTarif%' oluşturulmalı
                String sql = "SELECT * FROM MALZEMELER WHERE MalzemeAdi LIKE ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, "%" + suAnkiTumdenMalzeme + "%");
                ResultSet resultSet2 = preparedStatement.executeQuery();

                while (resultSet2.next()) {
                    tumMalzemelerListesi.add(resultSet2.getString("MalzemeAdi"));
                }

                malzemelerListView.getItems().setAll(tumMalzemelerListesi); // ListView'i güncelle
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    @FXML
    void malzemeEkle(ActionEvent event) {
        String malzemeAdi = malzemeAdiGuncellemekIçin.getText();
        float malzemeMiktari = 0;

        if(malzemeMiktariGuncellemekIçin.getText().trim().isEmpty()){
            uyariEkrani.setText("Lütfen Malzeme Miktarını Giriniz");
            return;
        }else{
            malzemeMiktari = Float.parseFloat(malzemeMiktariGuncellemekIçin.getText());
        }

        if(malzemeAdi.trim().isEmpty() || malzemeAdi == null){
            uyariEkrani.setText("Eklemek İçin Bir Malzeme Giriniz");
        }else{
            try{
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                String sql = "INSERT INTO TARIFMALZEMELER (TarifID, MalzemeID, MalzemeMiktar) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, tarifId);
                preparedStatement.setInt(2, suAnkiTumdenMalzemeId);
                preparedStatement.setFloat(3, malzemeMiktari);

                if(malzemelerListesi.contains(malzemeAdi)){
                    uyariEkrani.setText("Lütfen Tarifte Olmayan Bir Malzeme Ekleyiniz");
                }else{
                    if(preparedStatement.executeUpdate() > 0){     // preparedStatement.executeUpdate() bu ifade kaç kayıt döndüğünü gösteriyor
                        // tarife eklediğimiz malzemeyi arrayliste de ekleyip ardından tariftekiMalzemelerListView ı güncelliyoruz
                        malzemelerListesi.add(malzemeAdi); // Arrayliste ekle
                        tariftekiMalzemelerListView.getItems().setAll(malzemelerListesi); // setAll dediğimiz için listView ın içini önce silicek sonra en baştan ekleme yapıcak

                        uyariEkrani.setText("Malzeme Tarife Başarı ile Eklendi");
                        // malzeme adını ve miktarını gösteren alanı boşaltıyoruz ki yeni işlem için hazır olsun
                        malzemeAdiGuncellemekIçin.clear();
                        malzemeMiktariGuncellemekIçin.clear();
                    }else{
                        uyariEkrani.setText("Tarife Malzeme Ekleme İşlemi Başarısız");
                    }
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    @FXML
    void malzemeGuncelle(ActionEvent event) {
        String malzemeAdi = malzemeAdiGuncellemekIçin.getText();
        float malzemeMiktari = 0;
        if(malzemeMiktariGuncellemekIçin.getText().trim().isEmpty()){
            uyariEkrani.setText("Lütfen Malzeme Miktarını Giriniz");
            return;
        }else{
            malzemeMiktari = Float.parseFloat(malzemeMiktariGuncellemekIçin.getText());
        }

        if(malzemeAdi.trim().isEmpty() || malzemeAdi == null){
            uyariEkrani.setText("Güncellemek İçin Bir Malzeme Giriniz");
        }else{
            try{
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                String sql = "UPDATE TARIFMALZEMELER SET MalzemeMiktar = ? WHERE MalzemeID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setFloat(1, malzemeMiktari);
                preparedStatement.setInt(2, suAnkiMalzemeId);

                if(malzemelerListesi.contains(malzemeAdi)){
                    if (preparedStatement.executeUpdate() > 0) {
                        uyariEkrani.setText("Kayıt başarıyla güncellendi.");
                        malzemeAdiGuncellemekIçin.clear();
                        malzemeMiktariGuncellemekIçin.clear();
                    }else{
                        uyariEkrani.setText("Güncelleme sırasında hata oluştu.");
                    }
                }else{
                    uyariEkrani.setText("Lütfen Tarifte Olan Bir Nesneyi Güncelleyiniz");
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    @FXML
    void malzemeSil(ActionEvent event) {
        String malzemeAdi = malzemeAdiGuncellemekIçin.getText();
        if(malzemeAdi.trim().isEmpty() || malzemeAdi == null){
            uyariEkrani.setText("Silmek İçin Bir Malzeme Giriniz");
        }else{
            try{
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                String sql = "DELETE FROM TARIFMALZEMELER WHERE TarifID = ? AND MalzemeID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, tarifId);
                preparedStatement.setInt(2, suAnkiMalzemeId);

                if(malzemelerListesi.contains(malzemeAdi)){
                    if(preparedStatement.executeUpdate() > 0){
                        uyariEkrani.setText("Malzeme Başarı ile Silindi");
                        // malzeme adını ve miktarını gösteren alanı boşaltıyoruz ki yeni işlem için hazır olsun
                        malzemeAdiGuncellemekIçin.clear();
                        malzemeMiktariGuncellemekIçin.clear();

                        // tariftekiMalzemelerListView'ı güncelleyerek sildiğimiz malzemeyi listeden kaldırıyoruz
                        malzemelerListesi.remove(malzemeAdi); // ArrayList'ten kaldır
                        tariftekiMalzemelerListView.getItems().remove(malzemeAdi); // ListView'den kaldır
                    }else{
                        uyariEkrani.setText("Silme İşlemi Başarısız");
                    }
                }else{
                    uyariEkrani.setText("Lütfen Tarifte Olan Bir Nesneyi Siliniz");
                }

            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    @FXML
    void tarifAra(ActionEvent event) {
        if(tarifAdiGiriniz.getText().equals("")){
            tarifAdiListesi.clear(); // Listeyi temizliyoruz

            try{
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM TARIFLER");

                while(resultSet.next()){
                    tarifAdiListesi.add(resultSet.getString("TarifAdi"));
                }
            }catch(SQLException exception){
                exception.printStackTrace();
            }
            // tekrardan tarif adı arama alanını boş bir şekilde çalıştırırsam bütün tarif listesini versin diye bu işlemi yaptım
            if(tarifAdiAramaSayac > 0){
                tariflerListView.getItems().clear();
                tariflerListView.getItems().addAll(tarifAdiListesi);
            }
            tarifAdiAramaSayac++;

        } else {
            // Eğer kullanıcı bir malzeme adı girmişse, o malzeme adını içerenleri bul
            tarifAdiListesi.clear(); // Listeyi temizle
            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                suAnkiTarif = tarifAdiGiriniz.getText();

                // LIKE sorgusu için '%suAnkiTarif%' oluşturulmalı
                String sql = "SELECT * FROM TARIFLER WHERE TarifAdi LIKE ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, "%" + suAnkiTarif + "%");

                ResultSet resultSet2 = preparedStatement.executeQuery();

                while (resultSet2.next()) {
                    tarifAdiListesi.add(resultSet2.getString("TarifAdi"));
                }

                tariflerListView.getItems().setAll(tarifAdiListesi); // ListView'i güncelle
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    @FXML
    void tarifGuncelle(ActionEvent event) {
        String guncellenmisTarifAdi = tarifAdiGuncellemekIçin.getText();
        String guncellenmisKategori = kategoriGuncellemekIçin.getText();
        String guncellenmisTarifi = tarifiGuncellemekIçin.getText();

        if(suresiGuncellemekIçin.getText() == null || suresiGuncellemekIçin.getText().trim().isEmpty()){
            uyariEkrani.setText("Lütfen Tüm Alanları Doldurunuz !!!");
            return;
        }

        int guncellenmisHazirlanmaSuresi = Integer.parseInt(suresiGuncellemekIçin.getText());

        if(guncellenmisTarifAdi == null ||guncellenmisTarifAdi.trim().isEmpty() || guncellenmisKategori == null || guncellenmisKategori.trim().isEmpty() || guncellenmisTarifi == null || guncellenmisTarifi.trim().isEmpty() || guncellenmisHazirlanmaSuresi > 0){
            uyariEkrani.setText("Lütfen Tüm Alanları Doğru Bir Şekilde Doldurunuz");
        }

        try{
            // Veritabanına bağlantısı oluşturuyorum
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                    "springstudent",
                    "springstudent"
            );

            String sql = "UPDATE TARIFLER SET TarifAdi = ?, Kategori = ?, HazirlamaSuresi = ?, Talimatlar = ? WHERE TarifID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, guncellenmisTarifAdi); // Yeni tarif adı
            preparedStatement.setString(2, guncellenmisKategori); // Yeni kategori
            preparedStatement.setInt(3, guncellenmisHazirlanmaSuresi); // Yeni hazırlama süresi
            preparedStatement.setString(4, guncellenmisTarifi); // Yeni talimatlar
            preparedStatement.setInt(5, tarifId); // Güncellenecek tarifin ID'si

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                uyariEkrani.setText("Kayıt başarıyla güncellendi.");
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @FXML
    void tarifSil(ActionEvent event) {
        if(tarifAdiSilmekIçin.getText().equals("") || tarifAdiSilmekIçin.getText() == null){
            uyariEkrani.setText("Lütfen Silmek İçin Bir Tarif Giriniz !!");
        }else{
            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );
                String sql = "DELETE FROM TARIFLER WHERE TarifAdi = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, suAnkiTarif);

                if(preparedStatement.executeUpdate() > 0){
                    uyariEkrani.setText("Kayıt Başarı ile Silindi");
                    // sildikten sonra arayüzdeki yerlerden de siliyoruz
                    tarifAdiSilmekIçin.clear();
                    tarifiGuncellemekIçin.clear();
                    kategoriGuncellemekIçin.clear();
                    suresiGuncellemekIçin.clear();
                    tarifiGuncellemekIçin.clear();
                    tariftekiMalzemelerListView.getItems().clear();
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }


    @FXML
    void anaEkranaGidis(ActionEvent event) throws IOException {
        new SceneSwitch(guncellemeSilmeEkranAnchorPane, "ana-ekran.fxml");
    }

    @FXML
    void tarifEklemeEkraninaGidis(ActionEvent event) throws IOException {
        new SceneSwitch(guncellemeSilmeEkranAnchorPane, "tarif-islemleri.fxml");
    }
}
