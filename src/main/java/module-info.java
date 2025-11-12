module f25.cs157a.evergreenbank {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens f25.cs157a.evergreenbank to javafx.fxml;
    exports f25.cs157a.evergreenbank;
}