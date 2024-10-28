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
import java.util.ResourceBundle;


public class TarifOnerisiVeDinamikArama implements Initializable {

    @FXML
    private TextField malzemeAdiGiriniz;

    @FXML
    private ListView<String> malzemelerListView;

    @FXML
    private TextField secilenMalzemeAdi;

    @FXML
    private TextField secilenMalzemeMiktari;

    @FXML
    private TextField tarifAdiGiriniz;

    @FXML
    private AnchorPane tarifOnerisiAnchorPane;

    @FXML
    private ListView<TarifBilgisiOneriVeArama> tariflerListView;

    @FXML
    private Label uyarıMesaji;

    ArrayList<String> malzemeAdiListesi = new ArrayList<>();
    ArrayList<TarifBilgisiOneriVeArama> tarifBilgileriListesi = new ArrayList<>();
    ArrayList<TarifBilgisiOneriVeArama> tarifBilgileriListesiFiltrelenmis = new ArrayList<>();
    ArrayList<MalzemeBilgisi> malzemeBilgisi = new ArrayList<>();
    int malzemeAdiAramaSayac = 0;
    int tarifAdiAramaSayac = 0;
    String suAnkiMalzeme;
    String suAnkiTarif;
    String suAnkiMalzemeMiktari;
    float suAnkiMalzemeBirimFiyat;



    // ilk başta her zaman bu method çalışır
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        malzemeAra(null);
        //tarifAra(null);
        malzemelerListView.getItems().addAll(malzemeAdiListesi);

        malzemelerListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                suAnkiMalzeme = malzemelerListView.getSelectionModel().getSelectedItem();

                try {
                    Connection connection = DriverManager.getConnection(
                            "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                            "springstudent",
                            "springstudent"
                    );

                    String sql = "SELECT ToplamMiktar, BirimFiyat FROM MALZEMELER WHERE MalzemeAdi = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, suAnkiMalzeme);

                    ResultSet resultSet = preparedStatement.executeQuery();

                    if (resultSet.next()) {
                        suAnkiMalzemeMiktari = resultSet.getString("ToplamMiktar");
                        suAnkiMalzemeBirimFiyat = Float.parseFloat(resultSet.getString("BirimFiyat"));
                    }

                    malzemeAdiGiriniz.setText(suAnkiMalzeme);
                    secilenMalzemeAdi.setText(suAnkiMalzeme);
                    secilenMalzemeMiktari.setText(suAnkiMalzemeMiktari);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }


    private boolean malzemelerYeterliMi(String tarifAdi) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                    "springstudent",
                    "springstudent"
            );

            String sql = "SELECT m.MalzemeAdi, tm.MalzemeMiktar FROM TARIFMALZEMELER tm " +
                    "JOIN MALZEMELER m ON tm.MalzemeID = m.MalzemeID " +
                    "JOIN TARIFLER t ON tm.TarifID = t.TarifID WHERE t.TarifAdi = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, tarifAdi);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String malzemeAdi = resultSet.getString("MalzemeAdi");
                double gerekliMiktar = resultSet.getDouble("MalzemeMiktar");

                boolean malzemeBulundu = false;
                for (MalzemeBilgisi malzeme : malzemeBilgisi) {
                    if (malzeme.getMalzemeAdi().equals(malzemeAdi)) {
                        malzemeBulundu = true;
                        double mevcutMiktar = parseMiktar(malzeme.getMalzemeMiktari());

                        if (mevcutMiktar < gerekliMiktar) {
                            return false; // Mevcut miktar yetersiz
                        }
                        break;
                    }
                }

                if (!malzemeBulundu) {
                    return false; // Malzeme yok
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



    @FXML
    void malzemeAra(ActionEvent event) {
        if(malzemeAdiGiriniz.getText().equals("")){

            malzemeAdiListesi.clear(); // Listeyi temizliyorum
            try{
                Connection connection = DriverManager.getConnection(
                        "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                        "springstudent",
                        "springstudent"
                );

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM MALZEMELER");

                while(resultSet.next()){
                    malzemeAdiListesi.add(resultSet.getString("MalzemeAdi"));
                }
            }catch(SQLException exception){
                exception.printStackTrace();
            }
            // tekrardan malzemeAdiArama alanını boş bir şekilde çalıştırırsam bütün malzeme listesini versin diye bu işlemi yaptım
            if(malzemeAdiAramaSayac > 0){
                malzemelerListView.getItems().setAll(malzemeAdiListesi);
            }
            malzemeAdiAramaSayac++;
            secilenMalzemeMiktari.clear();
        } else {
            // Eğer kullanıcı bir malzeme adı girmişse, o malzeme adını içerenleri bul
            malzemeAdiListesi.clear(); // Listeyi temizliyorum
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
                    malzemeAdiListesi.add(resultSet2.getString("MalzemeAdi"));
                }

                malzemelerListView.getItems().setAll(malzemeAdiListesi); // ListView'i güncelle
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            secilenMalzemeMiktari.clear();
        }
    }

    @FXML
    void malzemeEkle(ActionEvent event) {
        if(malzemeAdiGiriniz.getText().equals("") || malzemeAdiGiriniz.getText().equals("")){
            uyarıMesaji.setText("Lütfen Eklemek İçin Bir Malzeme Giriniz");
            secilenMalzemeAdi.clear();
            secilenMalzemeMiktari.clear();
            malzemeAdiGiriniz.clear();
        }else{
            // Kullanıcı bu malzemeyi zaten eklemiş mi eklememiş mi onu kontrol ediyoruz.
            for(MalzemeBilgisi mevcutMalzeme : malzemeBilgisi){
                if(mevcutMalzeme.getMalzemeAdi().equals(malzemeAdiGiriniz.getText())){
                    uyarıMesaji.setText("Bu Malzeme Zaten Eklenmiştir !!");
                    secilenMalzemeAdi.clear();
                    secilenMalzemeMiktari.clear();
                    malzemeAdiGiriniz.clear();
                    return;
                }
            }
            // Kullanıcı mevcut malzemeler listesinin haricinde bir malzeme ekleyemesin diye
            if(malzemeAdiListesi.contains(malzemeAdiGiriniz.getText())){
                uyarıMesaji.setText("Malzeme Başarı İle Eklenmiştir");
                malzemeBilgisi.add(new MalzemeBilgisi(secilenMalzemeAdi.getText(), secilenMalzemeMiktari.getText()));
                secilenMalzemeAdi.clear();
                secilenMalzemeMiktari.clear();
                malzemeAdiGiriniz.clear();
            }else{
                uyarıMesaji.setText("Lütfen Mevcut Bir Malzeme Seçiniz");
            }
        }
    }


    @FXML
    void tarifAra(ActionEvent event) {
        if(tarifAdiGiriniz.getText().equals("")) {
                tariflerListView.getItems().setAll(tarifBilgileriListesi); // ListView'i güncelle
        } else {
            tarifBilgileriListesiFiltrelenmis.clear();

            suAnkiTarif = tarifAdiGiriniz.getText().toLowerCase();

            for(TarifBilgisiOneriVeArama tarifler : tarifBilgileriListesi){
                if(tarifler.getTarifAdi().toLowerCase().contains(suAnkiTarif)){
                    tarifBilgileriListesiFiltrelenmis.add(tarifler);
                }
            }

            tariflerListView.getItems().setAll(tarifBilgileriListesiFiltrelenmis);
        }
    }

    @FXML
    void tarifleriListele(ActionEvent event) {
        tarifBilgileriListesi.clear();

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
                float eslesmeYuzdesi = hesaplaEslesmeYuzdesi(tarifAdi);
                float eksikMalzemeMaliyet = hesaplaEksikMalzemeMaliyeti(tarifAdi);

                // Tarif bilgilerini listeye ekle
                tarifBilgileriListesi.add(new TarifBilgisiOneriVeArama(tarifAdi, eslesmeYuzdesi, eksikMalzemeMaliyet));
            }
            // eslesme yüzdesine göre tarifleri sırala
            tarifleriSirala(tarifBilgileriListesi);

            ObservableList<TarifBilgisiOneriVeArama> observableTarifListesi = FXCollections.observableArrayList(tarifBilgileriListesi);
            tariflerListView.setItems(observableTarifListesi);

            tariflerListView.setCellFactory(listView -> new ListCell<TarifBilgisiOneriVeArama>() {
                @Override
                protected void updateItem(TarifBilgisiOneriVeArama tarifBilgisi, boolean empty) {
                    super.updateItem(tarifBilgisi, empty);
                    if (empty || tarifBilgisi == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(tarifBilgisi.getTarifAdi() + "    Eşleşme Yüzdesi : " + "%" + tarifBilgisi.getEslesmeYuzdesi() + "    Eksik Malzeme Maliyeti : " + tarifBilgisi.getEksikMalzeme() + " TL");
                        if (malzemelerYeterliMi(tarifBilgisi.getTarifAdi())) {
                            setStyle("-fx-background-color: green; -fx-text-fill: white;");
                        } else {
                            setStyle("-fx-background-color: red; -fx-text-fill: white;");
                        }
                    }
                }
            });
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        malzemeAdiGiriniz.clear();
        //malzemeBilgisi.clear();
    }

    private void tarifleriSirala(ArrayList<TarifBilgisiOneriVeArama> tarifListesi) {
        int n = tarifListesi.size();
        boolean swapped;
        // Bubble Sort algoritması
        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                // Eğer şu anki tarifin eşleşme yüzdesi bir sonraki tariften küçükse yer değiştir
                if (tarifListesi.get(j).getEslesmeYuzdesi() < tarifListesi.get(j + 1).getEslesmeYuzdesi()) {
                    // Tarifleri takas et
                    TarifBilgisiOneriVeArama temp = tarifListesi.get(j);
                    tarifListesi.set(j, tarifListesi.get(j + 1));
                    tarifListesi.set(j + 1, temp);
                    swapped = true;
                }
            }
            // Eğer bu turda hiç takas yapılmadıysa, dizi sıralıdır ve döngü sona erdirilir
            if (!swapped) {
                break;
            }
        }
    }


    // Eksik malzeme maliyetini hesapla
    private float hesaplaEksikMalzemeMaliyeti(String tarifAdi) {
        float toplamEksikMaliyet = 0;

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                    "springstudent",
                    "springstudent"
            );

            String sql = "SELECT tm.MalzemeID, tm.MalzemeMiktar, m.BirimFiyat, m.MalzemeAdi FROM TARIFMALZEMELER tm " +
                    "JOIN MALZEMELER m ON tm.MalzemeID = m.MalzemeID " +
                    "JOIN TARIFLER t ON tm.TarifID = t.TarifID " +
                    "WHERE t.TarifAdi = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, tarifAdi);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String malzemeAdi = resultSet.getString("MalzemeAdi");
                float gerekenMiktar = resultSet.getFloat("MalzemeMiktar");
                float birimFiyat = resultSet.getFloat("BirimFiyat");

                // Malzeme mevcut mu ve miktar yeterli mi?
                float mevcutMiktar = getMalzemeMiktari(malzemeAdi);
                if (mevcutMiktar < gerekenMiktar) {
                    // Yetersiz veya eksik miktar için maliyet hesapla
                    float eksikMiktar = gerekenMiktar - mevcutMiktar;
                    toplamEksikMaliyet += eksikMiktar * birimFiyat;
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return toplamEksikMaliyet;
    }


    // Malzeme miktarını al
    private float getMalzemeMiktari(String malzemeAdi) {
        for (MalzemeBilgisi malzeme : malzemeBilgisi) {
            if (malzeme.getMalzemeAdi().equals(malzemeAdi)) {
                return parseMiktarr(malzeme.getMalzemeMiktari());
            }
        }
        return 0;
    }


    private float parseMiktarr(String miktarStr) {
        // Sadece sayısal kısmı alıyoruz, ilk sayıyı bulana kadar karakterleri yok sayacağız
        String numericPart = miktarStr.replaceAll("[^0-9.]", "");
        return Float.parseFloat(numericPart);
    }


    // Malzeme uyum yüzdesini hesapla
    private float hesaplaEslesmeYuzdesi(String tarifAdi) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/yemek_tarifi",
                    "springstudent",
                    "springstudent"
            );

            String sql = "SELECT COUNT(*) AS toplamMalzemeSayisi FROM TARIFMALZEMELER tm " +
                    "JOIN TARIFLER t ON tm.TarifID = t.TarifID WHERE t.TarifAdi = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, tarifAdi);
            ResultSet resultSet = preparedStatement.executeQuery();

            int toplamMalzemeSayisi = 0;
            if (resultSet.next()) {
                toplamMalzemeSayisi = resultSet.getInt("toplamMalzemeSayisi");
            }

            int uyusanMalzemeSayisi = 0;
            for (MalzemeBilgisi malzeme : malzemeBilgisi) {
                String sqlUyusma = "SELECT COUNT(*) AS uyusanMalzeme FROM TARIFMALZEMELER tm " +
                        "JOIN MALZEMELER m ON tm.MalzemeID = m.MalzemeID " +
                        "JOIN TARIFLER t ON tm.TarifID = t.TarifID " +
                        "WHERE t.TarifAdi = ? AND m.MalzemeAdi = ?";
                PreparedStatement uyusmaPreparedStatement = connection.prepareStatement(sqlUyusma);
                uyusmaPreparedStatement.setString(1, tarifAdi);
                uyusmaPreparedStatement.setString(2, malzeme.getMalzemeAdi());

                ResultSet uyusmaResultSet = uyusmaPreparedStatement.executeQuery();
                if (uyusmaResultSet.next() && uyusmaResultSet.getInt("uyusanMalzeme") > 0) {
                    uyusanMalzemeSayisi++;
                }
            }

            if (toplamMalzemeSayisi == 0) {
                return 0;
            }

            return ((float) uyusanMalzemeSayisi / toplamMalzemeSayisi) * 100;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }


    @FXML
    void anaSayfayaDon(ActionEvent event) throws IOException {
        new SceneSwitch(tarifOnerisiAnchorPane, "ana-ekran.fxml");
    }
}

