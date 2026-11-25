module com.gridlock {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.gridlock to javafx.fxml;
    exports com.gridlock;
}
