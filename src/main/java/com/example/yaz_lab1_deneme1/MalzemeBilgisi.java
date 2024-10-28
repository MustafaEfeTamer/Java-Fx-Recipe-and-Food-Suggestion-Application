package com.example.yaz_lab1_deneme1;

public class MalzemeBilgisi {
    String malzemeAdi;
    String malzemeMiktari;

    public MalzemeBilgisi(String malzemeAdi, String malzemeMiktari) {
        this.malzemeAdi = malzemeAdi;
        this.malzemeMiktari = malzemeMiktari;
    }

    public String getMalzemeAdi() {
        return malzemeAdi;
    }

    public void setMalzemeAdi(String malzemeAdi) {
        this.malzemeAdi = malzemeAdi;
    }

    public String getMalzemeMiktari() {
        return malzemeMiktari;
    }

    public void setMalzemeMiktari(String malzemeMiktari) {
        this.malzemeMiktari = malzemeMiktari;
    }
}
