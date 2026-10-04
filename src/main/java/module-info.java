module com.library {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.library;
    exports com.library.models;
    exports com.library.controllers;

    opens com.library.controllers to javafx.fxml;
    opens com.library.models to javafx.base;
}