package com.example.yaz_lab1_deneme1;

public class TarifBilgisi {
    private String tarifAdi;
    private String hazirlamaSuresi;
    private float maliyet;
    private String kategori;
    private int malzemeSayisi;

    public TarifBilgisi(String tarifAdi, String hazirlamaSuresi, float maliyet, String kategori, int malzemeSayisi) {
        this.tarifAdi = tarifAdi;
        this.hazirlamaSuresi = hazirlamaSuresi;
        this.maliyet = maliyet;
        this.kategori = kategori;
        this.malzemeSayisi = malzemeSayisi;
    }

    public String getTarifAdi() {
        return tarifAdi;
    }

    public String getHazirlamaSuresi() {
        return hazirlamaSuresi;
    }

    public float getMaliyet() {
        return maliyet;
    }

    public String getKategori() {
        return kategori;
    }

    public int getMalzemeSayisi() {
        return malzemeSayisi;
    }
}

