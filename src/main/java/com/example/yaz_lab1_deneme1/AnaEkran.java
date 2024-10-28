package com.example.yaz_lab1_deneme1;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class AnaEkran implements Initializable {

    @FXML
    private AnchorPane anaEkranAnchorPane;

    @FXML
    private ChoiceBox<String> choiceBoxFiltreleme;

    @FXML
    private ChoiceBox<String> choiceBoxSiralama;

    @FXML
    private TextField tarifAdiGiriniz;

    @FXML
    private Label hazirlamaSuresiBilgisi;

    @FXML
    private Label kategoriBilgisi;

    @FXML
    private Label maliyetBilgisi;

    @FXML
    private Label tarifAdiBilgisi;

    @FXML
    private TextArea tarifArea;

    @FXML
    private ListView<TarifBilgisi> tariflerListView;

    @FXML
    private ListView<String> tariftekiMalzemelerListView;
    ArrayList<Integer> malzemeIdListesi = new ArrayList<>();
    ArrayList<String> malzemelerListesi = new ArrayList<>();
    ArrayList<TarifBilgisi> tarifBilgileriListesi = new ArrayList<>();

    int tarifAdiAramaSayac = 0;
    String suAnkiTarif;
    String tarifKategori;
    String hazirlamaSuresi;
    String tarif;
    int tarifId;
    String[] siralama = {"Hızlı -> Yavaş", "Yavaş -> Hızlı", "Çok Maliyet -> Az Maliyet", "Az Maliyet -> Çok Maliyet"};
    String[] filtreleme = {"Malzeme Sayısı", "Kategori", "Maliyet Aralığı"};



    // ilk başta her zaman bu method çalışır
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        choiceBoxSiralama.getItems().addAll(siralama);
        choiceBoxFiltreleme.getItems().addAll(filtreleme);

        // tariflerListView'a değer getirir
        tarifAra(null);
        ObservableList<TarifBilgisi> observableTarifListesi = FXCollections.observableArrayList(tarifBilgileriListesi);
        tariflerListView.setItems(observableTarifListesi);


        tariflerListView.setCellFactory(listView -> new ListCell<TarifBilgisi>() {
            @Override
            protected void updateItem(TarifBilgisi tarifBilgisi, boolean empty) {
                super.updateItem(tarifBilgisi, empty);
                if (empty || tarifBilgisi == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(tarifBilgisi.getTarifAdi() + " ->  " + tarifBilgisi.getHazirlamaSuresi() + " dk  -> " + tarifBilgisi.getMaliyet() + " TL");
                    if (malzemelerYeterliMi(tarifBilgisi.getTarifAdi())) {
                        setStyle("-fx-background-color: green; -fx-text-fill: white;");
                    } else {
                        setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    }
                }
            }
        });


        // Filtreleme işlemi
        choiceBoxFiltreleme.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("Maliyet Aralığı")) {
                double[] maliyetAraligi = maliyetAraligiAl();
                if (maliyetAraligi != null) {
                    filtrele(observableTarifListesi, newValue, null, maliyetAraligi[0], maliyetAraligi[1], 0);
                }
            } else if (newValue.equals("Kategori")) {
                String kategori = kategoriAl(); // Kullanıcıdan kategori bilgisi alınıyor
                if (kategori != null && !kategori.isEmpty()) {
                    filtrele(observableTarifListesi, newValue, kategori, 0, 0, 0);
                }
            } else if (newValue.equals("Malzeme Sayısı")) {
                int malzemeSayisi = malzemeSayisiAl(); // Kullanıcıdan malzeme sayısı alınıyor
                if (malzemeSayisi >= 0) {
                    filtrele(observableTarifListesi, newValue, null, malzemeSayisi, 0, malzemeSayisi);
                }
            }
        });


        // Sıralama değiştiğinde çağrılacak dinleyici
        choiceBoxSiralama.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Sıralama işlemini gerçekleştirin
            bubbleSort(observableTarifListesi, newValue);
            // Güncellenmiş listeyi ListView'e ata
            tariflerListView.setItems(observableTarifListesi);
        });


        tariflerListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TarifBilgisi>() {
            @Override
            public void changed(ObservableValue<? extends TarifBilgisi> observableValue, TarifBilgisi tarifBilgisi, TarifBilgisi t1) {

                TarifBilgisi suAnkiTarifKayiti = tariflerListView.getSelectionModel().getSelectedItem();

                try {
                    Connection connection = DriverManager.getConnection(
                            "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                            "springstudent",
                            "springstudent"
                    );

                    String sql = "SELECT TarifAdi, TarifID, Kategori, HazirlamaSuresi, Talimatlar FROM TARIFLER WHERE TarifAdi = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, suAnkiTarifKayiti.getTarifAdi());

                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        tarifKategori = resultSet.getString("Kategori");
                        hazirlamaSuresi = resultSet.getString("HazirlamaSuresi");
                        tarif = resultSet.getString("Talimatlar");
                        tarifId = resultSet.getInt("TarifID");
                    }

                    tarifAdiBilgisi.setText(suAnkiTarifKayiti.getTarifAdi());
                    kategoriBilgisi.setText(tarifKategori);
                    hazirlamaSuresiBilgisi.setText(hazirlamaSuresi);
                    maliyetBilgisi.setText(String.valueOf(suAnkiTarifKayiti.getMaliyet()));
                    tarifArea.setText(tarif);


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
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    // Malzemelerin yeterli olup olmadığını kontrol eden yöntem
    private boolean malzemelerYeterliMi(String tarifAdi) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                    "springstudent",
                    "springstudent"
            );

            // Tarifin ihtiyaç duyduğu malzemeleri ve miktarlarını alıyorum
            String sql = "SELECT m.MalzemeAdi, tm.MalzemeMiktar FROM TARIFMALZEMELER tm " +
                    "JOIN MALZEMELER m ON tm.MalzemeID = m.MalzemeID " +
                    "JOIN TARIFLER t ON tm.TarifID = t.TarifID WHERE t.TarifAdi = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, tarifAdi);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String malzemeAdi = resultSet.getString("MalzemeAdi");
                double gerekliMiktar = resultSet.getDouble("MalzemeMiktar");

                // 'malzemeler' tablosunda mevcut miktarı kontrol et
                String miktarSql = "SELECT ToplamMiktar FROM MALZEMELER WHERE MalzemeAdi = ?";
                PreparedStatement miktarPreparedStatement = connection.prepareStatement(miktarSql);
                miktarPreparedStatement.setString(1, malzemeAdi);
                ResultSet miktarResultSet = miktarPreparedStatement.executeQuery();

                if (miktarResultSet.next()) {
                    double mevcutMiktar = parseMiktar(miktarResultSet.getString("ToplamMiktar"));

                    // Eğer mevcut miktar gereken miktardan az ise false döndürüyorum
                    if (mevcutMiktar < gerekliMiktar) {
                        return false;
                    }
                } else {
                    // Eğer malzeme 'malzemeler' tablosunda bulunmuyorsa false döndür
                    return false;
                }
            }
            return true; // Tüm malzemeler yeterli
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    private double parseMiktar(String miktarStr) {
        // Sadece sayısal kısmı alıyoruz, ilk sayıyı bulana kadar karakterleri yok sayacağız
        String numericPart = miktarStr.replaceAll("[^0-9.]", "");
        return Double.parseDouble(numericPart);
    }



    // TextInputDialog ile kullanıcıdan maliyet aralığını alma
    private double[] maliyetAraligiAl() {
        double[] maliyetAraligi = new double[2];

        TextInputDialog minDialog = new TextInputDialog();
        minDialog.setTitle("Minimum Maliyet");
        minDialog.setHeaderText("Lütfen minimum maliyet değerini girin:");
        Optional<String> minResult = minDialog.showAndWait();
        if (minResult.isPresent()) {
            try {
                maliyetAraligi[0] = Double.parseDouble(minResult.get());
            } catch (NumberFormatException e) {
                showErrorAlert("Geçersiz minimum maliyet değeri. Lütfen bir sayı girin.");
                return null;
            }
        }

        TextInputDialog maxDialog = new TextInputDialog();
        maxDialog.setTitle("Maksimum Maliyet");
        maxDialog.setHeaderText("Lütfen maksimum maliyet değerini girin:");
        Optional<String> maxResult = maxDialog.showAndWait();
        if (maxResult.isPresent()) {
            try {
                maliyetAraligi[1] = Double.parseDouble(maxResult.get());
            } catch (NumberFormatException e) {
                showErrorAlert("Geçersiz maksimum maliyet değeri. Lütfen bir sayı girin.");
                return null;
            }
        }

        return maliyetAraligi;
    }


    // TextInputDialog ile kullanıcıdan malzeme sayısını alma
    private int malzemeSayisiAl() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Malzeme Sayısı");
        dialog.setHeaderText("Lütfen malzeme sayısını girin:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                return Integer.parseInt(result.get());
            } catch (NumberFormatException e) {
                showErrorAlert("Geçersiz malzeme sayısı. Lütfen bir tam sayı girin.");
            }
        }
        return -1; // Geçerli bir giriş olmadığında veya hata durumunda -1 döndürür
    }

    // TextInputDialog ile kullanıcıdan kategori bilgisini alma
    private String kategoriAl() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Kategori");
        dialog.setHeaderText("Lütfen kategori adını girin:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get().trim(); // Girilen kategori ismini döndür
        }
        return null; // Geçerli bir giriş olmadığında null döndürür
    }


    // Hata mesajı göstermek için kullanılan yardımcı metod
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Filtreleme işlemi
    private void filtrele(ObservableList<TarifBilgisi> tarifListesi, String filtrelemeSecimi, String kategori, double minMaliyet, double maxMaliyet, int malzemeSayisi) {
        ObservableList<TarifBilgisi> filtrelenmisListe = FXCollections.observableArrayList();

        for (TarifBilgisi tarif : tarifListesi) {
            boolean ekle = false;

            if (filtrelemeSecimi.equals("Kategori") && tarif.getKategori().equalsIgnoreCase(kategori)) {
                ekle = true;
            } else if (filtrelemeSecimi.equals("Maliyet Aralığı") && tarif.getMaliyet() >= minMaliyet && tarif.getMaliyet() <= maxMaliyet) {
                ekle = true;
            } else if (filtrelemeSecimi.equals("Malzeme Sayısı") && tarif.getMalzemeSayisi() == malzemeSayisi) {
                ekle = true;
            }

            if (ekle) {
                filtrelenmisListe.add(tarif);
            }
        }

        tariflerListView.setItems(filtrelenmisListe);
    }



    // Bubble Sort algoritması ile sıralamlarımı yapıyorum
    private void bubbleSort(ObservableList<TarifBilgisi> tarifListesi, String siralamaSecimi) {
        int n = tarifListesi.size();
        boolean swapped;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;

            for (int j = 0; j < n - 1 - i; j++) {
                int hazirlamaSuresi1 = Integer.parseInt(tarifListesi.get(j).getHazirlamaSuresi());
                int hazirlamaSuresi2 = Integer.parseInt(tarifListesi.get(j + 1).getHazirlamaSuresi());
                double maliyet1 = tarifListesi.get(j).getMaliyet();
                double maliyet2 = tarifListesi.get(j + 1).getMaliyet();

                if (siralamaSecimi.equals("Hızlı -> Yavaş") && hazirlamaSuresi1 > hazirlamaSuresi2) {
                    swap(tarifListesi, j, j + 1);
                    swapped = true;
                } else if (siralamaSecimi.equals("Yavaş -> Hızlı") && hazirlamaSuresi1 < hazirlamaSuresi2) {
                    swap(tarifListesi, j, j + 1);
                    swapped = true;
                } else if (siralamaSecimi.equals("Çok Maliyet -> Az Maliyet") && maliyet1 < maliyet2) {
                    swap(tarifListesi, j, j + 1);
                    swapped = true;
                } else if (siralamaSecimi.equals("Az Maliyet -> Çok Maliyet") && maliyet1 > maliyet2) {
                    swap(tarifListesi, j, j + 1);
                    swapped = true;
                }
            }

            if (!swapped) {
                break; // Eğer hiçbir değişiklik olmadıysa döngüden çık
            }
        }
    }

    private void swap(ObservableList<TarifBilgisi> list, int i, int j) {
        TarifBilgisi temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }


    @FXML
    void tarifAra(ActionEvent event) {
        if(tarifAdiGiriniz.getText().equals("")) {
            tarifBilgileriListesi.clear(); // Listeyi temizliyoruz

            tarifAdiBilgisi.setText("");
            kategoriBilgisi.setText("");
            hazirlamaSuresiBilgisi.setText("");
            maliyetBilgisi.setText("");
            tarifArea.setText("");

            try {
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM TARIFLER");

                while (resultSet.next()) {
                    String tarifAdi = resultSet.getString("TarifAdi");
                    String hazirlamaSuresi = resultSet.getString("HazirlamaSuresi");
                    String kategori = resultSet.getString("Kategori");

                    // Maliyeti hesaplamak için gerekli işlemleri yapalım
                    int tarifId = resultSet.getInt("TarifID");
                    float toplamMaliyet = 0.0F;

                    // Tarifin malzemelerini al
                    String malzemeSql = "SELECT MalzemeID FROM TARIFMALZEMELER WHERE TarifID = ?";
                    PreparedStatement malzemePreparedStatement = connection.prepareStatement(malzemeSql);
                    malzemePreparedStatement.setInt(1, tarifId);
                    ResultSet malzemeResultSet = malzemePreparedStatement.executeQuery();

                    int malzemeSayisi = 0;
                    // Malzeme miktarlarını ve birim fiyatlarını al
                    while (malzemeResultSet.next()) {
                        int malzemeID = malzemeResultSet.getInt("MalzemeID");

                        // Miktarı al
                        String miktarSql = "SELECT MalzemeMiktar FROM TARIFMALZEMELER WHERE MalzemeID = ? AND TarifID = ?";
                        PreparedStatement miktarPreparedStatement = connection.prepareStatement(miktarSql);
                        miktarPreparedStatement.setInt(1, malzemeID);
                        miktarPreparedStatement.setInt(2, tarifId);
                        ResultSet miktarResultSet = miktarPreparedStatement.executeQuery();

                        float malzemeMiktari = 0.0F;
                        if (miktarResultSet.next()) {
                            malzemeMiktari = miktarResultSet.getFloat("MalzemeMiktar");
                        }

                        // Birim fiyatı al
                        String fiyatSql = "SELECT BirimFiyat FROM MALZEMELER WHERE MalzemeID = ?";
                        PreparedStatement fiyatPreparedStatement = connection.prepareStatement(fiyatSql);
                        fiyatPreparedStatement.setInt(1, malzemeID);
                        ResultSet fiyatResultSet = fiyatPreparedStatement.executeQuery();

                        float birimFiyat = 0.0F;
                        if (fiyatResultSet.next()) {
                            birimFiyat = fiyatResultSet.getFloat("BirimFiyat");
                        }

                        // Toplam maliyeti hesapla
                        toplamMaliyet += malzemeMiktari * birimFiyat;

                        malzemeSayisi++;
                    }

                    // Tarif bilgilerini listeye ekle
                    tarifBilgileriListesi.add(new TarifBilgisi(tarifAdi, hazirlamaSuresi, toplamMaliyet, kategori, malzemeSayisi));
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }

            // tekrardan tarif adı arama alanını boş bir şekilde çalıştırırsam bütün tarif listesini versin diye bu işlemi yaptım
            if(tarifAdiAramaSayac > 0) {
                tariflerListView.getItems().clear();
                tariflerListView.getItems().addAll(tarifBilgileriListesi);
            }
            tarifAdiAramaSayac++;

        } else {
            // Eğer kullanıcı bir malzeme adı girmişse, o malzeme adını içerenleri bul
            tarifBilgileriListesi.clear(); // Listeyi temizle
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
                    String tarifAdi = resultSet2.getString("TarifAdi");
                    String hazirlamaSuresi = resultSet2.getString("HazirlamaSuresi");
                    String kategori = resultSet2.getString("Kategori");

                    int tarifId = resultSet2.getInt("TarifID");
                    float toplamMaliyet = 0.0F;

                    // Tarifin malzemelerini al
                    String malzemeSql = "SELECT MalzemeID FROM TARIFMALZEMELER WHERE TarifID = ?";
                    PreparedStatement malzemePreparedStatement = connection.prepareStatement(malzemeSql);
                    malzemePreparedStatement.setInt(1, tarifId);
                    ResultSet malzemeResultSet = malzemePreparedStatement.executeQuery();


                    int malzemeSayisi = 0;
                    // Malzeme miktarlarını ve birim fiyatlarını alıyorum
                    while (malzemeResultSet.next()) {
                        int malzemeID = malzemeResultSet.getInt("MalzemeID");

                        // Miktarı al
                        String miktarSql = "SELECT MalzemeMiktar FROM TARIFMALZEMELER WHERE MalzemeID = ? AND TarifID = ?";
                        PreparedStatement miktarPreparedStatement = connection.prepareStatement(miktarSql);
                        miktarPreparedStatement.setInt(1, malzemeID);
                        miktarPreparedStatement.setInt(2, tarifId);
                        ResultSet miktarResultSet = miktarPreparedStatement.executeQuery();

                        float malzemeMiktari = 0.0F;
                        if (miktarResultSet.next()) {
                            malzemeMiktari = miktarResultSet.getFloat("MalzemeMiktar");
                        }

                        // Birim fiyatı al
                        String fiyatSql = "SELECT BirimFiyat FROM MALZEMELER WHERE MalzemeID = ?";
                        PreparedStatement fiyatPreparedStatement = connection.prepareStatement(fiyatSql);
                        fiyatPreparedStatement.setInt(1, malzemeID);
                        ResultSet fiyatResultSet = fiyatPreparedStatement.executeQuery();

                        float birimFiyat = 0.0F;
                        if (fiyatResultSet.next()) {
                            birimFiyat = fiyatResultSet.getFloat("BirimFiyat");
                        }

                        // Toplam maliyeti hesapla
                        toplamMaliyet += malzemeMiktari * birimFiyat;

                        malzemeSayisi++;
                    }

                    // Tarif bilgilerini listeye ekle
                    tarifBilgileriListesi.add(new TarifBilgisi(tarifAdi, hazirlamaSuresi, toplamMaliyet, kategori, malzemeSayisi));
                }

                tariflerListView.getItems().setAll(tarifBilgileriListesi); // ListView'i güncelle
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    @FXML
    void tarifIslemleriMenu(ActionEvent event) throws IOException {
        new SceneSwitch(anaEkranAnchorPane, "tarif-islemleri.fxml");
    }


    @FXML
    void tarifOneriVeDinamikArama(ActionEvent event) throws IOException {
        new SceneSwitch(anaEkranAnchorPane, "tarifOnerisi-ve-dinamikArama.fxml");
    }
}
