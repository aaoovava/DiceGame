module com.example.dicegame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires junit;
    requires org.junit.jupiter.api;
    requires org.slf4j;

    opens view.applications to javafx.fxml;
    exports view.applications;
    exports controllers;
    opens controllers to javafx.fxml;

    opens unit_tests to junit;
    exports unit_tests to junit;


}