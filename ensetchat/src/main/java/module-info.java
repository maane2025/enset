module com.example.ensetchat {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.ensetchat to javafx.fxml;
    exports com.example.ensetchat;
}