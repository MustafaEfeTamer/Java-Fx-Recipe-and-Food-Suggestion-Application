module com.example.yaz_lab1_deneme1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.yaz_lab1_deneme1 to javafx.fxml;
    exports com.example.yaz_lab1_deneme1;
}