module f25.cs157a.evergreenbank {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires mysql.connector.j;
    requires javafx.graphics;
    requires javafx.base;

    opens f25.cs157a.evergreenbank to javafx.fxml;
    exports f25.cs157a.evergreenbank;
    exports f25.cs157a.evergreenbank.Controllers;
    opens f25.cs157a.evergreenbank.Controllers to javafx.fxml;
    exports f25.cs157a.evergreenbank.Classes;
    opens f25.cs157a.evergreenbank.Classes to javafx.fxml;
    exports f25.cs157a.evergreenbank.Databases;
    opens f25.cs157a.evergreenbank.Databases to javafx.fxml;
}