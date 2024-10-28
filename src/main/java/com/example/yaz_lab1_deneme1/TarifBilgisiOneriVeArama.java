package com.example.yaz_lab1_deneme1;

public class TarifBilgisiOneriVeArama {
    String tarifAdi;
    float eslesmeYuzdesi;
    float eksikMalzeme;

    public TarifBilgisiOneriVeArama(String tarifAdi, float eslesmeYuzdesi, float eksikMalzeme) {
        this.tarifAdi = tarifAdi;
        this.eslesmeYuzdesi = eslesmeYuzdesi;
        this.eksikMalzeme = eksikMalzeme;
    }


    public String getTarifAdi() {
        return tarifAdi;
    }

    public void setTarifAdi(String tarifAdi) {
        this.tarifAdi = tarifAdi;
    }

    public float getEslesmeYuzdesi() {
        return eslesmeYuzdesi;
    }

    public void setEslesmeYuzdesi(float eslesmeYuzdesi) {
        this.eslesmeYuzdesi = eslesmeYuzdesi;
    }

    public float getEksikMalzeme() {
        return eksikMalzeme;
    }

    public void setEksikMalzeme(float eksikMalzeme) {
        this.eksikMalzeme = eksikMalzeme;
    }

    @Override
    public String toString() {
        return tarifAdi + " -> %" + eslesmeYuzdesi + " -> " + eksikMalzeme + " TL";
    }
}
